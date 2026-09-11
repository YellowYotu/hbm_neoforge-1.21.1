package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifier;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FluidStorageBlock extends BaseEntityBlock {
    public enum StorageKind { PLASTIC_BARREL, CORRODED_BARREL, STEEL_BARREL, ANTIMATTER_BARREL, TANK }
    public static final MapCodec<FluidStorageBlock> CODEC = simpleCodec(properties -> new FluidStorageBlock(properties, 16_000, StorageKind.STEEL_BARREL));
    private final int capacity;
    private final StorageKind kind;

    public FluidStorageBlock(Properties properties, int capacity) { this(properties, capacity, StorageKind.STEEL_BARREL); }
    public FluidStorageBlock(Properties properties, int capacity, StorageKind kind) { super(properties); this.capacity = capacity; this.kind = kind; }
    public int getCapacity() { return capacity; }
    public StorageKind getKind() { return kind; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            FluidPipeBlock.refreshConnections(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            FluidPipeBlock.refreshConnections(level, pos);
        }
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FluidStorageBlockEntity tank) {
            NTMFluidType selected = null;
            if (stack.getItem() instanceof ItemFluidIdentifier identifier) {
                selected = identifier.getFluidType();
            } else if (stack.getItem() instanceof ItemFluidIdentifierMulti) {
                selected = ItemFluidIdentifierMulti.getType(stack, true);
            }
            if (selected != null) {
                if (!level.isClientSide()) {
                    tank.setFluidType(selected);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FluidStorageBlockEntity tank) {
            serverPlayer.openMenu(tank, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FluidStorageBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FLUID_STORAGE.get(), FluidStorageBlockEntity::serverTick); }
}
