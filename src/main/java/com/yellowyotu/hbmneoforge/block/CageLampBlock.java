package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CageLampBlock extends DirectionalBlock {
    public static final MapCodec<CageLampBlock> CODEC = simpleCodec(CageLampBlock::new);
    private static final VoxelShape EAST = Block.box(0.0, 4.0, 4.0, 5.0, 12.0, 12.0);
    private static final VoxelShape WEST = Block.box(11.0, 4.0, 4.0, 16.0, 12.0, 12.0);
    private static final VoxelShape UP = Block.box(4.0, 0.0, 4.0, 12.0, 5.0, 12.0);
    private static final VoxelShape DOWN = Block.box(4.0, 11.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape SOUTH = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 5.0);
    private static final VoxelShape NORTH = Block.box(4.0, 4.0, 11.0, 12.0, 12.0, 16.0);

    public CageLampBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
            case SOUTH -> SOUTH;
            case NORTH -> NORTH;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
