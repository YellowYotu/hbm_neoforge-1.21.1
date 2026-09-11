package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.FoundryChannelBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FoundryChannelBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    private static final VoxelShape CENTER = Block.box(5, 0, 5, 11, 8, 11);
    private static final VoxelShape NORTH_SHAPE = Block.box(5, 0, 0, 11, 8, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 0, 11, 11, 8, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 5, 5, 8, 11);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 0, 5, 16, 8, 11);

    public FoundryChannelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return connections(level, pos, state);
    }

    private BlockState connections(BlockGetter level, BlockPos pos, BlockState state) {
        return state.setValue(NORTH, connects(level, pos.north(), Direction.NORTH))
                .setValue(SOUTH, connects(level, pos.south(), Direction.SOUTH))
                .setValue(WEST, connects(level, pos.west(), Direction.WEST))
                .setValue(EAST, connects(level, pos.east(), Direction.EAST));
    }

    private boolean connects(BlockGetter level, BlockPos pos, Direction directionToNeighbor) {
        BlockState neighbor = level.getBlockState(pos);
        if (neighbor.getBlock() instanceof FoundryChannelBlock) {
            return true;
        }
        if (neighbor.getBlock() instanceof FoundryCastingBlock casting && !casting.isBasin()) {
            return true;
        }
        if (neighbor.getBlock() instanceof FoundryOutletBlock) {
            return neighbor.getValue(FoundryOutletBlock.FACING) == directionToNeighbor;
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER;
        if (state.getValue(NORTH)) { shape = Shapes.or(shape, NORTH_SHAPE); }
        if (state.getValue(SOUTH)) { shape = Shapes.or(shape, SOUTH_SHAPE); }
        if (state.getValue(WEST)) { shape = Shapes.or(shape, WEST_SHAPE); }
        if (state.getValue(EAST)) { shape = Shapes.or(shape, EAST_SHAPE); }
        return shape;
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FoundryChannelBlockEntity channel && channel.getAmount() > 0 && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(ParticleTypes.LAVA, x, pos.getY() + 0.28D, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryChannelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FOUNDRY_CHANNEL.get(), FoundryChannelBlockEntity::serverTick);
    }
}
