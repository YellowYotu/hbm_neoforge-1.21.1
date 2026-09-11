package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class WipBlockItem extends HBMBlockItem {
    private final String[] tooltipKeys;

    public WipBlockItem(Block block, Properties properties, String... tooltipKeys) {
        super(block, properties);
        this.tooltipKeys = tooltipKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (int i = 0; i < tooltipKeys.length; i++) {
            tooltip.add(Component.translatable(tooltipKeys[i]).withStyle(i == 0 ? ChatFormatting.RED : ChatFormatting.YELLOW));
        }
    }
}
