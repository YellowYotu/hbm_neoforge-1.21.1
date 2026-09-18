package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.AirIntakeBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.AirIntakeDummyBlockEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class AirIntakeBlock extends BaseEntityBlock {
    public static final MapCodec<AirIntakeBlock> CODEC = simpleCodec(AirIntakeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AirIntakeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        for (BlockPos part : getFootprint(pos, facing)) {
            if (!part.equals(pos) && !context.getLevel().getBlockState(part).canBeReplaced(context)) {
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
        for (BlockPos part : getFootprint(pos, state.getValue(FACING))) {
            if (part.equals(pos)) {
                continue;
            }
            level.setBlock(part, ModBlocks.AIR_INTAKE_DUMMY.get().defaultBlockState(), 3);
            if (level.getBlockEntity(part) instanceof AirIntakeDummyBlockEntity dummy) {
                dummy.setController(pos);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            for (BlockPos part : getFootprint(pos, state.getValue(FACING))) {
                if (!part.equals(pos) && level.getBlockState(part).is(ModBlocks.AIR_INTAKE_DUMMY.get())) {
                    level.removeBlock(part, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static List<BlockPos> getFootprint(BlockPos controller, Direction facing) {
        BlockPos east = controller.east();
        BlockPos south = controller.south();
        return List.of(controller, east, south, east.south());
    }

    public static List<BlockPos> getConnectionAnchors(BlockPos controller, Direction facing) {
        List<BlockPos> footprint = getFootprint(controller, facing);
        List<BlockPos> anchors = new ArrayList<>(8);
        for (BlockPos part : footprint) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = part.relative(direction);
                if (!footprint.contains(adjacent) && !anchors.contains(adjacent)) {
                    anchors.add(adjacent);
                }
            }
        }
        return anchors;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.AIR_INTAKE.get());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, com.yellowyotu.hbmneoforge.ModBlockEntities.AIR_INTAKE.get(), AirIntakeBlockEntity::serverTick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirIntakeBlockEntity(pos, state);
    }
}
