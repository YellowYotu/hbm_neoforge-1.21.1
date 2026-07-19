package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class RadioactiveBlockItem extends BlockItem {
    private final float radiationPerSecond;

    public RadioactiveBlockItem(Block block, Properties properties, float radiationPerSecond) {
        super(block, properties);
        this.radiationPerSecond = radiationPerSecond;
    }

    public float getRadiationPerSecond() {
        return radiationPerSecond;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal(format(radiationPerSecond) + " RAD/s").withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1) {
            tooltip.add(Component.literal("Stack: " + format(radiationPerSecond * stack.getCount()) + " RAD/s")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static String format(float value) {
        return String.valueOf(Math.floor(value * 1000.0F) / 1000.0F);
    }
}
