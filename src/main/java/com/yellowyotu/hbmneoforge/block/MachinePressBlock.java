package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.item.ItemStack;

public final class MachinePressBlock extends BaseEntityBlock {

    public static final MapCodec<MachinePressBlock> CODEC =
            simpleCodec(MachinePressBlock::new);

    public MachinePressBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }

        if (!level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }

        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) {
            return;
        }

        level.setBlock(
                pos.above(),
                ModBlocks.MACHINE_PRESS_DUMMY.get()
                        .defaultBlockState()
                        .setValue(MachinePressDummyBlock.OFFSET, 1),
                Block.UPDATE_ALL
        );

        level.setBlock(
                pos.above(2),
                ModBlocks.MACHINE_PRESS_DUMMY.get()
                        .defaultBlockState()
                        .setValue(MachinePressDummyBlock.OFFSET, 2),
                Block.UPDATE_ALL
        );
    }

    /*
     * Важно: удаляем dummy-блоки через onRemove, а не через playerWillDestroy.
     * onRemove срабатывает также при разрушении главного блока из кода.
     */
    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()) {
                if (level.getBlockEntity(pos)
                        instanceof MachinePressBlockEntity press) {

                    dropInventory(level, pos, press);
                }

                removeDummy(level, pos.above());
                removeDummy(level, pos.above(2));
            }

            super.onRemove(
                    state,
                    level,
                    pos,
                    newState,
                    movedByPiston
            );
        }
    }

    private static void removeDummy(Level level, BlockPos pos) {
        if (level.getBlockState(pos)
                .is(ModBlocks.MACHINE_PRESS_DUMMY.get())) {

            level.removeBlock(pos, false);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MachinePressBlockEntity press) {

            serverPlayer.openMenu(
                    press,
                    buffer -> buffer.writeBlockPos(pos)
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void dropInventory(
            Level level,
            BlockPos pos,
            MachinePressBlockEntity press
    ) {
        var inventory = press.getInventory();

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.extractItem(
                    slot,
                    inventory.getStackInSlot(slot).getCount(),
                    false
            );

            if (!stack.isEmpty()) {
                popResource(level, pos, stack);
            }
        }
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        return blockEntity instanceof MenuProvider provider
                ? provider
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.MACHINE_PRESS.get(),
                MachinePressBlockEntity::tick
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachinePressBlockEntity(pos, state);
    }
}