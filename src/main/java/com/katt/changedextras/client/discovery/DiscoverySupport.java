package com.katt.changedextras.client.discovery;

import com.katt.changedextras.ChangedExtras;
import com.katt.changedextras.network.DiscoveryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.ServerStatusPing;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public final class DiscoverySupport {
    public static final UUID AUTHORIZED_USER = UUID.fromString("70080b3e-8cf3-46f3-922e-7b3a32269935");

    private DiscoverySupport() {
    }

    public static boolean isAuthorizedUser(@Nullable Minecraft minecraft) {
        return minecraft != null && Objects.equals(minecraft.getUser().getProfileId(), AUTHORIZED_USER);
    }

    public static boolean hasChangedExtras(@Nullable ServerStatusPing forgeData) {
        return forgeData != null && forgeData.getRemoteModData().containsKey(ChangedExtras.MODID);
    }

    public static boolean isDiscoveryEnabled(@Nullable ServerStatusPing forgeData) {
        if (forgeData == null) {
            return false;
        }

        ServerStatusPing.ChannelData channelData = forgeData.getRemoteChannels().get(DiscoveryNetwork.CHANNEL_ID);
        return channelData != null && DiscoveryNetwork.isDiscoveryEnabledVersion(channelData.version());
    }
}
