package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.item.RadioactiveBlockItem;
import com.yellowyotu.hbmneoforge.item.RadioactiveItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RadiationExposure {

    private RadiationExposure() {
    }

    public static float getInventoryRadiationPerSecond(Player player) {
        float radiation = 0.0F;

        for (ItemStack stack : player.getInventory().items) {
            radiation += getStackRadiationPerSecond(stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            radiation += getStackRadiationPerSecond(stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            radiation += getStackRadiationPerSecond(stack);
        }

        return radiation;
    }

    public static float getStackRadiationPerSecond(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        if (stack.getItem() instanceof RadioactiveItem radioactiveItem) {
            return radioactiveItem.getRadiationPerSecond() * stack.getCount();
        }
        if (stack.getItem() instanceof RadioactiveBlockItem radioactiveBlockItem) {
            return radioactiveBlockItem.getRadiationPerSecond() * stack.getCount();
        }
        return 0.0F;
    }
}
