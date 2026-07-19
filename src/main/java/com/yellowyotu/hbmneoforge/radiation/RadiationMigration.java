package com.yellowyotu.hbmneoforge.radiation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

final class RadiationMigration {

    private RadiationMigration() {
    }

    static void migrateLegacySources(ServerLevel level) {
        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get()) {
            return;
        }

        RadiationLevelData radiationData = ChunkRadiationManager.getData(level);
        if (radiationData.isLegacySourcesMigrated()) {
            return;
        }

        LegacyRadiationWorldData legacyData = level.getDataStorage().computeIfAbsent(LegacyRadiationWorldData.FACTORY, LegacyRadiationWorldData.dataName());
        for (LegacyRadiationWorldData.Source source : legacyData.getSources()) {
            ChunkRadiationManager.incrementRadiation(level, source.pos(), source.strength());
            BlockState state = level.getBlockState(source.pos());
            if (state.getBlock() instanceof RadiationEmitter) {
                level.scheduleTick(source.pos(), state.getBlock(), 1);
            }
        }

        radiationData.setLegacySourcesMigrated();
    }
}
