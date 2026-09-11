package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.blockentity.FireboxBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class FireboxBlock extends AbstractHeaterBlock {
    public static final MapCodec<FireboxBlock> CODEC = simpleCodec(FireboxBlock::new);
    public FireboxBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends FireboxBlock> codec() { return CODEC; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FireboxBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.FIREBOX.get(), FireboxBlockEntity::serverTick); }
}
