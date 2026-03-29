package com.katt.changedextras.common;

import com.katt.changedextras.ChangedExtras;
import net.minecraft.world.level.GameRules;

public final class ChangedExtrasGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> LATEX_EQUIPMENT_ENABLED = GameRules.register(
            "changedextrasLatexEquipmentEnabled",
            GameRules.Category.SPAWNING,
            GameRules.BooleanValue.create(true)
    );

    public static final GameRules.Key<GameRules.IntegerValue> LATEX_TOOL_CHANCE = GameRules.register(
            "changedextrasLatexToolChance",
            GameRules.Category.SPAWNING,
            GameRules.IntegerValue.create(15)
    );

    public static final GameRules.Key<GameRules.IntegerValue> LATEX_ARMOR_CHANCE = GameRules.register(
            "changedextrasLatexArmorChance",
            GameRules.Category.SPAWNING,
            GameRules.IntegerValue.create(30)
    );

    private ChangedExtrasGameRules() {
    }

    public static void bootstrap() {
        ChangedExtras.class.getName();
    }

    public static float getToolChance(GameRules gameRules) {
        return clampPercent(gameRules.getInt(LATEX_TOOL_CHANCE)) / 100.0F;
    }

    public static float getArmorChance(GameRules gameRules) {
        return clampPercent(gameRules.getInt(LATEX_ARMOR_CHANCE)) / 100.0F;
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
