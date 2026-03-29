package com.katt.changedextras.mixin;

import com.katt.changedextras.client.discovery.ChangedExtrasServerMarker;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerData.class)
public class ServerDataMixin implements ChangedExtrasServerMarker {
    @Unique
    private boolean changedextras$hasChangedExtras;

    @Override
    public boolean changedextras$hasChangedExtras() {
        return this.changedextras$hasChangedExtras;
    }

    @Override
    public void changedextras$setHasChangedExtras(boolean hasChangedExtras) {
        this.changedextras$hasChangedExtras = hasChangedExtras;
    }
}
