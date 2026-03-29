package com.katt.changedextras.network;

import com.katt.changedextras.ChangedExtras;
import com.katt.changedextras.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class DiscoveryNetwork {
    public static final ResourceLocation CHANNEL_ID =
            ResourceLocation.fromNamespaceAndPath(ChangedExtras.MODID, "discovery");

    private static final String PROTOCOL_VERSION = "1";
    private static final String ENABLED_VERSION = PROTOCOL_VERSION + ":enabled";
    private static final String DISABLED_VERSION = PROTOCOL_VERSION + ":disabled";

    @SuppressWarnings("unused")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_ID,
            DiscoveryNetwork::getAdvertisedVersion,
            NetworkRegistry.acceptMissingOr(DiscoveryNetwork::isCompatibleVersion),
            NetworkRegistry.acceptMissingOr(DiscoveryNetwork::isCompatibleVersion)
    );

    private DiscoveryNetwork() {
    }

    public static void bootstrap() {
    }

    public static boolean isDiscoveryEnabledVersion(String version) {
        return ENABLED_VERSION.equals(version);
    }

    private static String getAdvertisedVersion() {
        return Config.serverDiscoveryEnabled ? ENABLED_VERSION : DISABLED_VERSION;
    }

    private static boolean isCompatibleVersion(String version) {
        return version != null && version.startsWith(PROTOCOL_VERSION + ":");
    }
}
