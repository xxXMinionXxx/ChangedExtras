package com.katt.changedextras.events;

import com.katt.changedextras.init.ChangedExtrasAbilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changedextras", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChangedExtrasEvents {

    private static final String NBT_TAG = "JackpotActive";

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();

        // Server side only
        if (living.level().isClientSide()) return;

        // Check NBT flag directly on the entity
        if (!living.getPersistentData().getBoolean(NBT_TAG)) return;

        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 9, false, false));
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 2, false, false));
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false));

        if (living instanceof Player player) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0f);
        }
    }
}