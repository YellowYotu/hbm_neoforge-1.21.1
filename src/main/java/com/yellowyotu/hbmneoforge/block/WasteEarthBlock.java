package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.radiation.RadiationConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class WasteEarthBlock extends Block {

    public static final MapCodec<WasteEarthBlock> CODEC = simpleCodec(WasteEarthBlock::new);

    public WasteEarthBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (RadiationConfig.CLEANUP_DEAD_DIRT.get() || (level.getRawBrightness(abovePos, 0) < 4 && aboveState.getLightBlock(level, abovePos) > 2)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
    }
}
