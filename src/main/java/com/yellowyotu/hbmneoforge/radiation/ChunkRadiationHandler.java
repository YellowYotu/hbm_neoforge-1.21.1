package com.yellowyotu.hbmneoforge.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public abstract class ChunkRadiationHandler {

    public abstract void updateSystem(MinecraftServer server);

    public abstract float getRadiation(ServerLevel level, BlockPos pos);

    public abstract void setRadiation(ServerLevel level, BlockPos pos, float radiation);

    public void incrementRadiation(ServerLevel level, BlockPos pos, float radiation) {
        setRadiation(level, pos, getRadiation(level, pos) + radiation);
    }

    public void decrementRadiation(ServerLevel level, BlockPos pos, float radiation) {
        setRadiation(level, pos, Math.max(getRadiation(level, pos) - radiation, 0.0F));
    }

    public abstract void clearSystem(ServerLevel level);

    public void handleWorldDestruction(MinecraftServer server) {
    }
}
