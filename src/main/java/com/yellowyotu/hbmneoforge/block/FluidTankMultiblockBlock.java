package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageDummyBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class FluidTankMultiblockBlock extends FluidStorageBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty FLUID_MODEL = IntegerProperty.create("fluid_model", 0, NTMFluidType.values().length);
    private final int length;
    private final int width;
    private final int height;

    public FluidTankMultiblockBlock(Properties properties, int capacity, StorageKind kind, int length, int width, int height) {
        super(properties, capacity, kind);
        this.length = length;
        this.width = width;
        this.height = height;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FLUID_MODEL, NTMFluidType.values().length));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FLUID_MODEL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return canPlaceStructure(context.getLevel(), context.getClickedPos(), state) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        forEachStructurePos(pos, state, target -> {
            if (target.equals(pos)) {
                return;
            }
            level.setBlock(target, ModBlocks.FLUID_TANK_DUMMY.get().defaultBlockState(), 3);
            if (level.getBlockEntity(target) instanceof FluidStorageDummyBlockEntity dummy) {
                dummy.setController(pos);
            }
        });
        refreshPortConnections(level, pos, state);
    }

    private boolean canPlaceStructure(Level level, BlockPos pos, BlockState state) {
        final boolean[] valid = {true};
        forEachStructurePos(pos, state, target -> {
            if (target.equals(pos)) {
                return;
            }
            if (!level.getBlockState(target).canBeReplaced()) {
                valid[0] = false;
            }
        });
        return valid[0];
    }

    public List<BlockPos> getExternalPortPositions(BlockPos core, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        Direction front = facing;
        int sideOffset = width / 2;
        int leftPort = -1;
        int rightPort = 1;
        List<BlockPos> ports = new ArrayList<>(4);
        ports.add(core.relative(right, leftPort).relative(front, sideOffset + 1));
        ports.add(core.relative(right, rightPort).relative(front, sideOffset + 1));
        ports.add(core.relative(right, leftPort).relative(front.getOpposite(), sideOffset + 1));
        ports.add(core.relative(right, rightPort).relative(front.getOpposite(), sideOffset + 1));
        return ports;
    }

    public boolean canConnectAt(LevelAccessor level, BlockPos structurePos, Direction outward) {
        BlockEntity blockEntity = level.getBlockEntity(structurePos);
        BlockPos core = null;
        if (blockEntity instanceof FluidStorageDummyBlockEntity dummy) {
            core = dummy.getController();
        } else if (blockEntity instanceof FluidStorageBlockEntity) {
            core = structurePos;
        }
        if (core == null) {
            return false;
        }
        BlockState coreState = level.getBlockState(core);
        if (!(coreState.getBlock() instanceof FluidTankMultiblockBlock tank)) {
            return false;
        }
        BlockPos external = structurePos.relative(outward);
        return tank.getExternalPortPositions(core, coreState).contains(external);
    }

    public void destroyStructure(Level level, BlockPos core, boolean drop) {
        BlockState state = level.getBlockState(core);
        if (!(state.getBlock() instanceof FluidTankMultiblockBlock)) {
            removeOwnedDummies(level, core);
            return;
        }
        removeOwnedDummies(level, core);
        if (drop && !level.isClientSide()) {
            Block.popResource(level, core, new ItemStack(asItem()));
        }
        level.setBlock(core, Blocks.AIR.defaultBlockState(), 35);
        refreshPortConnections(level, core, state);
    }

    private void removeOwnedDummies(Level level, BlockPos core) {
        BlockPos.betweenClosedStream(core.offset(-8, -1, -8), core.offset(8, 4, 8)).map(BlockPos::immutable).toList().forEach(target -> {
            if (level.getBlockEntity(target) instanceof FluidStorageDummyBlockEntity dummy && core.equals(dummy.getController())) {
                level.setBlock(target, Blocks.AIR.defaultBlockState(), 35);
                FluidPipeBlock.refreshConnections(level, target);
            }
        });
    }

    private void refreshPortConnections(Level level, BlockPos core, BlockState state) {
        for (BlockPos portPos : getExternalPortPositions(core, state)) {
            FluidPipeBlock.refreshConnections(level, portPos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            removeOwnedDummies(level, pos);
            refreshPortConnections(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FluidStorageBlockEntity tank) {
            serverPlayer.openMenu(tank, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void forEachStructurePos(BlockPos core, BlockState state, java.util.function.Consumer<BlockPos> consumer) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        int minLength = -(length / 2);
        int minWidth = -(width / 2);
        for (int y = 0; y < height; y++) {
            for (int lengthOffset = minLength; lengthOffset < minLength + length; lengthOffset++) {
                for (int widthOffset = minWidth; widthOffset < minWidth + width; widthOffset++) {
                    consumer.accept(core.relative(right, lengthOffset).relative(facing, widthOffset).above(y));
                }
            }
        }
    }
}
