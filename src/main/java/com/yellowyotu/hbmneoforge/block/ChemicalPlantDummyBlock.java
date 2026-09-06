package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantDummyBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ChemicalPlantDummyBlock extends BaseEntityBlock {
    public static final MapCodec<ChemicalPlantDummyBlock> CODEC = simpleCodec(ChemicalPlantDummyBlock::new);

    public ChemicalPlantDummyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ChemicalPlantDummyBlockEntity dummy) {
            BlockPos controller = dummy.getController();
            if (controller != null && !level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(controller) instanceof ChemicalPlantBlockEntity machine) {
                serverPlayer.openMenu(machine, buffer -> buffer.writeBlockPos(controller));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide() && level.getBlockEntity(pos) instanceof ChemicalPlantDummyBlockEntity dummy) {
            BlockPos controller = dummy.getController();
            if (controller != null && level.getBlockState(controller).is(com.yellowyotu.hbmneoforge.ModBlocks.CHEMICAL_PLANT.get())) {
                level.destroyBlock(controller, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(com.yellowyotu.hbmneoforge.ModItems.CHEMICAL_PLANT.get());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChemicalPlantDummyBlockEntity(pos, state);
    }
}
