package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WasteLeavesBlock extends Block {

    public static final MapCodec<WasteLeavesBlock> CODEC = simpleCodec(WasteLeavesBlock::new);

    public WasteLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) != 0) {
            return;
        }

        boolean spawnFallingLayer = level.isEmptyBlock(pos.below());
        level.removeBlock(pos, false);

        if (spawnFallingLayer) {
            FallingBlockEntity leaves = FallingBlockEntity.fall(level, pos, ModBlocks.LEAVES_LAYER.get().defaultBlockState());
            leaves.time = 2;
            leaves.disableDrop();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(7) == 0 && level.isEmptyBlock(pos.below())) {
            level.addParticle(ModParticles.DEAD_LEAF.get(), pos.getX() + random.nextDouble(), pos.getY() - 0.05D, pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }
}
