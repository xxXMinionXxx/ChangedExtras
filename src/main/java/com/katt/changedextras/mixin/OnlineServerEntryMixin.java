package com.katt.changedextras.mixin;

import com.katt.changedextras.client.discovery.ChangedExtrasServerMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public class OnlineServerEntryMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ServerData serverData;

    @Inject(method = "render", at = @At("TAIL"))
    private void changedextras$renderMarker(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                            int mouseX, int mouseY, boolean hovered, float partialTick, CallbackInfo ci) {
        if (!((ChangedExtrasServerMarker) this.serverData).changedextras$hasChangedExtras()) {
            return;
        }

        guiGraphics.drawString(this.minecraft.font, "CE", left + width - 33, top + 10, 0x7CFF7C, false);
    }
}
