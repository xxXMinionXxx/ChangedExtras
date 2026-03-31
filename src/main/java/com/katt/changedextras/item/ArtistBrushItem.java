package com.katt.changedextras.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtistBrushItem extends Item {
    public static final String BRUSH_DATA_TAG = "ArtistBrushData";
    public static final String TEXTURE_PATH_TAG = "TexturePath";
    public static final String HEX_COLOR_TAG = "HexColor";
    public static final String UV_X_TAG = "UvX";
    public static final String UV_Y_TAG = "UvY";
    public static final String CUSTOM_UV_ENABLED_TAG = "CustomUvEnabled";
    public static final String TARGET_FORM_TAG = "TargetForm";
    public static final String TARGET_FORM_ID = "changed:custom_latex";
    public static final String SELECTED_TARGET_NAME_TAG = "SelectedTargetName";
    public static final String SELECTED_TARGET_UUID_TAG = "SelectedTargetUuid";
    public static final String SELECTED_TARGET_TYPE_TAG = "SelectedTargetType";

    public ArtistBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openEditor(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag brushData = stack.getTagElement(BRUSH_DATA_TAG);
        tooltip.add(Component.translatable("item.changedextras.artist_brush.tooltip").withStyle(ChatFormatting.GRAY));
        if (brushData == null) {
            tooltip.add(Component.translatable("item.changedextras.artist_brush.tooltip.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("item.changedextras.artist_brush.texture", brushData.getString(TEXTURE_PATH_TAG)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.changedextras.artist_brush.hex", brushData.getString(HEX_COLOR_TAG)).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.changedextras.artist_brush.target", brushData.getString(TARGET_FORM_TAG)).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.changedextras.artist_brush.selected", brushData.getString(SELECTED_TARGET_NAME_TAG)).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.changedextras.artist_brush.uv_toggle", brushData.getBoolean(CUSTOM_UV_ENABLED_TAG) ? "On" : "Off").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.changedextras.artist_brush.uv", brushData.getInt(UV_X_TAG), brushData.getInt(UV_Y_TAG)).withStyle(ChatFormatting.YELLOW));
    }

    public static CompoundTag getOrCreateBrushData(ItemStack stack) {
        CompoundTag brushData = stack.getTagElement(BRUSH_DATA_TAG);
        if (brushData == null) {
            brushData = new CompoundTag();
            brushData.putString(TEXTURE_PATH_TAG, "");
            brushData.putString(HEX_COLOR_TAG, "#FFFFFF");
            brushData.putInt(UV_X_TAG, 0);
            brushData.putInt(UV_Y_TAG, 0);
            brushData.putBoolean(CUSTOM_UV_ENABLED_TAG, false);
            brushData.putString(TARGET_FORM_TAG, TARGET_FORM_ID);
            brushData.putString(SELECTED_TARGET_NAME_TAG, "None");
            brushData.putString(SELECTED_TARGET_UUID_TAG, "");
            brushData.putString(SELECTED_TARGET_TYPE_TAG, "none");
            stack.getOrCreateTag().put(BRUSH_DATA_TAG, brushData);
        }
        return brushData;
    }

    public static void setSelectedTarget(ItemStack stack, String name, String uuid, String type) {
        CompoundTag brushData = getOrCreateBrushData(stack);
        brushData.putString(SELECTED_TARGET_NAME_TAG, name);
        brushData.putString(SELECTED_TARGET_UUID_TAG, uuid);
        brushData.putString(SELECTED_TARGET_TYPE_TAG, type);
    }

    private static void openEditor(InteractionHand hand) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ItemStack stack = minecraft.player.getItemInHand(hand);
        minecraft.setScreen(new com.katt.changedextras.client.ArtistBrushScreen(hand, stack));
    }
}
