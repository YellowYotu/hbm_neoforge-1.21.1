package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.block.FluidPipeBlock;
import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public final class ItemFluidIdentifier extends Item {
    private final NTMFluidType type;

    public ItemFluidIdentifier(Properties properties, NTMFluidType type) {
        super(properties);
        this.type = type;
    }

    public NTMFluidType getFluidType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!context.getLevel().isClientSide()) {
            if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidPipeBlockEntity pipe) {
                if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) { pipe.clearFilter(); } else { pipe.setFilter(type); }
                return InteractionResult.SUCCESS;
            }
            if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidStorageBlockEntity storage) {
                storage.setFluidType(type);
                return InteractionResult.SUCCESS;
            }
        }
        if (state.getBlock() instanceof FluidPipeBlock || context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidStorageBlockEntity) { return InteractionResult.sidedSuccess(context.getLevel().isClientSide()); }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.fluid_identifier", type.displayName()).withStyle(ChatFormatting.AQUA));
    }
}
