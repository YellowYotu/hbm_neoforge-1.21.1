package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationShielding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class RadiationShieldingPaneBlock extends IronBarsBlock implements RadiationShielding {

    public RadiationShieldingPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return state
                .setValue(NORTH, state.getValue(NORTH) || isReinforcedGlass(level.getBlockState(pos.north())))
                .setValue(EAST, state.getValue(EAST) || isReinforcedGlass(level.getBlockState(pos.east())))
                .setValue(SOUTH, state.getValue(SOUTH) || isReinforcedGlass(level.getBlockState(pos.south())))
                .setValue(WEST, state.getValue(WEST) || isReinforcedGlass(level.getBlockState(pos.west())));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction.getAxis().isHorizontal() && isReinforcedGlass(neighborState)) {
            return updated.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
        }
        return updated;
    }

    private static boolean isReinforcedGlass(BlockState state) {
        return state.is(ModBlocks.REINFORCED_GLASS.get());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            ChunkRadiationManager.markSectionForRebuild(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            ChunkRadiationManager.markSectionForRebuild(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public float getRadiationResistance(BlockState state) {
        return 25.0F;
    }
}
