package com.katt.changedextras.network;

import com.katt.changedextras.item.ArtistBrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SaveArtistBrushPacket {
    private final InteractionHand hand;
    private final String texturePath;
    private final String hexColor;
    private final int uvX;
    private final int uvY;
    private final boolean customUvEnabled;

    public SaveArtistBrushPacket(InteractionHand hand, String texturePath, String hexColor, int uvX, int uvY, boolean customUvEnabled) {
        this.hand = hand;
        this.texturePath = texturePath;
        this.hexColor = hexColor;
        this.uvX = uvX;
        this.uvY = uvY;
        this.customUvEnabled = customUvEnabled;
    }

    public static void encode(SaveArtistBrushPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeUtf(msg.texturePath, 1024);
        buf.writeUtf(msg.hexColor, 16);
        buf.writeInt(msg.uvX);
        buf.writeInt(msg.uvY);
        buf.writeBoolean(msg.customUvEnabled);
    }

    public static SaveArtistBrushPacket decode(FriendlyByteBuf buf) {
        return new SaveArtistBrushPacket(
                buf.readEnum(InteractionHand.class),
                buf.readUtf(1024),
                buf.readUtf(16),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void handle(SaveArtistBrushPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = player.getItemInHand(msg.hand);
            if (!(stack.getItem() instanceof ArtistBrushItem)) {
                return;
            }

            CompoundTag brushData = ArtistBrushItem.getOrCreateBrushData(stack);
            brushData.putString(ArtistBrushItem.TEXTURE_PATH_TAG, msg.texturePath);
            brushData.putString(ArtistBrushItem.HEX_COLOR_TAG, msg.hexColor);
            brushData.putInt(ArtistBrushItem.UV_X_TAG, msg.uvX);
            brushData.putInt(ArtistBrushItem.UV_Y_TAG, msg.uvY);
            brushData.putBoolean(ArtistBrushItem.CUSTOM_UV_ENABLED_TAG, msg.customUvEnabled);
            brushData.putString(ArtistBrushItem.TARGET_FORM_TAG, ArtistBrushItem.TARGET_FORM_ID);

            String targetUuid = brushData.getString(ArtistBrushItem.SELECTED_TARGET_UUID_TAG);
            if (!targetUuid.isBlank()) {
                Entity target = player.serverLevel().getEntity(java.util.UUID.fromString(targetUuid));
                if (target != null) {
                    int color = parseColor(msg.hexColor);
                    ChangedExtrasNetwork.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                            new SyncArtistColorPacket(target.getId(), color, msg.texturePath, true, msg.uvX, msg.uvY, msg.customUvEnabled)
                    );
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private static int parseColor(String hexColor) {
        String normalized = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException ignored) {
            return 0xFFFFFF;
        }
    }
}
