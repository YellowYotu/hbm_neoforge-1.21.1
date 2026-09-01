package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class HBMBlockItem extends BlockItem {
    public HBMBlockItem(Block block, Properties properties) { super(block, properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        float resistance = Math.min(100.0F, Math.max(0.0F, getBlock().getExplosionResistance()));
        if (resistance > 0.0F) { tooltip.add(Component.translatable("trait.radResistance", format(resistance)).withStyle(ChatFormatting.YELLOW)); }
        net.minecraft.nbt.CompoundTag data = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (data.contains("filter")) {
            com.yellowyotu.hbmneoforge.fluid.NTMFluidType type = com.yellowyotu.hbmneoforge.fluid.NTMFluidType.byId(data.getString("filter"));
            if (type != null) {
                tooltip.add(type.displayName().copy().withStyle(ChatFormatting.AQUA));
            }
        }
    }
    protected static String format(float value) { return value == Math.rint(value) ? Integer.toString((int) value) : Float.toString(value); }
}
