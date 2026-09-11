package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.FoundryCastingBlockEntity;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FoundryCastingBlock extends BaseEntityBlock {
    private static final VoxelShape MOLD = Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 2, 0, 16, 8, 2), Block.box(0, 2, 14, 16, 8, 16), Block.box(0, 2, 2, 2, 8, 14), Block.box(14, 2, 2, 16, 8, 14));
    private static final VoxelShape BASIN = Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 2, 0, 16, 16, 2), Block.box(0, 2, 14, 16, 16, 16), Block.box(0, 2, 2, 2, 16, 14), Block.box(14, 2, 2, 16, 16, 14));
    private final boolean basin;

    public FoundryCastingBlock(Properties properties, boolean basin) {
        super(properties);
        this.basin = basin;
    }

    public boolean isBasin() {
        return basin;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return basin ? BASIN : MOLD;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack output = casting.getInventory().getStackInSlot(1);
            if (!output.isEmpty()) {
                ItemStack give = output.copy();
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
                casting.getInventory().setStackInSlot(1, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        net.minecraft.resources.ResourceLocation heldId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (heldId != null && heldId.getPath().equals("screwdriver") && casting.getAmount() == 0) {
            if (!level.isClientSide()) {
                ItemStack mold = casting.getInventory().getStackInSlot(0);
                if (!mold.isEmpty()) {
                    ItemStack give = mold.copy();
                    if (!player.getInventory().add(give)) {
                        player.drop(give, false);
                    }
                    casting.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!FoundryMaterialRegistry.isMold(stack, basin)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide()) {
            ItemStack old = casting.getInventory().getStackInSlot(0);
            if (!old.isEmpty()) {
                if (casting.getAmount() > 0) {
                    return ItemInteractionResult.FAIL;
                }
                ItemStack give = old.copy();
                if (!player.getInventory().add(give)) {
                    player.drop(give, false);
                }
            }
            ItemStack mold = stack.copyWithCount(1);
            casting.getInventory().setStackInSlot(0, mold);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }



    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting) {
            for (int slot = 0; slot < casting.getInventory().getSlots(); slot++) {
                ItemStack stack = casting.getInventory().getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(basin ? ModItems.FOUNDRY_BASIN.get() : ModItems.FOUNDRY_MOLD.get());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FoundryCastingBlockEntity entity = new FoundryCastingBlockEntity(pos, state);
        entity.setBasin(basin);
        return entity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FOUNDRY_CASTING.get(), FoundryCastingBlockEntity::serverTick);
    }
}
