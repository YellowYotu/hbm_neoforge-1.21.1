package com.yellowyotu.hbmneoforge.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HeatUtil {
    private HeatUtil() {
    }

    public static int pullHeatFromBelow(Level level, BlockPos consumerPos, int requested) {
        if (requested <= 0) {
            return 0;
        }
        BlockEntity below = level.getBlockEntity(consumerPos.below());
        if (!(below instanceof HeatSource source)) {
            return 0;
        }
        return source.extractHeat(requested);
    }
}
