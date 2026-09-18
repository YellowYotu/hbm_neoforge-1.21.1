package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderDummyBlockEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

public final class ArcWelderBlock extends BaseEntityBlock {
    public static final MapCodec<ArcWelderBlock> CODEC = simpleCodec(ArcWelderBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ArcWelderBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
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
            if (!pos.equals(controller) && !context.getLevel().getBlockState(pos).canBeReplaced(context)) {
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
        for (BlockPos dummyPos : getDummyPositions(pos, facing)) {
            level.setBlock(dummyPos, ModBlocks.ARC_WELDER_DUMMY.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(dummyPos) instanceof ArcWelderDummyBlockEntity dummy) {
                dummy.setController(pos);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ArcWelderBlockEntity welder) {
            serverPlayer.openMenu(welder, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ArcWelderBlockEntity welder) {
                welder.dropContents();
            }
            for (BlockPos dummyPos : getDummyPositions(pos, state.getValue(FACING))) {
                if (level.getBlockState(dummyPos).is(ModBlocks.ARC_WELDER_DUMMY.get())) {
                    level.setBlock(dummyPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static List<BlockPos> getAllPositions(BlockPos controller, Direction facing) {
        List<BlockPos> result = new ArrayList<>(12);
        result.add(controller);
        result.addAll(getDummyPositions(controller, facing));
        return List.copyOf(result);
    }

    public static List<BlockPos> getDummyPositions(BlockPos controller, Direction facing) {
        Direction right = facing.getClockWise();
        Direction left = right.getOpposite();
        Direction back = facing.getOpposite();
        List<BlockPos> result = new ArrayList<>(11);
        List<BlockPos> bottom = List.of(
                controller.relative(left),
                controller.relative(right),
                controller.relative(back),
                controller.relative(back).relative(left),
                controller.relative(back).relative(right));
        result.addAll(bottom);
        result.add(controller.above());
        for (BlockPos bottomPos : bottom) {
            result.add(bottomPos.above());
        }
        return List.copyOf(result);
    }

    public static List<BlockPos> getConnectionAnchors(BlockPos controller, Direction facing) {
        List<BlockPos> structure = getAllPositions(controller, facing);
        List<BlockPos> anchors = new ArrayList<>();
        for (BlockPos part : structure) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = part.relative(direction);
                if (!structure.contains(adjacent) && !anchors.contains(adjacent)) {
                    anchors.add(adjacent);
                }
            }
        }
        return List.copyOf(anchors);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.ARC_WELDER.get());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcWelderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.ARC_WELDER.get(), ArcWelderBlockEntity::serverTick);
    }
}
