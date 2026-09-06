package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class RadiationShieldingBlockItem extends BlockItem {
    private final float resistance;
    public RadiationShieldingBlockItem(Block block, Properties properties, float resistance) { super(block, properties); this.resistance = resistance; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) { super.appendHoverText(stack, context, tooltip, flag); tooltip.add(Component.translatable("trait.radshield").withStyle(ChatFormatting.YELLOW)); tooltip.add(Component.translatable("trait.radResistance", trim(resistance)).withStyle(ChatFormatting.YELLOW)); }
    private static String trim(float value) { return value == (int) value ? Integer.toString((int) value) : Float.toString(value); }
}
