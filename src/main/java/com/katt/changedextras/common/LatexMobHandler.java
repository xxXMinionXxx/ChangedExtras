package com.katt.changedextras.common;

import com.katt.changedextras.entity.ModEntities;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.katt.changedextras.ChangedExtras.MODID)
public class LatexMobHandler {
    private static final Item[] TOOL_POOL = {
            Items.STONE_SWORD,
            Items.STONE_PICKAXE,
            Items.STONE_AXE,
            Items.STONE_SHOVEL,
            Items.STONE_HOE,
            Items.IRON_SWORD,
            Items.IRON_PICKAXE,
            Items.IRON_AXE,
            Items.IRON_SHOVEL,
            Items.IRON_HOE,
            Items.GOLDEN_SWORD,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_HOE,
            Items.DIAMOND_SWORD,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_AXE,
            Items.DIAMOND_SHOVEL,
            Items.DIAMOND_HOE
    };

    private static final Item[][] ARMOR_POOLS = {
            {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS},
            {Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS},
            {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS}
    };

    private LatexMobHandler() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        if (!isHandledLatexCreature(mob)) {
            return;
        }

        if (event.getSpawnType() == MobSpawnType.CONVERSION) {
            return;
        }

        if (!mob.level().getGameRules().getBoolean(ChangedExtrasGameRules.LATEX_EQUIPMENT_ENABLED)) {
            return;
        }

        RandomSource random = mob.getRandom();
        float toolChance = ChangedExtrasGameRules.getToolChance(mob.level().getGameRules());
        if (random.nextFloat() < toolChance) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(getRandomTool(random)));
        }

        float armorChance = ChangedExtrasGameRules.getArmorChance(mob.level().getGameRules());
        if (random.nextFloat() < armorChance) {
            equipRandomArmorSet(mob, random);
        }
    }

    private static boolean isHandledLatexCreature(Mob mob) {
        return mob.getType() == ModEntities.CONEKAT_MALE.get()
                || mob.getType() == ModEntities.CONEKAT_FEMALE.get()
                || mob.getType() == ModEntities.WHITE_CAT.get();
    }

    private static Item getRandomTool(RandomSource random) {
        return TOOL_POOL[random.nextInt(TOOL_POOL.length)];
    }

    private static void equipRandomArmorSet(Mob mob, RandomSource random) {
        Item[] armorSet = ARMOR_POOLS[random.nextInt(ARMOR_POOLS.length)];
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(armorSet[0]));
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(armorSet[1]));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(armorSet[2]));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(armorSet[3]));
    }
}
