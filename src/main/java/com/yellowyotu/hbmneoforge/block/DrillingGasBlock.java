package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DrillingGasBlock extends Block {
    public enum Hazard {
        RADON,
        ASBESTOS
    }

    private final Hazard hazard;

    public DrillingGasBlock(Properties properties, Hazard hazard) {
        super(properties);
        this.hazard = hazard;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (hazard == Hazard.RADON) {
            RadiationSystem.addRadiation(living, 0.5F);
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, true, false));
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(hazard == Hazard.RADON ? 20 : 10) == 0) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
}
