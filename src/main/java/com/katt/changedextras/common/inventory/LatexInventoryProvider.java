package com.katt.changedextras.common.inventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LatexInventoryProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final LatexInventory inventory = new LatexInventory();
    private final LazyOptional<LatexInventory> optional = LazyOptional.of(() -> inventory);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}