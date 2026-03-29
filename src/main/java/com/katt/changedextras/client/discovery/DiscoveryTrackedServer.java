package com.katt.changedextras.client.discovery;

import net.minecraft.client.multiplayer.ServerData;

public final class DiscoveryTrackedServer {
    private final ServerData serverData;
    private boolean scanComplete;
    private boolean hasChangedExtras;
    private boolean discoveryEnabled;

    public DiscoveryTrackedServer(ServerData serverData) {
        this.serverData = serverData;
    }

    public ServerData serverData() {
        return serverData;
    }

    public boolean scanComplete() {
        return scanComplete;
    }

    public boolean hasChangedExtras() {
        return hasChangedExtras;
    }

    public boolean discoveryEnabled() {
        return discoveryEnabled;
    }

    public void applyScan(boolean hasChangedExtras, boolean discoveryEnabled) {
        this.scanComplete = true;
        this.hasChangedExtras = hasChangedExtras;
        this.discoveryEnabled = discoveryEnabled;
    }
}
