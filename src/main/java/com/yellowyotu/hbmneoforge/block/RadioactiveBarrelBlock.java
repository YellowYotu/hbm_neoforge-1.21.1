package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RadioactiveBarrelBlock extends RadioactiveSourceBlock {

    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public RadioactiveBarrelBlock(Properties properties, float radiationPerSecond) {
        super(properties, radiationPerSecond);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        ChunkRadiationManager.incrementRadiation(level, pos, 35.0F);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 18.0F, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.25D + random.nextDouble() * 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.25D + random.nextDouble() * 0.5D, 0.0D, 0.0D, 0.0D);
    }
}
