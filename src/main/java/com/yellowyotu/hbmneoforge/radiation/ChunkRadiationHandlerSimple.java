package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModBlocks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Most basic implementation of a chunk radiation system: Each chunk has a radiation value which spreads out to its neighbors.
 * @author hbm
 */
public final class ChunkRadiationHandlerSimple extends ChunkRadiationHandler {

    private static final float MAX_RADIATION = 100_000.0F;
    private static final int WORLD_DESTRUCTION_CHUNKS = 5;
    private static final int WORLD_DESTRUCTION_PASSES = 10;
    private static final float WORLD_DESTRUCTION_THRESHOLD = 10.0F;

    @Override
    public float getRadiation(ServerLevel level, BlockPos pos) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        Float radiation = data.simpleRadiation().get(new ChunkPos(pos).toLong());
        return radiation == null ? 0.0F : Mth.clamp(radiation, 0.0F, MAX_RADIATION);
    }

    @Override
    public void setRadiation(ServerLevel level, BlockPos pos, float radiation) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        data.simpleRadiation().put(new ChunkPos(pos).toLong(), Mth.clamp(radiation, 0.0F, MAX_RADIATION));
        data.setDirty();
    }

    @Override
    public void updateSystem(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            RadiationLevelData data = ChunkRadiationManager.getData(level);
            Map<Long, Float> radiation = data.simpleRadiation();
            Map<Long, Float> buffer = new HashMap<>(radiation);
            radiation.clear();

            for (Map.Entry<Long, Float> entry : buffer.entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey());
                if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                    radiation.put(entry.getKey(), entry.getValue());
                    continue;
                }
                if (entry.getValue() == 0.0F) {
                    continue;
                }

                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        int type = Math.abs(offsetX) + Math.abs(offsetZ);
                        float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
                        long targetPos = ChunkPos.asLong(chunkPos.x + offsetX, chunkPos.z + offsetZ);

                        if (buffer.containsKey(targetPos)) {
                            float currentRadiation = radiation.getOrDefault(targetPos, 0.0F);
                            float newRadiation = currentRadiation + entry.getValue() * percent;
                            newRadiation = Mth.clamp(newRadiation * 0.99F - 0.05F, 0.0F, MAX_RADIATION);
                            radiation.put(targetPos, newRadiation);
                        } else {
                            radiation.put(targetPos, entry.getValue() * percent);
                        }
                    }
                }
            }

            data.setDirty();
        }
    }

    @Override
    public void handleWorldDestruction(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            RadiationLevelData data = ChunkRadiationManager.getData(level);
            List<Map.Entry<Long, Float>> entries = data.simpleRadiation().entrySet().stream().filter(entry -> {
                ChunkPos chunkPos = new ChunkPos(entry.getKey());
                return level.hasChunk(chunkPos.x, chunkPos.z);
            }).toList();

            if (entries.isEmpty()) {
                continue;
            }

            RandomSource random = level.random;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int chunkIndex = 0; chunkIndex < WORLD_DESTRUCTION_CHUNKS; chunkIndex++) {
                Map.Entry<Long, Float> randomEntry = entries.get(random.nextInt(entries.size()));
                ChunkPos chunkPos = new ChunkPos(randomEntry.getKey());

                for (int pass = 0; pass < WORLD_DESTRUCTION_PASSES; pass++) {
                    if (randomEntry.getValue() < WORLD_DESTRUCTION_THRESHOLD) {
                        continue;
                    }
                    if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                        continue;
                    }

                    for (int localX = 0; localX < 16; localX++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            if (random.nextInt(3) != 0) {
                                continue;
                            }

                            int x = chunkPos.getMinBlockX() + localX;
                            int z = chunkPos.getMinBlockZ() + localZ;
                            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - random.nextInt(2);
                            if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
                                continue;
                            }

                            cursor.set(x, y, z);
                            BlockState state = level.getBlockState(cursor);

                            if (state.is(Blocks.GRASS_BLOCK)) {
                                level.setBlock(cursor, ModBlocks.WASTE_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
                            } else if (isOriginalTallGrass(state)) {
                                level.removeBlock(cursor, false);
                            } else if (state.is(BlockTags.LEAVES) && !state.is(ModBlocks.WASTE_LEAVES.get())) {
                                if (random.nextInt(7) <= 5) {
                                    level.setBlock(cursor, ModBlocks.WASTE_LEAVES.get().defaultBlockState(), Block.UPDATE_ALL);
                                } else {
                                    level.removeBlock(cursor, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clearSystem(ServerLevel level) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        data.simpleRadiation().clear();
        data.setDirty();
    }

    private static boolean isOriginalTallGrass(BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH);
    }
}
