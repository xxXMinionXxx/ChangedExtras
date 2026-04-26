package com.katt.changedextras.common.ai;

import com.katt.changedextras.entity.beasts.ArtistEntity;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.ltxprogrammer.changed.entity.variant.TransfurVariantInstance;
import net.ltxprogrammer.changed.process.ProcessTransfur;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Set;

public final class LatexAiUtil {
    private static final Set<String> SMART_AI_EXCLUDED_FORMS = Set.of(
            "changedextras:artist",
            "changed_addon:form_experiment_009",
            "changed_addon:form_experiment_10"
    );

    private LatexAiUtil() {
    }

    public static boolean isSmartAiExcluded(ChangedEntity mob) {
        if (mob instanceof ArtistEntity) {
            return true;
        }

        String identity = getEntityIdentity(mob);
        return SMART_AI_EXCLUDED_FORMS.contains(identity)
                || identity.contains("experiment_009")
                || identity.contains("experiment_10");
    }

    public static boolean isPlayerTransfurred(Player player) {
        return ProcessTransfur.isPlayerTransfurred(player) || ProcessTransfur.getPlayerTransfurVariant(player) != null;
    }

    public static boolean areHostileLatexFactions(ChangedEntity mob, Player player) {
        LatexAlignment mobAlignment = classifyMobAlignment(mob);
        LatexAlignment playerAlignment = classifyPlayerAlignment(player);
        return mobAlignment != LatexAlignment.OTHER
                && playerAlignment != LatexAlignment.OTHER
                && mobAlignment != playerAlignment;
    }

    @Nullable
    public static String getEntityFormId(ChangedEntity entity) {
        TransfurVariant<?> variant = TransfurVariant.findEntityTransfurVariant(entity);
        if (variant == null || variant.getFormId() == null) {
            return null;
        }

        return variant.getFormId().toString();
    }

    @Nullable
    public static String getPlayerFormId(Player player) {
        TransfurVariantInstance<?> variant = ProcessTransfur.getPlayerTransfurVariant(player);
        if (variant == null || variant.getFormId() == null) {
            return null;
        }

        return variant.getFormId().toString();
    }

    private static String getEntityIdentity(ChangedEntity entity) {
        String formId = getEntityFormId(entity);
        if (formId != null) {
            return formId;
        }

        ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityTypeId != null ? entityTypeId.toString() : "";
    }

    private static LatexAlignment classifyMobAlignment(ChangedEntity mob) {
        return classifyAlignment(getEntityIdentity(mob));
    }

    private static LatexAlignment classifyPlayerAlignment(Player player) {
        return classifyAlignment(getPlayerFormId(player));
    }

    private static LatexAlignment classifyAlignment(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return LatexAlignment.OTHER;
        }

        String normalized = id.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("white")) {
            return LatexAlignment.WHITE;
        }
        if (normalized.contains("dark")) {
            return LatexAlignment.DARK;
        }

        return LatexAlignment.OTHER;
    }

    private enum LatexAlignment {
        WHITE,
        DARK,
        OTHER
    }
}
