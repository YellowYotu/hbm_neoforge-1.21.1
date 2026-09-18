package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.OilDerrickBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.OilDerrickDummyBlockEntity;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class OilDerrickBlock extends BaseEntityBlock {
    public static final MapCodec<OilDerrickBlock> CODEC = simpleCodec(OilDerrickBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public OilDerrickBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        BlockPos controller = context.getClickedPos();
        for (BlockPos part : getMachinePositions(controller)) {
            if (!part.equals(controller) && !context.getLevel().getBlockState(part).canBeReplaced(context)) {
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
        BlockState dummyState = ModBlocks.OIL_DERRICK_DUMMY.get().defaultBlockState();
        for (BlockPos part : getMachinePositions(pos)) {
            if (part.equals(pos)) {
                continue;
            }
            level.setBlock(part, dummyState, 3);
            if (level.getBlockEntity(part) instanceof OilDerrickDummyBlockEntity dummy) {
                dummy.setController(pos);
            }
            FluidPipeBlock.refreshConnections(level, part);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof OilDerrickBlockEntity derrick) {
            serverPlayer.openMenu(derrick, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof OilDerrickBlockEntity derrick) {
                derrick.dropContents();
            }
            for (BlockPos part : getMachinePositions(pos)) {
                if (!part.equals(pos) && level.getBlockState(part).is(ModBlocks.OIL_DERRICK_DUMMY.get())) {
                    level.removeBlock(part, false);
                    FluidPipeBlock.refreshConnections(level, part);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static List<BlockPos> getMachinePositions(BlockPos controller) {
        List<BlockPos> positions = new ArrayList<>(78);
        positions.add(controller);

        // Exact CE Oil Derrick lower layer: controller in the center and four corner dummies.
        positions.add(controller.offset(-1, 0, -1));
        positions.add(controller.offset(-1, 0, 1));
        positions.add(controller.offset(1, 0, -1));
        positions.add(controller.offset(1, 0, 1));

        // Exact CE Oil Derrick tower: full 3x3 from Y + 1 through Y + 9.
        for (int y = 1; y <= 9; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(controller.offset(x, y, z));
                }
            }
        }

        return List.copyOf(positions);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.OIL_DERRICK.get());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.OIL_DERRICK.get(), OilDerrickBlockEntity::serverTick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OilDerrickBlockEntity(pos, state);
    }
}
