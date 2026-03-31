package com.katt.changedextras.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.ltxprogrammer.changed.client.renderer.CustomLatexRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ArtistTintManager {
    private static final Map<Integer, AppearanceData> ENTITY_APPEARANCES = new ConcurrentHashMap<>();
    private static final Map<String, CachedTexture> TEXTURE_CACHE = new ConcurrentHashMap<>();

    private ArtistTintManager() {
    }

    public static void setAppearance(int entityId, int color, String texturePath, int uvX, int uvY, boolean customUvEnabled) {
        ENTITY_APPEARANCES.put(entityId, new AppearanceData(color, texturePath == null ? "" : texturePath, uvX, uvY, customUvEnabled));
    }

    public static void clearTint(int entityId) {
        ENTITY_APPEARANCES.remove(entityId);
    }

    public static Integer getTint(int entityId) {
        AppearanceData data = ENTITY_APPEARANCES.get(entityId);
        if (data == null || !data.hasStandaloneTint()) {
            return null;
        }
        return data.color();
    }

    public static ResourceLocation getTexture(int entityId) {
        AppearanceData data = ENTITY_APPEARANCES.get(entityId);
        if (data == null) {
            return null;
        }

        String path = data.texturePath().trim();
        if (path.isBlank()) {
            return loadResourceTexture(CustomLatexRenderer.DEFAULT_SKIN_LOCATION, data.color());
        }

        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            return loadExternalTexture(filePath, data.color());
        }

        ResourceLocation resourceLocation = ResourceLocation.tryParse(path);
        return resourceLocation != null ? loadResourceTexture(resourceLocation, data.color()) : MissingTextureAtlasSprite.getLocation();
    }

    public static boolean hasCustomUv(int entityId) {
        AppearanceData data = ENTITY_APPEARANCES.get(entityId);
        return data != null && data.customUvEnabled();
    }

    public static float getUvOffsetX(int entityId) {
        AppearanceData data = ENTITY_APPEARANCES.get(entityId);
        return data == null ? 0.0F : data.uvX() / 160.0F;
    }

    public static float getUvOffsetY(int entityId) {
        AppearanceData data = ENTITY_APPEARANCES.get(entityId);
        return data == null ? 0.0F : data.uvY() / 160.0F;
    }

    public static void clearAll() {
        ENTITY_APPEARANCES.clear();
        TEXTURE_CACHE.values().forEach(CachedTexture::close);
        TEXTURE_CACHE.clear();
    }

    private static ResourceLocation loadExternalTexture(Path filePath, int color) {
        String normalized = filePath.toAbsolutePath().normalize().toString();
        String cacheKey = normalized + "#" + color;
        CachedTexture cached = TEXTURE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.location();
        }

        try (var stream = Files.newInputStream(filePath)) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(tintImage(image, color));
            ResourceLocation location = Minecraft.getInstance().getTextureManager()
                    .register("artist_brush_target/" + Math.abs(cacheKey.hashCode()), dynamicTexture);
            CachedTexture created = new CachedTexture(location, dynamicTexture);
            TEXTURE_CACHE.put(cacheKey, created);
            return location;
        } catch (IOException ignored) {
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    private static ResourceLocation loadResourceTexture(ResourceLocation location, int color) {
        if (color == 0xFFFFFF) {
            return location;
        }

        String cacheKey = location + "#" + color;
        CachedTexture cached = TEXTURE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.location();
        }

        try (var stream = Minecraft.getInstance().getResourceManager().open(location)) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(tintImage(image, color));
            ResourceLocation dynamicLocation = Minecraft.getInstance().getTextureManager()
                    .register("artist_brush_target/" + Math.abs(cacheKey.hashCode()), dynamicTexture);
            CachedTexture created = new CachedTexture(dynamicLocation, dynamicTexture);
            TEXTURE_CACHE.put(cacheKey, created);
            return dynamicLocation;
        } catch (IOException ignored) {
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    private static NativeImage tintImage(NativeImage source, int color) {
        int width = source.getWidth();
        int height = source.getHeight();
        NativeImage tinted = new NativeImage(width, height, true);
        float redMultiplier = ((color >> 16) & 0xFF) / 255.0F;
        float greenMultiplier = ((color >> 8) & 0xFF) / 255.0F;
        float blueMultiplier = (color & 0xFF) / 255.0F;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = source.getPixelRGBA(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                int blue = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int red = rgba & 0xFF;
                int tintedRed = Math.min(255, Math.round(red * redMultiplier));
                int tintedGreen = Math.min(255, Math.round(green * greenMultiplier));
                int tintedBlue = Math.min(255, Math.round(blue * blueMultiplier));
                int tintedRgba = (alpha << 24) | (tintedBlue << 16) | (tintedGreen << 8) | tintedRed;
                tinted.setPixelRGBA(x, y, tintedRgba);
            }
        }

        source.close();
        return tinted;
    }

    private record AppearanceData(int color, String texturePath, int uvX, int uvY, boolean customUvEnabled) {
        boolean hasStandaloneTint() {
            return texturePath.isBlank();
        }
    }

    private record CachedTexture(ResourceLocation location, DynamicTexture texture) {
        void close() {
            texture.close();
        }
    }
}
