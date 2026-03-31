package com.katt.changedextras.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ChangedExtrasNetwork {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("changedextras", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, JackpotStatePacket.class,
                JackpotStatePacket::encode,
                JackpotStatePacket::decode,
                JackpotStatePacket::handle);
        INSTANCE.registerMessage(id++, SaveArtistBrushPacket.class,
                SaveArtistBrushPacket::encode,
                SaveArtistBrushPacket::decode,
                SaveArtistBrushPacket::handle);
        INSTANCE.registerMessage(id++, SyncArtistColorPacket.class,
                SyncArtistColorPacket::encode,
                SyncArtistColorPacket::decode,
                SyncArtistColorPacket::handle);
    }
}
