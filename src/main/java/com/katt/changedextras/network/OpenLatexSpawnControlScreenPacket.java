package com.katt.changedextras.network;

import com.katt.changedextras.client.LatexSpawnControlScreen;
import com.katt.changedextras.common.LatexSpawnVariantEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenLatexSpawnControlScreenPacket {
    private final boolean allowDaySpawns;
    private final List<LatexSpawnVariantEntry> variants;

    public OpenLatexSpawnControlScreenPacket(boolean allowDaySpawns, List<LatexSpawnVariantEntry> variants) {
        this.allowDaySpawns = allowDaySpawns;
        this.variants = List.copyOf(variants);
    }

    public static void encode(OpenLatexSpawnControlScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.allowDaySpawns);
        buf.writeVarInt(msg.variants.size());
        for (LatexSpawnVariantEntry variant : msg.variants) {
            buf.writeUtf(variant.entityTypeId());
            buf.writeUtf(variant.displayName());
            buf.writeBoolean(variant.enabled());
        }
    }

    public static OpenLatexSpawnControlScreenPacket decode(FriendlyByteBuf buf) {
        boolean allowDaySpawns = buf.readBoolean();
        int size = buf.readVarInt();
        List<LatexSpawnVariantEntry> variants = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            variants.add(new LatexSpawnVariantEntry(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readBoolean()
            ));
        }
        return new OpenLatexSpawnControlScreenPacket(allowDaySpawns, variants);
    }

    public static void handle(OpenLatexSpawnControlScreenPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAccess.open(msg.allowDaySpawns, msg.variants)));
        ctx.setPacketHandled(true);
    }

    private static final class ClientAccess {
        private static void open(boolean allowDaySpawns, List<LatexSpawnVariantEntry> variants) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new LatexSpawnControlScreen(allowDaySpawns, variants));
        }
    }
}
