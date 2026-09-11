package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class FluidValveBlock extends FluidPipeBlock {
    public FluidValveBlock(Properties properties) { super(properties, null, Style.VALVE); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) { pipe.toggleEnabled(); player.displayClientMessage(Component.literal(pipe.isEnabled() ? "Valve: OPEN" : "Valve: CLOSED"), true); }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
