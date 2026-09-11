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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CageLampBlock extends DirectionalBlock {
    public static final MapCodec<CageLampBlock> CODEC = simpleCodec(CageLampBlock::new);
    public static final EnumProperty<Direction> ROTATION = EnumProperty.create("rotation", Direction.class, Direction.Plane.HORIZONTAL);
    private static final VoxelShape EAST = Block.box(0.0D, 4.0D, 3.0D, 3.25D, 12.0D, 13.0D);
    private static final VoxelShape WEST = Block.box(12.75D, 4.0D, 3.0D, 16.0D, 12.0D, 13.0D);
    private static final VoxelShape UP = Block.box(3.0D, 0.0D, 4.0D, 13.0D, 3.25D, 12.0D);
    private static final VoxelShape DOWN = Block.box(3.0D, 12.75D, 4.0D, 13.0D, 16.0D, 12.0D);
    private static final VoxelShape SOUTH = Block.box(3.0D, 4.0D, 0.0D, 13.0D, 12.0D, 3.25D);
    private static final VoxelShape NORTH = Block.box(3.0D, 4.0D, 12.75D, 13.0D, 12.0D, 16.0D);

    public CageLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ROTATION, Direction.NORTH));
    }

    @Override protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction attachment = context.getClickedFace();
        Direction rotation = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, attachment).setValue(ROTATION, rotation);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST; case WEST -> WEST; case UP -> UP; case DOWN -> DOWN; case SOUTH -> SOUTH; case NORTH -> NORTH;
        };
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, ROTATION); }
}
