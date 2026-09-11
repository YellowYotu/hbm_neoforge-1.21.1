package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import com.yellowyotu.hbmneoforge.block.QeContainmentDoorBlock;
import com.yellowyotu.hbmneoforge.block.SlidingSealDoorBlock;
import com.yellowyotu.hbmneoforge.blockentity.QeContainmentDoorBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.SlidingSealDoorBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ItemScrewdriver extends Item {
    public ItemScrewdriver(Properties properties) {
        super(properties.durability(100).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            return super.useOn(context);
        }

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockPos core = null;
        if (state.getBlock() instanceof QeContainmentDoorBlock) {
            core = QeContainmentDoorBlock.getCorePos(state, context.getClickedPos());
        } else if (state.getBlock() instanceof SlidingSealDoorBlock) {
            core = SlidingSealDoorBlock.getLowerPos(state, context.getClickedPos());
        }

        if (core == null) {
            return super.useOn(context);
        }

        if (!context.getLevel().isClientSide()) {
            if (context.getLevel().getBlockEntity(core) instanceof QeContainmentDoorBlockEntity door) {
                var mode = door.cycleAccessMode();
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.hbm_neoforge.door_mode", mode.displayName()), true);
            } else if (context.getLevel().getBlockEntity(core) instanceof SlidingSealDoorBlockEntity door) {
                var mode = door.cycleAccessMode();
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.hbm_neoforge.door_mode", mode.displayName()), true);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("desc.hbm_neoforge.screwdriver1").withStyle(ChatFormatting.GRAY));
    }
}
