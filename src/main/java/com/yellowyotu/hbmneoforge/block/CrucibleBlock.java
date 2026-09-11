package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.CrucibleBlockEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CrucibleBlock extends BaseEntityBlock {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);

    public CrucibleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends CrucibleBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        REMOVING.set(true);
        try {
            BlockState dummy = ModBlocks.CRUCIBLE_DUMMY.get().defaultBlockState();
            for (BlockPos dummyPos : getDummyPositions(pos)) {
                level.setBlock(dummyPos, dummy, Block.UPDATE_ALL);
            }
        } finally {
            REMOVING.set(false);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            serverPlayer.openMenu(crucible, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForPart(pos, pos, state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
                crucible.dropContents();
            }
            removeDummyBlocks(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.CRUCIBLE.get());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, com.yellowyotu.hbmneoforge.ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::serverTick);
    }

    private static void removeDummyBlocks(Level level, BlockPos controller) {
        REMOVING.set(true);
        try {
            for (BlockPos pos : getDummyPositions(controller)) {
                if (level.getBlockState(pos).is(ModBlocks.CRUCIBLE_DUMMY.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        } finally {
            REMOVING.set(false);
        }
    }

    public static boolean isRemoving() {
        return REMOVING.get();
    }

    public static List<BlockPos> getAllPositions(BlockPos controller) {
        List<BlockPos> result = new ArrayList<>(18);
        result.add(controller);
        result.addAll(getDummyPositions(controller));
        return List.copyOf(result);
    }

    public static List<BlockPos> getDummyPositions(BlockPos controller) {
        List<BlockPos> result = new ArrayList<>(17);
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = controller.offset(x, y, z);
                    if (pos.equals(controller)) {
                        continue;
                    }
                    result.add(pos);
                }
            }
        }
        return List.copyOf(result);
    }

    public static boolean containsPosition(BlockPos controller, BlockPos pos) {
        int dx = pos.getX() - controller.getX();
        int dy = pos.getY() - controller.getY();
        int dz = pos.getZ() - controller.getZ();
        return Math.abs(dx) <= 1 && dy >= 0 && dy <= 1 && Math.abs(dz) <= 1;
    }

    public static VoxelShape shapeForPart(BlockPos controller, BlockPos part, Direction facing) {
        int dx = part.getX() - controller.getX();
        int dy = part.getY() - controller.getY();
        int dz = part.getZ() - controller.getZ();
        VoxelShape shape = Shapes.empty();
        shape = addBox(shape, dx, dy, dz, facing, -1.5, 0.0, -1.5, 1.5, 0.5, 1.5);
        shape = addBox(shape, dx, dy, dz, facing, -1.25, 0.5, -1.25, 1.25, 1.5, -1.0);
        shape = addBox(shape, dx, dy, dz, facing, -1.25, 0.5, -1.25, -1.0, 1.5, 1.25);
        shape = addBox(shape, dx, dy, dz, facing, -1.25, 0.5, 1.0, 1.25, 1.5, 1.25);
        shape = addBox(shape, dx, dy, dz, facing, 1.0, 0.5, -1.25, 1.25, 1.5, 1.25);
        return shape.isEmpty() ? Shapes.empty() : shape.optimize();
    }

    private static VoxelShape addBox(VoxelShape current, int dx, int dy, int dz, Direction facing,
                                     double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double[] rotated = rotate(minX, minZ, maxX, maxZ, facing);
        double cellMinX = dx - 0.5;
        double cellMaxX = dx + 0.5;
        double cellMinY = dy;
        double cellMaxY = dy + 1.0;
        double cellMinZ = dz - 0.5;
        double cellMaxZ = dz + 0.5;
        double ix0 = Math.max(rotated[0], cellMinX);
        double iy0 = Math.max(minY, cellMinY);
        double iz0 = Math.max(rotated[1], cellMinZ);
        double ix1 = Math.min(rotated[2], cellMaxX);
        double iy1 = Math.min(maxY, cellMaxY);
        double iz1 = Math.min(rotated[3], cellMaxZ);
        if (ix0 >= ix1 || iy0 >= iy1 || iz0 >= iz1) {
            return current;
        }
        VoxelShape local = Block.box(
                (ix0 - cellMinX) * 16.0,
                (iy0 - cellMinY) * 16.0,
                (iz0 - cellMinZ) * 16.0,
                (ix1 - cellMinX) * 16.0,
                (iy1 - cellMinY) * 16.0,
                (iz1 - cellMinZ) * 16.0);
        return Shapes.or(current, local);
    }

    private static double[] rotate(double minX, double minZ, double maxX, double maxZ, Direction facing) {
        return switch (facing) {
            case EAST -> new double[] {-maxZ, minX, -minZ, maxX};
            case SOUTH -> new double[] {-maxX, -maxZ, -minX, -minZ};
            case WEST -> new double[] {minZ, -maxX, maxZ, -minX};
            default -> new double[] {minX, minZ, maxX, maxZ};
        };
    }
}
