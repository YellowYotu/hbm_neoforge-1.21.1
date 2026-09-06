package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID)
public final class ChunkRadiationManager {

    private static final ChunkRadiationHandler SIMPLE_HANDLER = new ChunkRadiationHandlerSimple();
    private static final ChunkRadiationHandlerNT POCKET_HANDLER = new ChunkRadiationHandlerNT();
    private static int simpleUpdateTimer;
    private static int migrationTimer;

    private ChunkRadiationManager() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        migrationTimer++;
        if (migrationTimer >= 20) {
            migrationTimer = 0;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                RadiationMigration.migrateLegacySources(level);
            }
        }

        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get()) {
            return;
        }

        if (RadiationConfig.ENABLE_POCKET_RADIATION.get()) {
            POCKET_HANDLER.updateSystem(event.getServer());
        } else {
            simpleUpdateTimer++;
            if (simpleUpdateTimer >= 20) {
                simpleUpdateTimer = 0;
                SIMPLE_HANDLER.updateSystem(event.getServer());
            }
        }

        if (RadiationConfig.ENABLE_WORLD_RADIATION_EFFECTS.get()) {
            getActiveHandler().handleWorldDestruction(event.getServer());
        }
    }

    public static float getRadiation(Level level, BlockPos pos) {
        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get() || !(level instanceof ServerLevel serverLevel)) {
            return 0.0F;
        }
        return getActiveHandler().getRadiation(serverLevel, pos);
    }

    public static void setRadiation(Level level, BlockPos pos, float radiation) {
        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getActiveHandler().setRadiation(serverLevel, pos, radiation);
    }

    public static void incrementRadiation(Level level, BlockPos pos, float radiation) {
        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getActiveHandler().incrementRadiation(serverLevel, pos, radiation);
    }

    public static void decrementRadiation(Level level, BlockPos pos, float radiation) {
        if (!RadiationConfig.ENABLE_CHUNK_RADIATION.get() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        getActiveHandler().decrementRadiation(serverLevel, pos, radiation);
    }

    public static void clearSystem(ServerLevel level) {
        getActiveHandler().clearSystem(level);
    }

    public static void markSectionForRebuild(Level level, BlockPos pos) {
        if (!RadiationConfig.ENABLE_POCKET_RADIATION.get()) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            POCKET_HANDLER.markSectionForRebuild(serverLevel, pos);
        }
    }

    static RadiationLevelData getData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RadiationLevelData.FACTORY, RadiationLevelData.dataName());
    }

    private static ChunkRadiationHandler getActiveHandler() {
        return RadiationConfig.ENABLE_POCKET_RADIATION.get() ? POCKET_HANDLER : SIMPLE_HANDLER;
    }
}
