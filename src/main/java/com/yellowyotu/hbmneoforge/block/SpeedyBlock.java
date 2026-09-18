package com.yellowyotu.hbmneoforge.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SpeedyBlock extends Block {
    private final double speedMultiplier;

    public SpeedyBlock(Properties properties, double speedMultiplier) {
        super(properties);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x * speedMultiplier, movement.y, movement.z * speedMultiplier);
        super.stepOn(level, pos, state, entity);
    }
}
