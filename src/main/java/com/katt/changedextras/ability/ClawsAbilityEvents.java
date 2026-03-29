package com.katt.changedextras.ability;

import com.katt.changedextras.ChangedExtras;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChangedExtras.MODID)
public final class ClawsAbilityEvents {
    private static final float BONUS_DAMAGE = 3.0F;
    private static final double BONUS_KNOCKBACK = 0.75D;

    private ClawsAbilityEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (!player.getMainHandItem().isEmpty()) {
            return;
        }

        if (!ClawsAbility.isActive(player)) {
            return;
        }

        LivingEntity target = event.getEntity();
        event.setAmount(event.getAmount() + BONUS_DAMAGE);

        Vec3 look = player.getLookAngle().normalize();
        target.push(look.x * BONUS_KNOCKBACK, 0.15D, look.z * BONUS_KNOCKBACK);
        target.hurtMarked = true;

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 origin = player.getEyePosition().add(look.scale(0.9D));
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    origin.x,
                    origin.y - 0.25D,
                    origin.z,
                    8,
                    0.2D,
                    0.15D,
                    0.2D,
                    0.01D
            );
        }
    }
}
