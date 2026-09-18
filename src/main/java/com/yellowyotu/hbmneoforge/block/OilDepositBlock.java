package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class OilDepositBlock extends Block {
    public OilDepositBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(ModBlocks.ORE_OIL_EMPTY.get())) {
            level.setBlock(pos, ModBlocks.ORE_OIL_EMPTY.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(below, ModBlocks.ORE_OIL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        super.neighborChanged(state, level, pos, block, fromPos, moving);
    }
}
