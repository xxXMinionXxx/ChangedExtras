package com.katt.changedextras.init;

import com.katt.changedextras.network.ChangedExtrasNetwork;
import com.katt.changedextras.network.JackpotStatePacket;
import com.katt.changedextras.ability.ClawsAbility;
import com.katt.changedextras.ability.PaintBallAbility;
import com.katt.changedextras.ability.PunctureAbility;
import com.katt.changedextras.ability.SwingAbility;
import com.katt.changedextras.entity.beasts.KattEntity;
import com.katt.changedextras.events.ChangedExtrasEvents;
import net.ltxprogrammer.changed.ability.AbstractAbility;
import net.ltxprogrammer.changed.ability.AbstractAbilityInstance;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.init.ChangedRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

public final class ChangedExtrasAbilities {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ChangedExtrasAbilities.class);

    public static final DeferredRegister<AbstractAbility<?>> REGISTRY =
            ChangedRegistry.ABILITY.createDeferred("changedextras");

    public static final RegistryObject<ClawsAbility> CLAWS =
            REGISTRY.register("claws", ClawsAbility::new);

    public static final RegistryObject<PaintBallAbility> PAINT_BALL =
            REGISTRY.register("paint_ball", PaintBallAbility::new);

    public static final RegistryObject<SwingAbility> SWING =
            REGISTRY.register("swing", SwingAbility::new);

    public static final RegistryObject<PunctureAbility> PUNCTURE =
            REGISTRY.register("puncture", PunctureAbility::new);

    public static final RegistryObject<AbstractAbility<JackpotAbilityInstance>> JACKPOT_AURA = REGISTRY.register("jackpot_aura",
            () -> new AbstractAbility<JackpotAbilityInstance>(JackpotAbilityInstance::new) {
                @Override
                public boolean canUse(IAbstractChangedEntity entity) { return true; }

                @Override
                public UseType getUseType(IAbstractChangedEntity entity) { return UseType.INSTANT; }
            });

    public static class JackpotAbilityInstance extends AbstractAbilityInstance {
        private static final String NBT_TAG = "JackpotActive";

        public JackpotAbilityInstance(AbstractAbility<?> ability, IAbstractChangedEntity entity) {
            super(ability, entity);
        }

        @Override
        public void tick() {}

        @Override
        public void startUsing() {
            if (this.entity.getPersistentData().getBoolean(NBT_TAG)) {
                return;
            }

            this.entity.getPersistentData().putBoolean(NBT_TAG, true);
            this.entity.getPersistentData().putInt(ChangedExtrasEvents.JACKPOT_TICKS_TAG, ChangedExtrasEvents.JACKPOT_DURATION_TICKS);
            broadcastState(true);

            if (this.entity.getEntity() instanceof KattEntity katt) {
                katt.setJackpot(true);
            }
        }

        private void broadcastState(boolean active) {
            if (!(this.entity.getEntity() instanceof ServerPlayer serverPlayer)) return;
            ChangedExtrasNetwork.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new JackpotStatePacket(serverPlayer.getUUID(), active)
            );
        }

        @Override public boolean canUse() { return true; }
        @Override public boolean canKeepUsing() { return true; }
        @Override public void stopUsing() {}
    }

    private ChangedExtrasAbilities() {}
}
