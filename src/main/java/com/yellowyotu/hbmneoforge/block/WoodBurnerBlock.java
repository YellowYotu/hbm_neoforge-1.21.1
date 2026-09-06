package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerDummyBlockEntity;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class WoodBurnerBlock extends BaseEntityBlock {
    public static final MapCodec<WoodBurnerBlock> CODEC = simpleCodec(WoodBurnerBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public WoodBurnerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return canPlaceStructure(context.getLevel(), context.getClickedPos(), state) ? state : null;
    }

    private static boolean canPlaceStructure(Level level, BlockPos core, BlockState state) {
        Direction right = state.getValue(FACING).getClockWise();
        for (int y = 0; y < 2; y++) {
            for (int forward = 0; forward < 2; forward++) {
                for (int side = 0; side < 2; side++) {
                    BlockPos pos = core.relative(state.getValue(FACING), forward).relative(right, side).above(y);
                    if (!pos.equals(core) && !level.getBlockState(pos).canBeReplaced()) { return false; }
                }
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) { return; }
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        for (int y = 0; y < 2; y++) {
            for (int forward = 0; forward < 2; forward++) {
                for (int side = 0; side < 2; side++) {
                    BlockPos target = pos.relative(facing, forward).relative(right, side).above(y);
                    if (target.equals(pos)) { continue; }
                    level.setBlock(target, ModBlocks.WOOD_BURNER_DUMMY.get().defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(target) instanceof WoodBurnerDummyBlockEntity dummy) { dummy.setController(pos); }
                }
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof WoodBurnerBlockEntity burner) { burner.dropContents(); }
            BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 2, 2)).map(BlockPos::immutable).toList().forEach(target -> {
                if (level.getBlockEntity(target) instanceof WoodBurnerDummyBlockEntity dummy && pos.equals(dummy.getController())) { level.removeBlock(target, false); }
            });
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof WoodBurnerBlockEntity burner) {
            serverPlayer.openMenu(burner, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WoodBurnerBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return createTickerHelper(type, ModBlockEntities.WOOD_BURNER.get(), WoodBurnerBlockEntity::serverTick); }
}
