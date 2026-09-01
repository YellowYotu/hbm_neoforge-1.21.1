package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageDummyBlockEntity;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class FluidStorageDummyBlock extends BaseEntityBlock {
    public static final MapCodec<FluidStorageDummyBlock> CODEC = simpleCodec(FluidStorageDummyBlock::new);
    public FluidStorageDummyBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FluidStorageDummyBlockEntity(pos, state); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FluidStorageDummyBlockEntity dummy) {
            NTMFluidType selected = null;
            if (stack.getItem() instanceof ItemFluidIdentifier identifier) { selected = identifier.getFluidType(); }
            if (stack.getItem() instanceof ItemFluidIdentifierMulti) { selected = ItemFluidIdentifierMulti.getType(stack, true); }
            if (selected != null) {
                if (!level.isClientSide()) { dummy.setFluidType(selected); }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof FluidStorageDummyBlockEntity dummy) {
            BlockPos core = dummy.getController();
            if (core != null && level.getBlockEntity(core) instanceof com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity tank) { serverPlayer.openMenu(tank, buffer -> buffer.writeBlockPos(core)); }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof FluidStorageDummyBlockEntity dummy) {
            BlockPos core = dummy.getController();
            if (core != null && level.getBlockState(core).getBlock() instanceof FluidTankMultiblockBlock) {
                return new ItemStack(ModBlocks.MACHINE_FLUID_TANK.get());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override public net.minecraft.world.level.block.state.BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FluidStorageDummyBlockEntity dummy) {
            BlockPos core = dummy.getController();
            if (core != null && level.getBlockState(core).getBlock() instanceof FluidTankMultiblockBlock tank) { tank.destroyStructure(level, core, !player.isCreative()); }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
