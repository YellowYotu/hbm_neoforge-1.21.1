package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RedCableBlock extends Block {
    public static final MapCodec<RedCableBlock> CODEC = simpleCodec(RedCableBlock::new);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    private static final VoxelShape CORE = box(5.5D, 5.5D, 5.5D, 10.5D, 10.5D, 10.5D);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);

    static {
        ARMS.put(Direction.NORTH, box(5.5D, 5.5D, 0.0D, 10.5D, 10.5D, 5.5D));
        ARMS.put(Direction.SOUTH, box(5.5D, 5.5D, 10.5D, 10.5D, 10.5D, 16.0D));
        ARMS.put(Direction.WEST, box(0.0D, 5.5D, 5.5D, 5.5D, 10.5D, 10.5D));
        ARMS.put(Direction.EAST, box(10.5D, 5.5D, 5.5D, 16.0D, 10.5D, 10.5D));
        ARMS.put(Direction.DOWN, box(5.5D, 0.0D, 5.5D, 10.5D, 5.5D, 10.5D));
        ARMS.put(Direction.UP, box(5.5D, 10.5D, 5.5D, 10.5D, 16.0D, 10.5D));
    }

    public RedCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateForConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return state.setValue(property(direction), canConnect(neighborState));
    }

    private BlockState stateForConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(property(direction), canConnect(level.getBlockState(pos.relative(direction))));
        }
        return state;
    }

    private static boolean canConnect(BlockState state) {
        return state.is(ModBlocks.RED_CABLE.get()) || state.is(ModBlocks.ASSEMBLY_MACHINE.get()) || state.is(ModBlocks.ASSEMBLY_MACHINE_DUMMY.get()) || state.is(ModBlocks.MACHINE_BATTERY_SOCKET.get()) || state.is(ModBlocks.MACHINE_BATTERY_SOCKET_DUMMY.get()) || state.is(ModBlocks.SHREDDER.get()) || state.is(ModBlocks.SOLDERING_STATION.get()) || state.is(ModBlocks.SOLDERING_STATION_DUMMY.get());
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            if (state.getValue(property(direction))) {
                shape = Shapes.or(shape, ARMS.get(direction));
            }
        }
        return shape;
    }
}
