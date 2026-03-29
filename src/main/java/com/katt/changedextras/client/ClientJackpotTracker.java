package com.katt.changedextras.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changedextras", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientJackpotTracker {
    private static boolean vignetteActive = false;
    private static final ResourceLocation VIGNETTE = new ResourceLocation("minecraft", "textures/misc/vignette.png");

    public static void setVignetteActive(boolean active) {
        vignetteActive = active;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!vignetteActive) return;

        // Render after the hotbar to ensure it overlays the world correctly
        if (event.getOverlay().id().getPath().equals("hotbar")) {
            drawPulse(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
    }

    private static void drawPulse(GuiGraphics gui, int w, int h) {
        float msPerBeat = 60000.0f / 126.0f;
        float phase = (System.currentTimeMillis() % (long)msPerBeat) / msPerBeat;

        // A sharper "pop" for the pulse (Sine squared)
        float alpha = (float) Math.pow(Math.sin(phase * Math.PI), 2) * 0.5f;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        // ADDITIVE BLENDING: This makes it glow/brighten instead of darken
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

        // Bright Lime Green
        RenderSystem.setShaderColor(0.0f, 0.8f, 0.1f, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        gui.blit(VIGNETTE, 0, 0, 0, 0, w, h, w, h);

        // Reset to default blending and color
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (!vignetteActive) return;

        float msPerBeat = 60000.0f / 124.0f;
        long currentTime = System.currentTimeMillis();
        float phase = (currentTime % (long)msPerBeat) / msPerBeat;
        float shakeEnvelope = (float) Math.pow(Math.sin(phase * Math.PI), 4);
        float pitchIntensity = 1.5f;
        float yawIntensity = 1.5f;

        float smoothSway = (float) Math.sin(currentTime * 0.005f) * yawIntensity;
        float smoothPitch = (float) Math.cos(currentTime * 0.003f) * pitchIntensity;


        event.setPitch(event.getPitch() + (smoothPitch * shakeEnvelope));
        event.setYaw(event.getYaw() + (smoothSway * shakeEnvelope));
    }
}