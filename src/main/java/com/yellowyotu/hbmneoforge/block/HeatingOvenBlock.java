package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.HeatingOvenBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class HeatingOvenBlock extends AbstractHeaterBlock {
    public static final MapCodec<HeatingOvenBlock> CODEC = simpleCodec(HeatingOvenBlock::new);
    public HeatingOvenBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends HeatingOvenBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new HeatingOvenBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.HEATING_OVEN.get(), HeatingOvenBlockEntity::serverTick); }
}
