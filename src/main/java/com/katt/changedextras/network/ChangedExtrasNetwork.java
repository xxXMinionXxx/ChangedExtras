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
        // This registers the Jackpot packet into the channel
        INSTANCE.registerMessage(id++, JackpotStatePacket.class,
                JackpotStatePacket::encode,
                JackpotStatePacket::decode,
                JackpotStatePacket::handle);
    }
}