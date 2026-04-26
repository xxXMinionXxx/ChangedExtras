package com.katt.changedextras.events;

import com.katt.changedextras.entity.beasts.KattEntity;
import com.katt.changedextras.network.JackpotStatePacket;
import com.katt.changedextras.init.ChangedExtrasAbilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changedextras", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChangedExtrasEvents {

    private static final String NBT_TAG = "JackpotActive";
    public static final String JACKPOT_TICKS_TAG = "JackpotTicksRemaining";
    private static final String JACKPOT_SLOWNESS_TICKS_TAG = "JackpotSlownessTicksRemaining";
    private static final String JACKPOT_NAUSEA_TICKS_TAG = "JackpotNauseaTicksRemaining";
    public static final int JACKPOT_DURATION_TICKS = (4 * 60 + 11) * 20;
    private static final int JACKPOT_SLOWNESS_DURATION_TICKS = 2 * 60 * 20;
    private static final int JACKPOT_NAUSEA_DURATION_TICKS = 60 * 20;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();

        // Server side only
        if (living.level().isClientSide()) return;

        tickJackpot(living);
        tickJackpotAftermath(living);
    }

    private static void tickJackpot(LivingEntity living) {
        if (!living.getPersistentData().getBoolean(NBT_TAG)) return;

        int ticksRemaining = living.getPersistentData().getInt(JACKPOT_TICKS_TAG);
        if (ticksRemaining <= 0) {
            endJackpot(living);
            return;
        }

        living.getPersistentData().putInt(JACKPOT_TICKS_TAG, ticksRemaining - 1);

        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 9, false, false));
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 2, false, false));
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false));

        if (living instanceof Player player) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0f);
        }
    }

    private static void endJackpot(LivingEntity living) {
        living.getPersistentData().putBoolean(NBT_TAG, false);
        living.getPersistentData().remove(JACKPOT_TICKS_TAG);
        living.getPersistentData().putInt(JACKPOT_SLOWNESS_TICKS_TAG, JACKPOT_SLOWNESS_DURATION_TICKS);
        living.getPersistentData().putInt(JACKPOT_NAUSEA_TICKS_TAG, JACKPOT_NAUSEA_DURATION_TICKS);

        if (living instanceof KattEntity katt) {
            katt.setJackpot(false);
        }

        if (living instanceof ServerPlayer player && living.level() instanceof ServerLevel level) {
            JackpotStatePacket.broadcast(level, player.getUUID(), false);
        }
    }

    private static void tickJackpotAftermath(LivingEntity living) {
        int slownessTicks = living.getPersistentData().getInt(JACKPOT_SLOWNESS_TICKS_TAG);
        if (slownessTicks > 0) {
            living.getPersistentData().putInt(JACKPOT_SLOWNESS_TICKS_TAG, slownessTicks - 1);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0, false, false));
        } else if (slownessTicks < 0) {
            living.getPersistentData().remove(JACKPOT_SLOWNESS_TICKS_TAG);
        }

        int nauseaTicks = living.getPersistentData().getInt(JACKPOT_NAUSEA_TICKS_TAG);
        if (nauseaTicks > 0) {
            living.getPersistentData().putInt(JACKPOT_NAUSEA_TICKS_TAG, nauseaTicks - 1);
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 1, false, false));
        } else if (nauseaTicks < 0) {
            living.getPersistentData().remove(JACKPOT_NAUSEA_TICKS_TAG);
        }
    }
}
