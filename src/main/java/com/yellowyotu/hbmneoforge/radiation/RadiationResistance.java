package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModEffects;
import com.yellowyotu.hbmneoforge.ModItems;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RadiationResistance {

    private static final UUID PU_238_UUID = UUID.fromString("c95fdfd3-bea7-4255-a44b-d21bc3df95e3");
    private static final float HELMET_SHARE = 0.2F;
    private static final float CHESTPLATE_SHARE = 0.4F;
    private static final float LEGGINGS_SHARE = 0.3F;
    private static final float BOOTS_SHARE = 0.1F;
    private static final float IRON_RESISTANCE = 0.0225F;
    private static final float GOLD_RESISTANCE = 0.0225F;
    private static final float HAZMAT_RESISTANCE = 0.6F;
    private static final Map<Item, Float> ARMOR_RESISTANCE = new IdentityHashMap<>();

    static {
        registerArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, IRON_RESISTANCE);
        registerArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, GOLD_RESISTANCE);
        registerArmorSet(ModItems.HAZMAT_HELMET.get(), ModItems.HAZMAT_PLATE.get(), ModItems.HAZMAT_LEGS.get(), ModItems.HAZMAT_BOOTS.get(), HAZMAT_RESISTANCE);
    }

    private RadiationResistance() {
    }

    public static void register(Item item, float resistance) {
        ARMOR_RESISTANCE.put(item, resistance);
    }

    public static float getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        return ARMOR_RESISTANCE.getOrDefault(stack.getItem(), 0.0F);
    }

    public static float getResistance(Player player) {
        float resistance = player.getUUID().equals(PU_238_UUID) ? 0.4F : 0.0F;
        for (ItemStack stack : player.getArmorSlots()) {
            resistance += getResistance(stack);
        }
        if (player.hasEffect(ModEffects.RAD_X)) {
            resistance += RadiationValues.RAD_X_RESISTANCE;
        }
        return resistance;
    }

    public static float calculateRadiationModifier(Player player) {
        return (float) Math.pow(10.0F, -getResistance(player));
    }

    public static float getProtectionPercent(Player player) {
        return (1.0F - calculateRadiationModifier(player)) * 100.0F;
    }

    private static void registerArmorSet(Item helmet, Item chestplate, Item leggings, Item boots, float totalResistance) {
        register(helmet, totalResistance * HELMET_SHARE);
        register(chestplate, totalResistance * CHESTPLATE_SHARE);
        register(leggings, totalResistance * LEGGINGS_SHARE);
        register(boots, totalResistance * BOOTS_SHARE);
    }
}
