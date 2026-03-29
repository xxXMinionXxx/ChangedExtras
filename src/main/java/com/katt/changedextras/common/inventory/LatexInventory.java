package com.katt.changedextras.common.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class LatexInventory extends ItemStackHandler {

    public LatexInventory() {
        super(12); // 12 slots, change if you want
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
    }

    public ItemStack addItem(ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            stack = insertItem(i, stack, false);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }
}