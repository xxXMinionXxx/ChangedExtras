package com.katt.changedextras.ability;

import net.ltxprogrammer.changed.ability.AbstractAbility;
import net.ltxprogrammer.changed.ability.IAbstractChangedEntity;
import net.ltxprogrammer.changed.ability.SimpleAbility;
import net.ltxprogrammer.changed.entity.variant.TransfurVariantInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

public class ClawsAbility extends SimpleAbility {
    public static final String ACTIVE_TAG = "changedextras.claws_active";

    @Override
    public Component getAbilityName(IAbstractChangedEntity entity) {
        return Component.translatable("ability.changedextras.claws.name");
    }

    @Override
    public Component getSelectedDisplayText(IAbstractChangedEntity entity) {
        return Component.translatable(
                isActive(entity) ? "ability.changedextras.claws.selected_on" : "ability.changedextras.claws.selected_off"
        );
    }

    @Override
    public List<Component> getAbilityDescription(IAbstractChangedEntity entity) {
        return List.of(Component.translatable("ability.changedextras.claws.description"));
    }

    @Override
    public AbstractAbility.UseType getUseType(IAbstractChangedEntity entity) {
        return AbstractAbility.UseType.INSTANT;
    }

    @Override
    public int getCoolDown(IAbstractChangedEntity entity) {
        return 10;
    }

    @Override
    public boolean canUse(IAbstractChangedEntity entity) {
        return entity.getTransfurVariantInstance() != null;
    }

    @Override
    public void startUsing(IAbstractChangedEntity entity) {
        if (entity.getLevel().isClientSide()) {
            return;
        }

        boolean active = !isActive(entity);
        setActive(entity, active);
        entity.displayClientMessage(
                Component.translatable(active ? "ability.changedextras.claws.toggle_on" : "ability.changedextras.claws.toggle_off"),
                true
        );
        setDirty(entity);
    }

    public static boolean isActive(IAbstractChangedEntity entity) {
        if (!hasClawsAbility(entity)) {
            setActive(entity, false);
            return false;
        }

        return entity.getPersistentData().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(IAbstractChangedEntity entity, boolean active) {
        entity.getPersistentData().putBoolean(ACTIVE_TAG, active);
    }

    public static boolean isActive(LivingEntity entity) {
        return getEntityState(entity).map(ClawsAbility::isActive).orElse(false);
    }

    public static Optional<IAbstractChangedEntity> getEntityState(LivingEntity entity) {
        return IAbstractChangedEntity.forEitherSafe(entity);
    }

    public static boolean hasClawsAbility(IAbstractChangedEntity entity) {
        TransfurVariantInstance<?> variant = entity.getTransfurVariantInstance();
        return variant != null && variant.hasAbility(com.katt.changedextras.init.ChangedExtrasAbilities.CLAWS.get());
    }
}
