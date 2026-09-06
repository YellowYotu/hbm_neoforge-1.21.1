package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerDummyBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WoodBurnerDummyBlock extends BaseEntityBlock {
    public static final MapCodec<WoodBurnerDummyBlock> CODEC = simpleCodec(WoodBurnerDummyBlock::new);
    public WoodBurnerDummyBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WoodBurnerDummyBlockEntity(pos, state); }
    @Override public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) { return new ItemStack(ModItems.WOOD_BURNER.get()); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof WoodBurnerDummyBlockEntity dummy) {
            BlockPos controller = dummy.getController();
            if (controller != null && !level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(controller) instanceof WoodBurnerBlockEntity burner) {
                serverPlayer.openMenu(burner, buffer -> buffer.writeBlockPos(controller));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof WoodBurnerDummyBlockEntity dummy) {
            BlockPos controller = dummy.getController();
            if (controller != null && level.getBlockState(controller).is(ModBlocks.WOOD_BURNER.get())) { level.destroyBlock(controller, true); }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
