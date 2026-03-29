package com.katt.changedextras.common.ai;

import com.katt.changedextras.ChangedExtras;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = ChangedExtras.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LatexMobAIHandler {
    private static final String INSTALLED_TAG = "changedextras_smart_ai_installed";

    private LatexMobAIHandler() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ChangedEntity mob)) return;

        if (mob.getPersistentData().getBoolean(INSTALLED_TAG)) return;
        mob.getPersistentData().putBoolean(INSTALLED_TAG, true);

        removeConflictingLookGoals(mob);
        installTargetGoal(mob);
        mob.setCanPickUpLoot(true);

        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && followRange.getBaseValue() < 40.0D) {
            followRange.setBaseValue(40.0D);
        }

        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.24D);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ChangedEntity mob)) return;
        if (mob.level().isClientSide()) return;

        LatexMind mind = LatexMindStore.get(mob);
        mind.tick(mob);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof ChangedEntity)) return;

        event.addCapability(
                ResourceLocation.fromNamespaceAndPath(ChangedExtras.MODID, "latex_inventory"),
                new com.katt.changedextras.common.inventory.LatexInventoryProvider()
        );
    }

    private static void installTargetGoal(ChangedEntity mob) {
        GoalSelector targets = getSelector(mob, "targetSelector");
        if (targets != null) {
            targets.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, true));
        }
    }

    private static void removeConflictingLookGoals(ChangedEntity mob) {
        GoalSelector goals = getSelector(mob, "goalSelector");
        if (goals != null) {
            goals.removeAllGoals(goal ->
                    goal instanceof RandomLookAroundGoal
                            || goal instanceof LookAtPlayerGoal
                            || goal instanceof WaterAvoidingRandomStrollGoal
            );
        }
    }

    @Nullable
    private static GoalSelector getSelector(ChangedEntity mob, String fieldName) {
        try {
            Field field = net.minecraft.world.entity.Mob.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (GoalSelector) field.get(mob);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
