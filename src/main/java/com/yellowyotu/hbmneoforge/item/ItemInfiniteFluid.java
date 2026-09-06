package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemInfiniteFluid extends Item {
    private final NTMFluidType type;
    private final int rate;

    public ItemInfiniteFluid(Properties properties, NTMFluidType type, int rate) {
        super(properties);
        this.type = type;
        this.rate = rate;
    }

    public NTMFluidType getFluidType() {
        return type;
    }

    public int getRate() {
        return rate;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide()) {
            NTMFluidType effective = type;
            if (effective == null) {
                effective = node instanceof FluidPipeBlockEntity pipe ? pipe.getFilter() : node.getFluidType();
            }
            if (effective == null) {
                return InteractionResult.FAIL;
            }

            if (node instanceof FluidPipeBlockEntity pipe) {
                if (pipe.getFilter() != effective) {
                    return InteractionResult.FAIL;
                }
                FluidNetworkUtil.fillNetwork(context.getLevel(), context.getClickedPos(), effective, rate, null);
            } else {
                node.fill(effective, rate);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(type == null
                ? Component.translatable("tooltip.hbm_neoforge.infinite_fluid_universal").withStyle(ChatFormatting.LIGHT_PURPLE)
                : Component.translatable("tooltip.hbm_neoforge.infinite_fluid", type.displayName()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(rate + " mB/t").withStyle(ChatFormatting.GRAY));
    }
}
