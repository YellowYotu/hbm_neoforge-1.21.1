package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.blockentity.SlidingSealDoorBlockEntity;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationShielding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class SlidingSealDoorBlock extends BaseEntityBlock implements RadiationShielding {
    public static final int MAX_PROGRESS = 40;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final IntegerProperty PROGRESS = IntegerProperty.create("progress", 0, MAX_PROGRESS);
    private static final double PANEL_MIN = 6.0D;
    private static final double PANEL_MAX = 10.0D;
    private static final double MAX_SLIDE = 14.4D;
    private static final VoxelShape CLOSED_X = box(PANEL_MIN, 0.0D, 0.0D, PANEL_MAX, 16.0D, 16.0D);
    private static final VoxelShape CLOSED_Z = box(0.0D, 0.0D, PANEL_MIN, 16.0D, 16.0D, PANEL_MAX);
    private static final VoxelShape FRAME_X = box(PANEL_MIN, 0.0D, 0.0D, PANEL_MAX, 16.0D, 16.0D);
    private static final VoxelShape FRAME_Z = box(0.0D, 0.0D, PANEL_MIN, 16.0D, 16.0D, PANEL_MAX);

    public SlidingSealDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(OPEN, false).setValue(PROGRESS, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SlidingSealDoorBlock::new);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (pos.getY() >= context.getLevel().getMaxBuildHeight() - 1 || !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        if (!level.isClientSide()) {
            ChunkRadiationManager.markSectionForRebuild(level, pos);
            ChunkRadiationManager.markSectionForRebuild(level, pos.above());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos lowerPos = getLowerPos(state, pos);
        BlockState lowerState = level.getBlockState(lowerPos);
        if (!lowerState.is(this)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            boolean open = !lowerState.getValue(OPEN);
            setOpen(level, lowerPos, lowerState, open);
            level.playSound(null, lowerPos, ModSounds.SLIDING_SEAL_MOVE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 2.0F, open ? 1.0F : 0.9F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void setOpen(Level level, BlockPos lowerPos, BlockState lowerState, boolean open) {
        level.setBlock(lowerPos, lowerState.setValue(OPEN, open), 3);
        BlockPos upperPos = lowerPos.above();
        BlockState upperState = level.getBlockState(upperPos);
        if (upperState.is(this)) {
            level.setBlock(upperPos, upperState.setValue(OPEN, open), 3);
        }
        ChunkRadiationManager.markSectionForRebuild(level, lowerPos);
        ChunkRadiationManager.markSectionForRebuild(level, upperPos);
    }

    public static void updateProgress(Level level, BlockPos lowerPos, BlockState lowerState, int progress) {
        int previousProgress = lowerState.getValue(PROGRESS);
        BlockState updatedLower = lowerState.setValue(PROGRESS, progress);
        level.setBlock(lowerPos, updatedLower, 3);
        BlockPos upperPos = lowerPos.above();
        BlockState upperState = level.getBlockState(upperPos);
        if (upperState.is(lowerState.getBlock())) {
            level.setBlock(upperPos, upperState.setValue(PROGRESS, progress).setValue(OPEN, updatedLower.getValue(OPEN)), 3);
        }
        if ((previousProgress == 0) != (progress == 0)) {
            ChunkRadiationManager.markSectionForRebuild(level, lowerPos);
            ChunkRadiationManager.markSectionForRebuild(level, upperPos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, OPEN, PROGRESS);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            if (neighborState.is(this) && neighborState.getValue(HALF) != half) {
                return state.setValue(FACING, neighborState.getValue(FACING)).setValue(OPEN, neighborState.getValue(OPEN)).setValue(PROGRESS, neighborState.getValue(PROGRESS));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
            }
            if (!level.isClientSide()) {
                ChunkRadiationManager.markSectionForRebuild(level, pos);
                ChunkRadiationManager.markSectionForRebuild(level, otherPos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);
        if (!level.isClientSide() && otherState.is(this)) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getDoorShape(state);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getDoorShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getDoorShape(state);
    }

    private static VoxelShape getDoorShape(BlockState state) {
        Direction facing = state.getValue(FACING);
        double progress = Mth.clamp(state.getValue(PROGRESS) / (double) MAX_PROGRESS, 0.0D, 1.0D);
        double eased = progress * progress * (3.0D - 2.0D * progress);
        double shift = eased * MAX_SLIDE;
        if (facing == Direction.EAST) {
            return box(PANEL_MIN, 0.0D, 0.0D + shift, PANEL_MAX, 16.0D, 16.0D + shift);
        }
        if (facing == Direction.WEST) {
            return box(PANEL_MIN, 0.0D, 0.0D - shift, PANEL_MAX, 16.0D, 16.0D - shift);
        }
        if (facing == Direction.SOUTH) {
            return box(0.0D - shift, 0.0D, PANEL_MIN, 16.0D - shift, 16.0D, PANEL_MAX);
        }
        return box(0.0D + shift, 0.0D, PANEL_MIN, 16.0D + shift, 16.0D, PANEL_MAX);
    }


    @Override
    public boolean isRadiationShielding(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return state.getValue(PROGRESS) == 0;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SlidingSealDoorBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.SLIDING_SEAL_DOOR.get(), SlidingSealDoorBlockEntity::tick);
    }

    private static BlockPos getLowerPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }
}
