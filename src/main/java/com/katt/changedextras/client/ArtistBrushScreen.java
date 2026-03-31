package com.katt.changedextras.client;

import com.katt.changedextras.item.ArtistBrushItem;
import com.katt.changedextras.network.ChangedExtrasNetwork;
import com.katt.changedextras.network.SaveArtistBrushPacket;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.ltxprogrammer.changed.client.renderer.CustomLatexRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArtistBrushScreen extends Screen {
    private static final int PREVIEW_MAX_WIDTH = 220;
    private static final int PREVIEW_MAX_HEIGHT = 220;
    private static final int CUSTOM_LATEX_ATLAS_SIZE = 160;
    private static final int UV_HANDLE_SIZE = 8;
    private static final UvRegion[] UV_REGIONS = new UvRegion[] {
            new UvRegion("Head", 32, 44, 8, 8, 0xAA4D96FF),
            new UvRegion("Snout", 108, 70, 4, 2, 0xAA7AE582),
            new UvRegion("Short Hair", 32, 60, 8, 6, 0xAA8E7CFF),
            new UvRegion("Long Hair", 36, 25, 8, 11, 0xAA8E7CFF),
            new UvRegion("Generic Torso", 0, 62, 8, 12, 0xAAFF8C42),
            new UvRegion("Chiseled Torso", 68, 16, 8, 12, 0xAAF7D154),
            new UvRegion("Female Torso", 0, 78, 8, 12, 0xAAFF9BB0),
            new UvRegion("Heavy Torso", 64, 56, 8, 9, 0xAAFFB347),
            new UvRegion("Right Arm", 24, 83, 4, 12, 0xAAF06292),
            new UvRegion("Left Arm", 68, 79, 4, 12, 0xAAF06292),
            new UvRegion("Right Leg", 92, 37, 4, 7, 0xAA4ADEDE),
            new UvRegion("Left Leg", 40, 92, 4, 7, 0xAA4ADEDE),
            new UvRegion("Abdomen", 86, 0, 8, 4, 0xAA4ED0A8),
            new UvRegion("Lower Abdomen", 64, 44, 9, 7, 0xAA4ED0A8),
            new UvRegion("Lower Torso", 0, 0, 8, 19, 0xAAC77DFF),
            new UvRegion("Front Left Leg", 34, 103, 4, 7, 0xAA5CC8FF),
            new UvRegion("Front Right Leg", 50, 104, 4, 7, 0xAA5CC8FF),
            new UvRegion("Back Left Leg", 74, 98, 4, 7, 0xAA5CC8FF),
            new UvRegion("Back Right Leg", 90, 98, 4, 7, 0xAA5CC8FF),
            new UvRegion("Pad Left", 66, 109, 4, 2, 0xAAFFD166),
            new UvRegion("Pad Right", 106, 108, 4, 2, 0xAAFFD166)
    };

    private final InteractionHand hand;
    private final ItemStack stack;
    private EditBox texturePath;
    private EditBox hexColor;
    private EditBox uvX;
    private EditBox uvY;
    private boolean customUvEnabled;
    private boolean draggingUv;
    private int leftPaneX;
    private int topPaneY;
    private int leftPaneWidth;
    private int previewX;
    private int previewY;
    private PreviewTextureData cachedPreviewData;
    private String cachedPreviewPath = "";

    public ArtistBrushScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("screen.changedextras.artist_brush"));
        this.hand = hand;
        this.stack = stack.copy();
    }

    @Override
    protected void init() {
        super.init();
        recalculateLayout();
        var brushData = ArtistBrushItem.getOrCreateBrushData(stack);
        customUvEnabled = brushData.getBoolean(ArtistBrushItem.CUSTOM_UV_ENABLED_TAG);

        int fieldWidth = Math.max(140, leftPaneWidth - 82);
        texturePath = new EditBox(this.font, leftPaneX, topPaneY + 20, fieldWidth, 20, Component.translatable("screen.changedextras.artist_brush.texture"));
        texturePath.setValue(brushData.getString(ArtistBrushItem.TEXTURE_PATH_TAG));
        addRenderableWidget(texturePath);

        addRenderableWidget(Button.builder(Component.translatable("screen.changedextras.artist_brush.browse"), button -> chooseTextureFile())
                .bounds(leftPaneX + fieldWidth + 8, topPaneY + 20, Math.max(70, leftPaneWidth - fieldWidth - 8), 20)
                .build());

        hexColor = new EditBox(this.font, leftPaneX, topPaneY + 78, Math.min(160, leftPaneWidth), 20, Component.translatable("screen.changedextras.artist_brush.hex"));
        hexColor.setValue(brushData.getString(ArtistBrushItem.HEX_COLOR_TAG));
        addRenderableWidget(hexColor);

        addRenderableWidget(Button.builder(toggleText(), button -> {
                    customUvEnabled = !customUvEnabled;
                    button.setMessage(toggleText());
                    updateUvControls();
                })
                .bounds(leftPaneX, topPaneY + 140, Math.min(180, leftPaneWidth), 20)
                .build());

        int uvFieldWidth = Math.min(90, Math.max(70, (leftPaneWidth - 12) / 2));
        uvX = new EditBox(this.font, leftPaneX, topPaneY + 178, uvFieldWidth, 20, Component.translatable("screen.changedextras.artist_brush.uv_x"));
        uvX.setValue(Integer.toString(brushData.getInt(ArtistBrushItem.UV_X_TAG)));
        addRenderableWidget(uvX);

        uvY = new EditBox(this.font, leftPaneX + uvFieldWidth + 12, topPaneY + 178, uvFieldWidth, 20, Component.translatable("screen.changedextras.artist_brush.uv_y"));
        uvY.setValue(Integer.toString(brushData.getInt(ArtistBrushItem.UV_Y_TAG)));
        addRenderableWidget(uvY);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(leftPaneX, topPaneY + 220, Math.max(90, (leftPaneWidth - 8) / 2), 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(leftPaneX + Math.max(90, (leftPaneWidth - 8) / 2) + 8, topPaneY + 220, Math.max(90, leftPaneWidth - Math.max(90, (leftPaneWidth - 8) / 2) - 8), 20)
                .build());

        updateUvControls();
        setInitialFocus(texturePath);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        PreviewTextureData preview = resolvePreviewTexture();
        String selectedName = trimToWidth(ArtistBrushItem.getOrCreateBrushData(stack).getString(ArtistBrushItem.SELECTED_TARGET_NAME_TAG), leftPaneWidth);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, topPaneY, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.texture"), leftPaneX, topPaneY + 8, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.hex"), leftPaneX, topPaneY + 66, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.target", ArtistBrushItem.TARGET_FORM_ID), leftPaneX, topPaneY + 106, 0xA6E3A1, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.selected", selectedName), leftPaneX, topPaneY + 120, 0x8DB9FF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.uv_x"), leftPaneX, topPaneY + 166, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.uv_y"), uvY.getX(), topPaneY + 166, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.preview"), previewX, topPaneY + 8, 0xCFCFCF, false);

        renderTexturePreview(graphics, previewX, previewY, preview);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && customUvEnabled && isInsidePreview(mouseX, mouseY)) {
            draggingUv = true;
            updateUvFromMouse(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingUv && customUvEnabled) {
            updateUvFromMouse(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingUv = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        super.removed();
        disposeCachedPreview();
    }

    private void saveAndClose() {
        ChangedExtrasNetwork.INSTANCE.sendToServer(new SaveArtistBrushPacket(
                hand,
                texturePath.getValue().trim(),
                normalizeHex(hexColor.getValue().trim()),
                getClampedUv(uvX),
                getClampedUv(uvY),
                customUvEnabled
        ));
        onClose();
    }

    private void renderTexturePreview(GuiGraphics graphics, int previewX, int previewY, PreviewTextureData preview) {
        ImageBounds bounds = getImageBounds(preview);
        int drawWidth = bounds.drawWidth();
        int drawHeight = bounds.drawHeight();
        int imageX = bounds.x();
        int imageY = bounds.y();

        graphics.fill(previewX - 3, previewY - 3, previewX + PREVIEW_MAX_WIDTH + 3, previewY + PREVIEW_MAX_HEIGHT + 3, 0xFF232323);
        graphics.fill(previewX, previewY, previewX + PREVIEW_MAX_WIDTH, previewY + PREVIEW_MAX_HEIGHT, 0xFF121212);

        for (int y = 0; y < drawHeight; y += 16) {
            for (int x = 0; x < drawWidth; x += 16) {
                int color = ((x + y) / 16) % 2 == 0 ? 0xFF3A3A3A : 0xFF505050;
                graphics.fill(imageX + x, imageY + y, imageX + Math.min(drawWidth, x + 16), imageY + Math.min(drawHeight, y + 16), color);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(imageX, imageY, 0.0F);
        graphics.pose().scale(drawWidth / (float)Math.max(1, preview.imageWidth()), drawHeight / (float)Math.max(1, preview.imageHeight()), 1.0F);
        graphics.blit(preview.texture(), 0, 0, 0, 0, preview.imageWidth(), preview.imageHeight(), preview.imageWidth(), preview.imageHeight());
        graphics.pose().popPose();

        int borderColor = customUvEnabled ? 0xFF00D1FF : 0xFF7F7F7F;
        drawRegionOutline(graphics, imageX, imageY, imageX + drawWidth, imageY + drawHeight, 0xFF5A5A5A);

        for (UvRegion region : UV_REGIONS) {
            int x1 = imageX + scaleUvX(region.u(), preview);
            int y1 = imageY + scaleUvY(region.v(), preview);
            int x2 = imageX + scaleUvX(region.u() + region.width(), preview);
            int y2 = imageY + scaleUvY(region.v() + region.height(), preview);
            drawRegionOutline(graphics, x1, y1, x2, y2, region.color());
        }

        int handleCenterX = imageX + scaleUvX(getClampedUv(uvX), preview);
        int handleCenterY = imageY + scaleUvY(getClampedUv(uvY), preview);
        graphics.fill(handleCenterX - UV_HANDLE_SIZE, handleCenterY, handleCenterX + UV_HANDLE_SIZE + 1, handleCenterY + 1, borderColor);
        graphics.fill(handleCenterX, handleCenterY - UV_HANDLE_SIZE, handleCenterX + 1, handleCenterY + UV_HANDLE_SIZE + 1, borderColor);
        graphics.fill(handleCenterX - 2, handleCenterY - 2, handleCenterX + 3, handleCenterY + 3, 0xFFFFFFFF);

        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.preview_hint"), previewX, previewY + PREVIEW_MAX_HEIGHT + 8, 0x8DD3FF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.preview_target_hint"), previewX, previewY + PREVIEW_MAX_HEIGHT + 20, 0x9FD7FF, false);
        graphics.drawString(this.font, Component.translatable("screen.changedextras.artist_brush.preview_size", preview.imageWidth(), preview.imageHeight()), previewX, previewY + PREVIEW_MAX_HEIGHT + 32, 0xD7D7D7, false);
    }

    private PreviewTextureData resolvePreviewTexture() {
        String path = texturePath.getValue().trim();
        if (path.isEmpty()) {
            return resolveResourceTexture(CustomLatexRenderer.DEFAULT_SKIN_LOCATION);
        }
        if (path.equals(cachedPreviewPath) && cachedPreviewData != null) {
            return cachedPreviewData;
        }

        disposeCachedPreview();

        Path filePath = Path.of(path);
        cachedPreviewData = Files.exists(filePath) ? resolveExternalTexture(filePath) : resolveResourceTexture(ResourceLocation.tryParse(path));
        cachedPreviewPath = path;
        return cachedPreviewData;
    }

    private PreviewTextureData resolveExternalTexture(Path filePath) {
        try (var stream = Files.newInputStream(filePath)) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            String normalized = filePath.toAbsolutePath().normalize().toString();
            ResourceLocation texture = this.minecraft.getTextureManager().register("artist_brush_preview/" + Math.abs(normalized.hashCode()), dynamicTexture);
            return PreviewTextureData.of(texture, image.getWidth(), image.getHeight(), dynamicTexture);
        } catch (IOException ignored) {
            return PreviewTextureData.missing();
        }
    }

    private PreviewTextureData resolveResourceTexture(ResourceLocation location) {
        if (location == null) {
            return PreviewTextureData.missing();
        }
        try (var stream = this.minecraft.getResourceManager().open(location)) {
            NativeImage image = NativeImage.read(stream);
            return PreviewTextureData.of(location, image.getWidth(), image.getHeight(), null);
        } catch (IOException ignored) {
            return PreviewTextureData.missing();
        }
    }

    private void updateUvControls() {
        uvX.setEditable(customUvEnabled);
        uvY.setEditable(customUvEnabled);
        uvX.setTextColor(customUvEnabled ? 0xFFFFFF : 0x777777);
        uvY.setTextColor(customUvEnabled ? 0xFFFFFF : 0x777777);
    }

    private Component toggleText() {
        return Component.literal((customUvEnabled ? "[x] " : "[ ] "))
                .append(Component.translatable("screen.changedextras.artist_brush.custom_uv"));
    }

    private boolean isInsidePreview(double mouseX, double mouseY) {
        PreviewTextureData preview = resolvePreviewTexture();
        ImageBounds bounds = getImageBounds(preview);
        return mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.drawWidth()
                && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.drawHeight();
    }

    private void updateUvFromMouse(double mouseX, double mouseY) {
        PreviewTextureData preview = resolvePreviewTexture();
        ImageBounds bounds = getImageBounds(preview);
        double clampedX = Math.max(bounds.x(), Math.min(bounds.x() + bounds.drawWidth(), mouseX));
        double clampedY = Math.max(bounds.y(), Math.min(bounds.y() + bounds.drawHeight(), mouseY));
        int atlasX = Math.round((float)((clampedX - bounds.x()) / Math.max(1.0D, bounds.drawWidth())) * CUSTOM_LATEX_ATLAS_SIZE);
        int atlasY = Math.round((float)((clampedY - bounds.y()) / Math.max(1.0D, bounds.drawHeight())) * CUSTOM_LATEX_ATLAS_SIZE);
        uvX.setValue(Integer.toString(clampAtlas(atlasX)));
        uvY.setValue(Integer.toString(clampAtlas(atlasY)));
    }

    private void chooseTextureFile() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.png"));
            filters.flip();
            String chosen = TinyFileDialogs.tinyfd_openFileDialog(
                    Component.translatable("screen.changedextras.artist_brush.browse").getString(),
                    texturePath.getValue().isBlank() ? null : texturePath.getValue(),
                    filters,
                    "PNG Images",
                    false
            );
            if (chosen != null && !chosen.isBlank()) {
                texturePath.setValue(chosen);
                cachedPreviewPath = "";
            }
        } catch (Throwable ignored) {
            showBrowseUnavailableMessage();
        }
    }

    private void showBrowseUnavailableMessage() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(Component.translatable("message.changedextras.artist_brush.browse_unavailable"), true);
        }
    }

    private int scaleUvX(int coordinate, PreviewTextureData preview) {
        return Math.round((coordinate / (float)CUSTOM_LATEX_ATLAS_SIZE) * preview.drawWidth());
    }

    private int scaleUvY(int coordinate, PreviewTextureData preview) {
        return Math.round((coordinate / (float)CUSTOM_LATEX_ATLAS_SIZE) * preview.drawHeight());
    }

    private static int clampAtlas(int value) {
        return Math.max(0, Math.min(CUSTOM_LATEX_ATLAS_SIZE, value));
    }

    private static int getClampedUv(EditBox box) {
        return clampAtlas(parseInt(box.getValue()));
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String normalizeHex(String value) {
        if (value.isEmpty()) {
            return "#FFFFFF";
        }
        return value.startsWith("#") ? value.toUpperCase() : ("#" + value.toUpperCase());
    }

    private void disposeCachedPreview() {
        if (cachedPreviewData != null && cachedPreviewData.dynamicTexture() != null) {
            cachedPreviewData.dynamicTexture().close();
        }
        cachedPreviewData = null;
        cachedPreviewPath = "";
    }

    private void recalculateLayout() {
        leftPaneWidth = Math.min(250, Math.max(190, this.width / 2 - 24));
        leftPaneX = Math.max(12, this.width / 2 - leftPaneWidth - (PREVIEW_MAX_WIDTH / 2) - 16);
        previewX = Math.min(this.width - PREVIEW_MAX_WIDTH - 12, leftPaneX + leftPaneWidth + 16);
        topPaneY = Math.max(12, (this.height - 280) / 2);
        previewY = topPaneY + 20;
    }

    private void drawRegionOutline(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }

    private String trimToWidth(String value, int maxWidth) {
        return this.font.plainSubstrByWidth(value, Math.max(40, maxWidth - 10));
    }

    private record UvRegion(String name, int u, int v, int width, int height, int color) {
    }

    private record ImageBounds(int x, int y, int drawWidth, int drawHeight) {
    }

    private record PreviewTextureData(ResourceLocation texture, int imageWidth, int imageHeight, int drawWidth, int drawHeight, DynamicTexture dynamicTexture) {
        static PreviewTextureData of(ResourceLocation texture, int imageWidth, int imageHeight, DynamicTexture dynamicTexture) {
            float scale = Math.min(PREVIEW_MAX_WIDTH / (float)Math.max(1, imageWidth), PREVIEW_MAX_HEIGHT / (float)Math.max(1, imageHeight));
            int drawWidth = Math.max(1, Math.round(imageWidth * scale));
            int drawHeight = Math.max(1, Math.round(imageHeight * scale));
            return new PreviewTextureData(texture, imageWidth, imageHeight, drawWidth, drawHeight, dynamicTexture);
        }

        static PreviewTextureData missing() {
            return of(MissingTextureAtlasSprite.getLocation(), 16, 16, null);
        }
    }

    private ImageBounds getImageBounds(PreviewTextureData preview) {
        int drawWidth = preview.drawWidth();
        int drawHeight = preview.drawHeight();
        int imageX = previewX + (PREVIEW_MAX_WIDTH - drawWidth) / 2;
        int imageY = previewY + (PREVIEW_MAX_HEIGHT - drawHeight) / 2;
        return new ImageBounds(imageX, imageY, drawWidth, drawHeight);
    }
}
