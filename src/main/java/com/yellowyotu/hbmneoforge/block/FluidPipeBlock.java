package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidPipeBlock extends BaseEntityBlock {
    public enum Style { NEO, BOX, PAINTABLE, GAUGE, VALVE, PUMP }

    public static final MapCodec<FluidPipeBlock> CODEC = simpleCodec(properties -> new FluidPipeBlock(properties, null, Style.NEO));
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty FILTERED = BooleanProperty.create("filtered");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape CENTER = box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape N = box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape S = box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape W = box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape E = box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape U = box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape D = box(5, 0, 5, 11, 5, 11);

    private final NTMFluidType fixedFilter;
    private final Style style;

    public FluidPipeBlock(Properties properties, NTMFluidType fixedFilter) {
        this(properties, fixedFilter, Style.NEO);
    }

    public FluidPipeBlock(Properties properties, NTMFluidType fixedFilter, Style style) {
        super(properties);
        this.fixedFilter = fixedFilter;
        this.style = style;
        registerDefaultState(stateDefinition.any().setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false).setValue(UP, false).setValue(DOWN, false).setValue(FILTERED, fixedFilter != null).setValue(ACTIVE, true).setValue(FACING, Direction.NORTH));
    }

    public NTMFluidType getFixedFilter() {
        return fixedFilter;
    }

    public Style getStyle() {
        return style;
    }

    public int getTransferRate() {
        return style == Style.PUMP ? 4_000 : 1_000;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, FILTERED, ACTIVE, FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return updateConnections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateConnections(state, level, pos);
    }

    public static void refreshConnections(@Nullable Level level, BlockPos pos) {
        if (level == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FluidPipeBlock) {
            BlockState updated = updateConnections(state, level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, 3);
            }
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof FluidPipeBlock) {
                BlockState updated = updateConnections(neighborState, level, neighborPos);
                if (updated != neighborState) {
                    level.setBlock(neighborPos, updated, 3);
                }
            }
        }
    }

    private static BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        return state
                .setValue(NORTH, canConnect(level, pos, Direction.NORTH))
                .setValue(SOUTH, canConnect(level, pos, Direction.SOUTH))
                .setValue(WEST, canConnect(level, pos, Direction.WEST))
                .setValue(EAST, canConnect(level, pos, Direction.EAST))
                .setValue(UP, canConnect(level, pos, Direction.UP))
                .setValue(DOWN, canConnect(level, pos, Direction.DOWN));
    }

    private static boolean canConnect(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockEntity selfEntity = level.getBlockEntity(pos);
        BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
        if (selfEntity instanceof FluidPipeBlockEntity self && neighborEntity instanceof FluidPipeBlockEntity other) {
            return self.canConnectTo(other);
        }
        if (selfEntity instanceof FluidPipeBlockEntity && neighborEntity instanceof com.yellowyotu.hbmneoforge.blockentity.FluidStorageDummyBlockEntity dummy) {
            BlockPos core = dummy.getController();
            if (core == null) {
                return false;
            }
            BlockState coreState = level.getBlockState(core);
            return coreState.getBlock() instanceof FluidTankMultiblockBlock tank && tank.canConnectAt(level, neighborPos, direction.getOpposite());
        }
        if (selfEntity instanceof FluidPipeBlockEntity && neighborEntity instanceof com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity storage) {
            BlockState storageState = level.getBlockState(neighborPos);
            if (storageState.getBlock() instanceof FluidTankMultiblockBlock tank) {
                return tank.canConnectAt(level, neighborPos, direction.getOpposite());
            }
            return true;
        }
        if (selfEntity instanceof FluidPipeBlockEntity && neighborEntity instanceof FluidNode) {
            return true;
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean n = state.getValue(NORTH);
        boolean s = state.getValue(SOUTH);
        boolean w = state.getValue(WEST);
        boolean e = state.getValue(EAST);
        boolean u = state.getValue(UP);
        boolean d = state.getValue(DOWN);
        int count = (n ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0) + (e ? 1 : 0) + (u ? 1 : 0) + (d ? 1 : 0);

        if (count == 0) {
            return Shapes.or(CENTER, N, S, W, E, U, D);
        }
        if (count == 1) {
            if (e || w) {
                return Shapes.or(CENTER, W, E);
            }
            if (u || d) {
                return Shapes.or(CENTER, U, D);
            }
            return Shapes.or(CENTER, N, S);
        }

        VoxelShape shape = CENTER;
        if (n) { shape = Shapes.or(shape, N); }
        if (s) { shape = Shapes.or(shape, S); }
        if (w) { shape = Shapes.or(shape, W); }
        if (e) { shape = Shapes.or(shape, E); }
        if (u) { shape = Shapes.or(shape, U); }
        if (d) { shape = Shapes.or(shape, D); }
        return shape;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) {
                NTMFluidType placedFilter = fixedFilter;
                net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (placedFilter == null && tag.contains("filter")) {
                    placedFilter = NTMFluidType.byId(tag.getString("filter"));
                }
                if (placedFilter != null) {
                    pipe.setFilter(placedFilter);
                }
            }
            refreshConnections(level, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (style == Style.PAINTABLE && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != this) {
            BlockState disguise = blockItem.getBlock().defaultBlockState();
            if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe && pipe.getDisguiseState() == null) {
                if (!level.isClientSide()) {
                    pipe.setDisguiseState(disguise);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FLUID_PIPE.get(), FluidPipeBlockEntity::serverTick);
    }
}
