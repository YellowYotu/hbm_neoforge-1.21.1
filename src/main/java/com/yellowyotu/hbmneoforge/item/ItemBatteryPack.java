package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public final class ItemBatteryPack extends Item {
    public static final int CAPACITY = 1_800_000;
    public static final int CHARGE_RATE = 1_000;
    public static final int DISCHARGE_RATE = 100;
    private static final String ENERGY_TAG = "Energy";

    public ItemBatteryPack(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int getEnergy(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return Math.clamp(data.copyTag().getInt(ENERGY_TAG), 0, CAPACITY);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(ENERGY_TAG, Math.clamp(energy, 0, CAPACITY));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int insertEnergy(ItemStack stack, int amount) {
        int accepted = Math.min(amount, Math.min(CHARGE_RATE, CAPACITY - getEnergy(stack)));
        setEnergy(stack, getEnergy(stack) + accepted);
        return accepted;
    }

    public static int extractEnergy(ItemStack stack, int amount) {
        int extracted = Math.min(amount, Math.min(DISCHARGE_RATE, getEnergy(stack)));
        setEnergy(stack, getEnergy(stack) - extracted);
        return extracted;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergy(stack) > 0 && getEnergy(stack) < CAPACITY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergy(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x202020;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int energy = getEnergy(stack);
        double percent = energy * 100.0D / CAPACITY;
        tooltip.add(Component.literal("Energy stored: " + energy + "/" + CAPACITY + "HE (" + String.format("%.1f", percent) + "%)").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Charge rate: " + CHARGE_RATE + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Discharge rate: " + DISCHARGE_RATE + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Time for full charge: 1.5min").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Charge lasts for: 15.0min").withStyle(ChatFormatting.GOLD));
    }
}
