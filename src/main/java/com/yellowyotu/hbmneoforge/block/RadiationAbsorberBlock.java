package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RadiationAbsorberBlock extends Block {

    public static final MapCodec<RadiationAbsorberBlock> CODEC = simpleCodec(RadiationAbsorberBlock::new);

    public RadiationAbsorberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, RadiationValues.BASIC_ABSORBER_TICK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        absorb(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        absorb(level, pos);
    }

    private void absorb(ServerLevel level, BlockPos pos) {
        ChunkRadiationManager.decrementRadiation(level, pos, RadiationValues.BASIC_ABSORBER_AMOUNT_PER_TICK);
        level.scheduleTick(pos, this, RadiationValues.BASIC_ABSORBER_TICK_INTERVAL);
    }
}
