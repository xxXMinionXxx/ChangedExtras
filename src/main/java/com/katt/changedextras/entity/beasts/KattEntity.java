package com.katt.changedextras.entity.beasts;

import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class KattEntity extends AbstractWhiteCatEntity {
    private static final EntityDataAccessor<Boolean> JACKPOT_STATE =
            SynchedEntityData.defineId(KattEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(JACKPOT_STATE, false);
    }

    public void setJackpot(boolean active) {
        this.entityData.set(JACKPOT_STATE, active);
    }

    public boolean isJackpot() {
        return this.entityData.get(JACKPOT_STATE);
    }
    public KattEntity(EntityType<? extends KattEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ChangedEntity.createLatexAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }
}
