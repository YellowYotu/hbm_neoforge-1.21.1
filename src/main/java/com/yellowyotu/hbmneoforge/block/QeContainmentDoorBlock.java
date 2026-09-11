package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.blockentity.DoorAccessMode;
import com.yellowyotu.hbmneoforge.blockentity.QeContainmentDoorBlockEntity;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationShielding;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class QeContainmentDoorBlock extends BaseEntityBlock implements RadiationShielding {
    public static final MapCodec<QeContainmentDoorBlock> CODEC = simpleCodec(QeContainmentDoorBlock::new);
    public static final DirectionProperty FACING = net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty FRAME = IntegerProperty.create("frame", 0, 160);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);
    public static final int CORE_PART = 1;

    private static boolean removing;

    public QeContainmentDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(FRAME, 0)
                .setValue(PART, CORE_PART));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos core = context.getClickedPos();
        Direction right = facing.getClockWise();
        Level level = context.getLevel();
        for (int y = 0; y < 3; y++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos target = core.above(y).relative(right, w);
                if (!level.getBlockState(target).canBeReplaced(context)) {
                    return null;
                }
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        for (int y = 0; y < 3; y++) {
            for (int w = -1; w <= 1; w++) {
                int part = y * 3 + (w + 1);
                BlockPos target = pos.above(y).relative(right, w);
                BlockState partState = state.setValue(PART, part);
                level.setBlock(target, partState, 3);
                if (!level.isClientSide()) {
                    ChunkRadiationManager.markSectionForRebuild(level, target);
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos core = getCorePos(state, pos);
        BlockState coreState = level.getBlockState(core);
        if (!coreState.is(this)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(core) instanceof QeContainmentDoorBlockEntity door) {
            if (!door.getAccessMode().allowsHand() || door.isMoving()) {
                return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide()) {
            setOpen(level, core, coreState, !coreState.getValue(OPEN));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.SCREWDRIVER.get()) || !player.isShiftKeyDown()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        BlockPos core = getCorePos(state, pos);
        if (level.getBlockEntity(core) instanceof QeContainmentDoorBlockEntity door) {
            if (!level.isClientSide()) {
                DoorAccessMode mode = door.cycleAccessMode();
                player.displayClientMessage(Component.translatable("message.hbm_neoforge.door_mode", mode.displayName()), true);
                updateRedstone(level, core, level.getBlockState(core), door);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (level.isClientSide()) {
            return;
        }
        BlockPos core = getCorePos(state, pos);
        BlockState coreState = level.getBlockState(core);
        if (coreState.is(this) && level.getBlockEntity(core) instanceof QeContainmentDoorBlockEntity door) {
            updateRedstone(level, core, coreState, door);
        }
    }

    private void updateRedstone(Level level, BlockPos core, BlockState coreState, QeContainmentDoorBlockEntity door) {
        boolean powered = isAnyPartPowered(level, core, coreState.getValue(FACING));
        if (powered == door.isPowered()) {
            return;
        }
        door.setPowered(powered);
        if (door.getAccessMode().allowsRedstone() && !door.isMoving()) {
            setOpen(level, core, coreState, powered);
        }
    }

    private static boolean isAnyPartPowered(Level level, BlockPos core, Direction facing) {
        Direction right = facing.getClockWise();
        for (int y = 0; y < 3; y++) {
            for (int w = -1; w <= 1; w++) {
                if (level.hasNeighborSignal(core.above(y).relative(right, w))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setOpen(Level level, BlockPos core, BlockState coreState, boolean open) {
        if (!coreState.is(ModBlocks.QE_CONTAINMENT.get()) || coreState.getValue(PART) != CORE_PART) {
            return;
        }
        level.setBlock(core, coreState.setValue(OPEN, open), 3);
        ChunkRadiationManager.markSectionForRebuild(level, core);
    }

    public static void updateFrame(Level level, BlockPos core, BlockState coreState, int frame) {
        BlockState latest = level.getBlockState(core);
        if (!latest.is(ModBlocks.QE_CONTAINMENT.get()) || latest.getValue(PART) != CORE_PART) {
            return;
        }
        if (latest.getValue(FRAME) != frame) {
            level.setBlock(core, latest.setValue(FRAME, frame), 2);
        }
        if (frame == 0 || frame == QeContainmentDoorBlockEntity.OPEN_TIME) {
            ChunkRadiationManager.markSectionForRebuild(level, core);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !removing) {
            BlockPos core = getCorePos(state, pos);
            stopMoveSound(level, core);
            Direction facing = state.getValue(FACING);
            Direction right = facing.getClockWise();
            removing = true;
            try {
                for (int y = 0; y < 3; y++) {
                    for (int w = -1; w <= 1; w++) {
                        BlockPos target = core.above(y).relative(right, w);
                        if (!target.equals(pos) && level.getBlockState(target).is(this)) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 35);
                        }
                        if (!level.isClientSide()) {
                            ChunkRadiationManager.markSectionForRebuild(level, target);
                        }
                    }
                }
            } finally {
                removing = false;
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModItems.QE_CONTAINMENT.get()));
        return drops;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state, level, pos);
    }

    private static VoxelShape shapeFor(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        int row = state.getValue(PART) / 3;
        BlockPos core = getCorePos(state, pos);
        BlockState coreState = level.getBlockState(core);
        boolean open = coreState.is(ModBlocks.QE_CONTAINMENT.get()) && coreState.getValue(FRAME) >= QeContainmentDoorBlockEntity.OPEN_TIME;
        if (open) {
            if (row == 1) {
                return net.minecraft.world.phys.shapes.Shapes.empty();
            }
            if (row == 2) {
                return facing.getAxis() == Direction.Axis.Z
                        ? box(0, 8, 8, 16, 16, 16)
                        : box(8, 8, 0, 16, 16, 16);
            }
            return facing.getAxis() == Direction.Axis.Z
                    ? box(0, 0, 8, 16, 1.6, 16)
                    : box(8, 0, 0, 16, 1.6, 16);
        }
        return facing.getAxis() == Direction.Axis.Z
                ? box(0, 0, 8, 16, 16, 16)
                : box(8, 0, 0, 16, 16, 16);
    }

    @Override
    public boolean isRadiationShielding(BlockState state, LevelReader level, BlockPos pos) {
        if (level == null) {
            return true;
        }
        BlockState coreState = level.getBlockState(getCorePos(state, pos));
        return coreState.is(ModBlocks.QE_CONTAINMENT.get()) && coreState.getValue(FRAME) == 0;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == CORE_PART ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == CORE_PART ? new QeContainmentDoorBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || state.getValue(PART) != CORE_PART) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.QE_CONTAINMENT.get(), QeContainmentDoorBlockEntity::tick);
    }


    public static void stopMoveSound(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
                BuiltInRegistries.SOUND_EVENT.getKey(ModSounds.QE_CONTAINMENT_MOVE.get()),
                SoundSource.BLOCKS);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 4096.0D) {
                player.connection.send(packet);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, FRAME, PART);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    public static BlockPos getCorePos(BlockState state, BlockPos pos) {
        int part = state.getValue(PART);
        int row = part / 3;
        int col = part % 3;
        Direction right = state.getValue(FACING).getClockWise();
        return pos.below(row).relative(right, -(col - 1));
    }
}
