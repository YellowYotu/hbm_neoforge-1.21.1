package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SolderingStationBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 24.0D, 16.0D);
    private static final ThreadLocal<Boolean> REMOVING_MACHINE = ThreadLocal.withInitial(() -> false);

    public static final MapCodec<SolderingStationBlock> CODEC = simpleCodec(SolderingStationBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public SolderingStationBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos controller = context.getClickedPos();
        for (BlockPos pos : getAllPositions(controller, facing)) {
            if (!context.getLevel().getBlockState(pos).canBeReplaced(context)) {
                return null;
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockState dummyState = ModBlocks.SOLDERING_STATION_DUMMY.get().defaultBlockState().setValue(SolderingStationDummyBlock.FACING, facing);
        REMOVING_MACHINE.set(true);
        try {
            for (BlockPos dummyPos : getDummyPositions(pos, facing)) {
                level.setBlock(dummyPos, dummyState, Block.UPDATE_ALL);
            }
        } finally {
            REMOVING_MACHINE.set(false);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof SolderingStationBlockEntity station) {
            serverPlayer.openMenu(station, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof SolderingStationBlockEntity station) {
                station.dropContents();
            }
            removeDummyBlocks(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void removeDummyBlocks(Level level, BlockPos controller, Direction facing) {
        Set<BlockPos> positions = new LinkedHashSet<>(getDummyPositions(controller, facing));
        positions.addAll(getLegacyDummyPositions(controller, facing));

        REMOVING_MACHINE.set(true);
        try {
            for (BlockPos dummyPos : positions) {
                if (level.getBlockState(dummyPos).is(ModBlocks.SOLDERING_STATION_DUMMY.get())) {
                    level.setBlock(dummyPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        } finally {
            REMOVING_MACHINE.set(false);
        }
    }

    public static boolean isRemovingMachine() {
        return REMOVING_MACHINE.get();
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
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolderingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.SOLDERING_STATION.get(), SolderingStationBlockEntity::serverTick);
    }

    public static List<BlockPos> getAllPositions(BlockPos controller, Direction facing) {
        List<BlockPos> positions = new ArrayList<>(4);
        positions.add(controller);
        positions.addAll(getDummyPositions(controller, facing));
        return List.copyOf(positions);
    }

    public static List<BlockPos> getDummyPositions(BlockPos controller, Direction facing) {
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();
        BlockPos backPos = controller.relative(back);
        BlockPos rightPos = controller.relative(right);
        return List.of(backPos, rightPos, backPos.relative(right));
    }

    public static boolean containsPosition(BlockPos controller, Direction facing, BlockPos position) {
        return getAllPositions(controller, facing).contains(position) || getLegacyAllPositions(controller, facing).contains(position);
    }

    private static List<BlockPos> getLegacyAllPositions(BlockPos controller, Direction facing) {
        List<BlockPos> positions = new ArrayList<>(4);
        positions.add(controller);
        positions.addAll(getLegacyDummyPositions(controller, facing));
        return positions;
    }

    private static List<BlockPos> getLegacyDummyPositions(BlockPos controller, Direction facing) {
        Direction left = facing.getCounterClockWise();
        BlockPos forward = controller.relative(facing);
        BlockPos leftPos = controller.relative(left);
        return List.of(forward, leftPos, forward.relative(left));
    }
}
