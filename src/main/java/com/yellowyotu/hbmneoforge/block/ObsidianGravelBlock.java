package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ObsidianGravelBlock extends FallingBlock {

    public static final MapCodec<ObsidianGravelBlock> CODEC = simpleCodec(ObsidianGravelBlock::new);

    public ObsidianGravelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0x241A2B;
    }
}
