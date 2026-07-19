package com.yellowyotu.hbmneoforge.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface RadiationShielding {

    default boolean isRadiationShielding(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }
}
