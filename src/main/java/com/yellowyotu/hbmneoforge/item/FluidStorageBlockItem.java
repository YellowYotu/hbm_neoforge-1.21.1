package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.block.FluidStorageBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class FluidStorageBlockItem extends HBMBlockItem {
    private final int capacity;
    private final FluidStorageBlock.StorageKind kind;

    public FluidStorageBlockItem(FluidStorageBlock block, Properties properties, int capacity, FluidStorageBlock.StorageKind kind) {
        super(block, properties);
        this.capacity = capacity;
        this.kind = kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("desc.hbm_neoforge.capacity", String.format("%,d", capacity)).withStyle(ChatFormatting.AQUA));
        switch (kind) {
            case PLASTIC_BARREL -> {
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannothot").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannotcor").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannotam").withStyle(ChatFormatting.YELLOW));
            }
            case CORRODED_BARREL -> {
                tooltip.add(Component.translatable("desc.hbm_neoforge.canhot").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.canhighcor").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannotam").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("desc.hbm_neoforge.leaky").withStyle(ChatFormatting.RED));
            }
            case STEEL_BARREL -> {
                tooltip.add(Component.translatable("desc.hbm_neoforge.canhot").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cancor").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannothighcor").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("desc.hbm_neoforge.cannotam").withStyle(ChatFormatting.YELLOW));
            }
            case ANTIMATTER_BARREL -> {
                tooltip.add(Component.translatable("desc.hbm_neoforge.canhot").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.canhighcor").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("desc.hbm_neoforge.canam").withStyle(ChatFormatting.GREEN));
            }
            case TANK -> { }
        }
    }
}
