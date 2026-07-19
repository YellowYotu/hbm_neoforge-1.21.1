package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class MachinePressDummyBlock extends Block {

    public static final MapCodec<MachinePressDummyBlock> CODEC =
            simpleCodec(MachinePressDummyBlock::new);

    public static final IntegerProperty OFFSET =
            IntegerProperty.create("offset", 1, 2);

    public MachinePressDummyBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any().setValue(OFFSET, 1)
        );
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(OFFSET);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        int offset = state.getValue(OFFSET);
        BlockPos mainPos = pos.below(offset);

        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(mainPos) instanceof MachinePressBlockEntity press) {

            serverPlayer.openMenu(
                    press,
                    buffer -> buffer.writeBlockPos(mainPos)
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /*
     * При разрушении любой верхней части уничтожаем основной блок.
     * Основной блок через свой onRemove удалит обе dummy-части.
     */
    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            int offset = state.getValue(OFFSET);
            BlockPos mainPos = pos.below(offset);

            if (level.getBlockState(mainPos).is(ModBlocks.MACHINE_PRESS.get())) {
                level.destroyBlock(mainPos, true);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}