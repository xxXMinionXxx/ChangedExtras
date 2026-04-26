package com.katt.changedextras.client;

import com.katt.changedextras.common.debug.LatexDebugSnapshot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LatexDebugOverlay {
    private static final Map<Integer, LatexDebugSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static volatile boolean enabled;

    private LatexDebugOverlay() {
    }

    public static void setEnabled(boolean enabled) {
        LatexDebugOverlay.enabled = enabled;
        if (!enabled) {
            SNAPSHOTS.clear();
        }
    }

    public static void updateSnapshots(List<LatexDebugSnapshot> snapshots) {
        SNAPSHOTS.clear();
        for (LatexDebugSnapshot snapshot : snapshots) {
            SNAPSHOTS.put(snapshot.entityId(), snapshot);
        }
    }

    public static void clear() {
        SNAPSHOTS.clear();
        enabled = false;
    }

    public static void render(RenderLevelStageEvent event) {
        if (!enabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || SNAPSHOTS.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        var cameraPos = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (LatexDebugSnapshot snapshot : SNAPSHOTS.values()) {
            VertexConsumer lines = buffer.getBuffer(RenderType.lines());
            Entity entity = minecraft.level.getEntity(snapshot.entityId());
            if (!(entity instanceof ChangedEntity changedEntity)) {
                continue;
            }

            LevelRenderer.renderLineBox(poseStack, lines, changedEntity.getBoundingBox(), 0.15F, 0.85F, 1.0F, 0.95F);
            renderMarker(poseStack, lines, snapshot.targetPos(), 1.0F, 0.2F, 0.2F);
            renderMarker(poseStack, lines, snapshot.lastSeenPos(), 1.0F, 0.85F, 0.2F);
            renderMarker(poseStack, lines, snapshot.buildPos(), 0.2F, 1.0F, 0.35F);
            renderMarker(poseStack, lines, snapshot.breakPos(), 1.0F, 0.45F, 0.1F);
            for (BlockPos imaginedPos : snapshot.imaginedBuildPath()) {
                renderImaginedBuildNode(poseStack, lines, imaginedPos);
            }

            for (BlockPos nodePos : snapshot.pathNodes()) {
                renderPathNode(poseStack, lines, nodePos);
            }
        }
        poseStack.popPose();
        buffer.endBatch(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (LatexDebugSnapshot snapshot : SNAPSHOTS.values()) {
            Entity entity = minecraft.level.getEntity(snapshot.entityId());
            if (entity instanceof ChangedEntity changedEntity) {
                renderPathLabel(minecraft, poseStack, buffer, snapshot, changedEntity);
            }
        }
        poseStack.popPose();
        buffer.endBatch();
    }

    private static void renderMarker(PoseStack poseStack, VertexConsumer lines, BlockPos pos, float red, float green, float blue) {
        if (pos == null) {
            return;
        }

        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                pos.getX() + 0.05D,
                pos.getY() + 0.05D,
                pos.getZ() + 0.05D,
                pos.getX() + 0.95D,
                pos.getY() + 0.95D,
                pos.getZ() + 0.95D,
                red,
                green,
                blue,
                0.95F
        );
    }

    private static void renderPathNode(PoseStack poseStack, VertexConsumer lines, BlockPos pos) {
        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                pos.getX() + 0.2D,
                pos.getY() + 0.2D,
                pos.getZ() + 0.2D,
                pos.getX() + 0.8D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.8D,
                0.35F,
                0.7F,
                1.0F,
                0.85F
        );
    }

    private static void renderImaginedBuildNode(PoseStack poseStack, VertexConsumer lines, BlockPos pos) {
        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                pos.getX() + 0.1D,
                pos.getY() + 0.1D,
                pos.getZ() + 0.1D,
                pos.getX() + 0.9D,
                pos.getY() + 0.9D,
                pos.getZ() + 0.9D,
                0.1F,
                0.95F,
                0.95F,
                0.75F
        );
    }

    private static void renderPathLabel(Minecraft minecraft, PoseStack poseStack, MultiBufferSource.BufferSource buffer,
                                        LatexDebugSnapshot snapshot, ChangedEntity entity) {
        Font font = minecraft.font;
        BlockPos anchorPos = !snapshot.pathNodes().isEmpty()
                ? snapshot.pathNodes().get(0)
                : (!snapshot.imaginedBuildPath().isEmpty() ? snapshot.imaginedBuildPath().get(0) : entity.blockPosition());
        String label = "RequieresBreak: " + snapshot.requiresBreak();

        poseStack.pushPose();
        poseStack.translate(anchorPos.getX() + 0.5D, anchorPos.getY() + 1.35D, anchorPos.getZ() + 0.5D);
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = 0.025F;
        poseStack.scale(-scale, -scale, scale);
        float x = -font.width(label) / 2.0F;
        font.drawInBatch(label, x, 0.0F, 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);
        poseStack.popPose();
    }
}
