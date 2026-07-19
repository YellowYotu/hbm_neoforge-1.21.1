package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.RadiationEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RadioactiveSourceBlock extends Block implements RadiationEmitter {

    private static final int RADIATION_TICK_INTERVAL = 20;
    private final float radiationPerSecond;

    public RadioactiveSourceBlock(Properties properties, float radiationPerSecond) {
        super(properties);
        this.radiationPerSecond = radiationPerSecond;
    }

    @Override
    public float getRadiationPerSecond() {
        return radiationPerSecond;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, RADIATION_TICK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChunkRadiationManager.incrementRadiation(level, pos, radiationPerSecond);
        level.scheduleTick(pos, this, RADIATION_TICK_INTERVAL);
    }
}
