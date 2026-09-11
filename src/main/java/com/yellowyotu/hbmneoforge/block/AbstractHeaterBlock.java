package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.AbstractHeaterBlockEntity;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractHeaterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final ThreadLocal<Boolean> REMOVING_MACHINE = ThreadLocal.withInitial(() -> false);

    protected AbstractHeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos controller = context.getClickedPos();
        for (BlockPos pos : getAllPositions(controller)) {
            if (pos.equals(controller)) {
                continue;
            }
            if (!context.getLevel().getBlockState(pos).canBeReplaced(context)) {
                return null;
            }
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(LIT, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        REMOVING_MACHINE.set(true);
        try {
            BlockState dummyState = ModBlocks.HEATER_DUMMY.get().defaultBlockState();
            for (BlockPos dummyPos : getDummyPositions(pos)) {
                level.setBlock(dummyPos, dummyState, Block.UPDATE_ALL);
            }
        } finally {
            REMOVING_MACHINE.set(false);
        }
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof AbstractHeaterBlockEntity heater) {
            serverPlayer.openMenu(heater, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof AbstractHeaterBlockEntity heater) {
                heater.dropContents();
            }
            removeDummyBlocks(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void removeDummyBlocks(Level level, BlockPos controller) {
        REMOVING_MACHINE.set(true);
        try {
            for (BlockPos dummyPos : getDummyPositions(controller)) {
                if (level.getBlockState(dummyPos).is(ModBlocks.HEATER_DUMMY.get())) {
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

    public static boolean containsPosition(BlockPos controller, BlockPos position) {
        return position.getY() == controller.getY() && Math.abs(position.getX() - controller.getX()) <= 1 && Math.abs(position.getZ() - controller.getZ()) <= 1;
    }

    public static List<BlockPos> getAllPositions(BlockPos controller) {
        List<BlockPos> positions = new ArrayList<>(9);
        positions.add(controller);
        positions.addAll(getDummyPositions(controller));
        return List.copyOf(positions);
    }

    public static List<BlockPos> getDummyPositions(BlockPos controller) {
        List<BlockPos> positions = new ArrayList<>(8);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                positions.add(controller.offset(x, 0, z));
            }
        }
        return List.copyOf(positions);
    }
}
