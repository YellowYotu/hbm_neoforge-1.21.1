package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public final class ItemBatteryPack extends Item {
    public static final int REDSTONE_CAPACITY = 1_800_000;
    public static final int REDSTONE_CHARGE_RATE = 1_000;
    public static final int REDSTONE_DISCHARGE_RATE = 100;
    private static final String ENERGY_TAG = "Energy";

    public enum BatteryType {
        NORMAL,
        SELF_CHARGING,
        INFINITE
    }

    private final int capacity;
    private final int chargeRate;
    private final int dischargeRate;
    private final BatteryType batteryType;

    public ItemBatteryPack(Properties properties) {
        this(properties, REDSTONE_CAPACITY, REDSTONE_CHARGE_RATE, REDSTONE_DISCHARGE_RATE, BatteryType.NORMAL);
    }

    public ItemBatteryPack(Properties properties, int capacity, int chargeRate, int dischargeRate) {
        this(properties, capacity, chargeRate, dischargeRate, BatteryType.NORMAL);
    }

    public ItemBatteryPack(Properties properties, int power, BatteryType batteryType) {
        this(properties, batteryType == BatteryType.INFINITE ? Integer.MAX_VALUE : power, 0, batteryType == BatteryType.INFINITE ? Integer.MAX_VALUE : power, batteryType);
    }

    private ItemBatteryPack(Properties properties, int capacity, int chargeRate, int dischargeRate, BatteryType batteryType) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
        this.chargeRate = chargeRate;
        this.dischargeRate = dischargeRate;
        this.batteryType = batteryType;
    }

    public static boolean isBattery(ItemStack stack) {
        return stack.getItem() instanceof ItemBatteryPack;
    }

    public static boolean isSelfCharging(ItemStack stack) {
        return battery(stack) != null && battery(stack).batteryType == BatteryType.SELF_CHARGING;
    }

    public static boolean isInfinite(ItemStack stack) {
        return battery(stack) != null && battery(stack).batteryType == BatteryType.INFINITE;
    }

    public static boolean isNonDepleting(ItemStack stack) {
        return isSelfCharging(stack) || isInfinite(stack);
    }

    public static boolean canReceiveEnergy(ItemStack stack) {
        return battery(stack) != null && battery(stack).batteryType == BatteryType.NORMAL;
    }

    public static int getCapacity(ItemStack stack) {
        ItemBatteryPack battery = battery(stack);
        return battery == null ? 0 : battery.capacity;
    }

    public static int getChargeRate(ItemStack stack) {
        ItemBatteryPack battery = battery(stack);
        return battery == null ? 0 : battery.chargeRate;
    }

    public static int getDischargeRate(ItemStack stack) {
        ItemBatteryPack battery = battery(stack);
        return battery == null ? 0 : battery.dischargeRate;
    }

    public static int getEnergy(ItemStack stack) {
        ItemBatteryPack battery = battery(stack);
        if (battery == null) {
            return 0;
        }
        if (battery.batteryType != BatteryType.NORMAL) {
            return battery.capacity;
        }
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return Math.clamp(data.copyTag().getInt(ENERGY_TAG), 0, battery.capacity);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        ItemBatteryPack battery = battery(stack);
        if (battery == null || battery.batteryType != BatteryType.NORMAL) {
            return;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(ENERGY_TAG, Math.clamp(energy, 0, battery.capacity));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int insertEnergy(ItemStack stack, int amount) {
        ItemBatteryPack battery = battery(stack);
        if (battery == null || battery.batteryType != BatteryType.NORMAL || amount <= 0) {
            return 0;
        }
        int energy = getEnergy(stack);
        int accepted = Math.min(amount, Math.min(battery.chargeRate, battery.capacity - energy));
        setEnergy(stack, energy + accepted);
        return accepted;
    }

    public static int extractEnergy(ItemStack stack, int amount) {
        ItemBatteryPack battery = battery(stack);
        if (battery == null || amount <= 0) {
            return 0;
        }
        int extracted = Math.min(amount, battery.dischargeRate);
        if (battery.batteryType != BatteryType.NORMAL) {
            return extracted;
        }
        int energy = getEnergy(stack);
        extracted = Math.min(extracted, energy);
        setEnergy(stack, energy - extracted);
        return extracted;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return batteryType == BatteryType.NORMAL;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return capacity <= 0 ? 0 : Math.round(13.0F * getEnergy(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = capacity <= 0 ? 0.0F : Math.clamp((float) getEnergy(stack) / capacity, 0.0F, 1.0F); return Mth.hsvToRgb(ratio / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (batteryType == BatteryType.INFINITE) {
            tooltip.add(Component.literal("Infinite energy").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("Discharge rate: unlimited").withStyle(ChatFormatting.YELLOW));
            return;
        }
        if (batteryType == BatteryType.SELF_CHARGING) {
            tooltip.add(Component.literal("Self-charging").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("Discharge rate: " + dischargeRate + "HE/t").withStyle(ChatFormatting.YELLOW));
            return;
        }
        int energy = getEnergy(stack);
        double percent = capacity <= 0 ? 0.0D : energy * 100.0D / capacity;
        tooltip.add(Component.literal("Energy stored: " + energy + "/" + capacity + "HE (" + String.format("%.1f", percent) + "%)").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Charge rate: " + chargeRate + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Discharge rate: " + dischargeRate + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Time for full charge: " + formatMinutes(capacity, chargeRate) + "min").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Charge lasts for: " + formatMinutes(capacity, dischargeRate) + "min").withStyle(ChatFormatting.GOLD));
    }

    private static String formatMinutes(int amount, int rate) {
        if (rate <= 0) {
            return "0.0";
        }
        return String.format("%.1f", amount / (rate * 20.0D * 60.0D));
    }

    private static ItemBatteryPack battery(ItemStack stack) {
        return stack.getItem() instanceof ItemBatteryPack battery ? battery : null;
    }
}
