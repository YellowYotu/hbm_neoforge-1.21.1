package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.AbstractHeaterBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HeaterDummyBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public HeaterDummyBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BlockPos findController(BlockGetter level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos candidate = pos.offset(x, 0, z);
                BlockState state = level.getBlockState(candidate);
                if (state.getBlock() instanceof AbstractHeaterBlock && AbstractHeaterBlock.containsPosition(candidate, pos)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos controller = findController(level, pos);
        if (controller == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(controller) instanceof AbstractHeaterBlockEntity heater) {
            serverPlayer.openMenu(heater, buffer -> buffer.writeBlockPos(controller));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && !AbstractHeaterBlock.isRemovingMachine()) {
            BlockPos controller = findController(level, pos);
            if (controller != null) {
                level.destroyBlock(controller, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos controller = findController(level, pos);
        if (controller != null) {
            BlockState controllerState = level.getBlockState(controller);
            if (controllerState.is(ModBlocks.FIREBOX.get())) {
                return new ItemStack(ModItems.FIREBOX.get());
            }
            if (controllerState.is(ModBlocks.HEATING_OVEN.get())) {
                return new ItemStack(ModItems.HEATING_OVEN.get());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
