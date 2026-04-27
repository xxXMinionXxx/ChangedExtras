package com.katt.changedextras.network;

import com.katt.changedextras.common.ChangedExtrasGameRules;
import com.katt.changedextras.common.LatexSpawnSelectionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class UpdateLatexSpawnRulesPacket {
    private final boolean allowDaySpawns;
    private final Set<String> disabledVariantIds;

    public UpdateLatexSpawnRulesPacket(boolean allowDaySpawns, Set<String> disabledVariantIds) {
        this.allowDaySpawns = allowDaySpawns;
        this.disabledVariantIds = Set.copyOf(disabledVariantIds);
    }

    public static void encode(UpdateLatexSpawnRulesPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.allowDaySpawns);
        buf.writeVarInt(msg.disabledVariantIds.size());
        for (String id : msg.disabledVariantIds) {
            buf.writeUtf(id);
        }
    }

    public static UpdateLatexSpawnRulesPacket decode(FriendlyByteBuf buf) {
        boolean allowDaySpawns = buf.readBoolean();
        int size = buf.readVarInt();
        Set<String> disabledVariantIds = new HashSet<>();
        for (int i = 0; i < size; i++) {
            disabledVariantIds.add(buf.readUtf());
        }
        return new UpdateLatexSpawnRulesPacket(allowDaySpawns, disabledVariantIds);
    }

    public static void handle(UpdateLatexSpawnRulesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !player.hasPermissions(2)) {
                return;
            }

            MinecraftServer server = player.server;
            GameRules gameRules = server.getGameRules();
            gameRules.getRule(ChangedExtrasGameRules.LATEX_SPAWN_IN_DAY).set(msg.allowDaySpawns, server);
            LatexSpawnSelectionData.get(server).replaceDisabledIds(msg.disabledVariantIds);
        });
        ctx.setPacketHandled(true);
    }
}
