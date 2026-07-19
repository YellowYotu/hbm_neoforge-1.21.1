package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationEmitter;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FalloutBlock extends FallingBlock implements RadiationEmitter {

    public static final MapCodec<FalloutBlock> CODEC = simpleCodec(FalloutBlock::new);

    public FalloutBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public float getRadiationPerSecond() {
        return RadiationValues.FALLOUT_BLOCK_SOURCE;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0x6A6A6A;
    }

    @Override
    protected int getDelayAfterPlace() {
        return 20;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChunkRadiationManager.incrementRadiation(level, pos, RadiationValues.FALLOUT_BLOCK_SOURCE);
        level.scheduleTick(pos, this, 20);
        super.tick(state, level, pos, random);
    }
}
