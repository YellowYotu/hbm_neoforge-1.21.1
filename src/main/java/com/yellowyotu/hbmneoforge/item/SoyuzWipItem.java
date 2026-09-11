package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class SoyuzWipItem extends Item {
    public SoyuzWipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Skin:"));
        tooltip.add(Component.literal("Original").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.wip").withStyle(ChatFormatting.YELLOW));
    }
}
