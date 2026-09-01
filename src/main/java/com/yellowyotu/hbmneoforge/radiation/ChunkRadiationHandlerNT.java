package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.SlidingSealDoorBlock;
import com.yellowyotu.hbmneoforge.radiation.RadiationShielding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Pocket-based radiation system ported from HBM NTM Community Edition's RadiationSystemNT.
 * Non-shielding blocks are flood-filled into independent pockets inside each 16x16x16 section.
 * Radiation exchanges only through matching open cells on neighboring section faces.
 */
public final class ChunkRadiationHandlerNT extends ChunkRadiationHandler {

    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final float MAX_RADIATION = 1_000_000.0F;
    private static final float SPREAD_FACTOR = 0.1F;
    private static final float RETENTION_FACTOR = 0.95F;
    private static final float PASSIVE_DECAY = 1.0F;
    private static final float MAX_BLOCK_RESISTANCE = 100.0F;
    private static final double RESISTANCE_DIVISOR = 10_000.0D;
    private static final float RAD_EPSILON = 1.0E-5F;

    private final Map<ServerLevel, DestructionTarget> destructionTargets = new java.util.HashMap<>();
    private int updateTimer;

    @Override
    public float getRadiation(ServerLevel level, BlockPos pos) {
        if (!isInsideBuildHeight(level, pos.getY()) || !level.hasChunkAt(pos)) {
            return 0.0F;
        }

        RadiationLevelData.PrismSectionData section = ChunkRadiationManager.getData(level).prismRadiation().get(sectionKey(pos));
        if (section == null) {
            return 0.0F;
        }

        return sanitize(section.radiation());
    }

    @Override
    public void setRadiation(ServerLevel level, BlockPos pos, float radiation) {
        if (!isInsideBuildHeight(level, pos.getY()) || !level.hasChunkAt(pos)) {
            return;
        }

        RadiationLevelData data = ChunkRadiationManager.getData(level);
        RadiationLevelData.PrismSectionData section = data.getOrCreatePrismSection(sectionKey(pos));
        section.setRadiation(sanitize(radiation));
        data.setDirty();
    }

    @Override
    public void updateSystem(MinecraftServer server) {
        updateTimer++;
        if (updateTimer < UPDATE_INTERVAL_TICKS) {
            return;
        }

        updateTimer = 0;

        for (ServerLevel level : server.getAllLevels()) {
            RadiationLevelData data = ChunkRadiationManager.getData(level);
            migrateLegacyRadiation(data);

            Set<Long> candidateKeys = collectCandidateSections(level, data);
            if (candidateKeys.isEmpty()) {
                continue;
            }

            List<Long> orderedKeys = candidateKeys.stream().sorted(Comparator.naturalOrder()).toList();

            for (long key : orderedKeys) {
                RadiationLevelData.PrismSectionData section = data.getOrCreatePrismSection(key);
                rebuildSection(level, key, section);
            }

            Map<Long, Float> previousRadiation = new java.util.HashMap<>();

            for (long key : orderedKeys) {
                RadiationLevelData.PrismSectionData section = data.prismRadiation().get(key);
                if (section == null) {
                    continue;
                }

                float previous = sanitize(section.radiation());
                previousRadiation.put(key, previous);
                section.setRadiation(0.0F);
            }

            for (long key : orderedKeys) {
                RadiationLevelData.PrismSectionData source = data.prismRadiation().get(key);
                if (source == null) {
                    continue;
                }

                float previous = previousRadiation.getOrDefault(key, 0.0F);
                if (previous <= RAD_EPSILON) {
                    continue;
                }

                float spread = 0.0F;

                for (Direction direction : Direction.values()) {
                    spread += spreadRadiation(level, data, key, source, previous, direction);
                }

                float remaining = (previous - spread) * RETENTION_FACTOR;
                remaining -= PASSIVE_DECAY;

                source.setRadiation(sanitize(source.radiation() + remaining));
            }

            selectDestructionTarget(level, data, orderedKeys);
            data.setDirty();
        }
    }

    @Override
    public void clearSystem(ServerLevel level) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        data.prismRadiation().clear();
        data.legacyPocketRadiation().clear();
        data.legacyPrismRadiation().clear();
        destructionTargets.remove(level);
        data.setDirty();
    }

    @Override
    public void handleWorldDestruction(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            DestructionTarget target = destructionTargets.remove(level);
            if (target == null) {
                continue;
            }

            decaySectionSurface(level, target.sectionKey());
        }
    }

    void markSectionForRebuild(ServerLevel level, BlockPos pos) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        RadiationLevelData.PrismSectionData section = data.prismRadiation().get(sectionKey(pos));

        if (section != null) {
            section.markResistanceDirty();
            data.setDirty();
        }
    }

    private static Set<Long> collectCandidateSections(ServerLevel level, RadiationLevelData data) {
        Set<Long> candidates = new HashSet<>();
        List<Long> activeKeys = new ArrayList<>();

        for (Map.Entry<Long, RadiationLevelData.PrismSectionData> entry : data.prismRadiation().entrySet()) {
            if (entry.getValue().radiation() <= RAD_EPSILON) {
                continue;
            }

            if (!isSectionLoaded(level, entry.getKey())) {
                continue;
            }

            activeKeys.add(entry.getKey());
            candidates.add(entry.getKey());
        }

        for (long key : activeKeys) {
            for (Direction direction : Direction.values()) {
                long neighborKey = offsetSection(key, direction);
                if (isSectionLoaded(level, neighborKey)) {
                    candidates.add(neighborKey);
                }
            }
        }

        return candidates;
    }

    private static float spreadRadiation(ServerLevel level, RadiationLevelData data, long sourceKey, RadiationLevelData.PrismSectionData source, float previousRadiation, Direction direction) {
        float amount = previousRadiation * SPREAD_FACTOR;

        if (amount <= 1.0F) {
            return 0.0F;
        }

        long targetKey = offsetSection(sourceKey, direction);

        if (!isSectionLoaded(level, targetKey)) {
            return amount;
        }

        RadiationLevelData.PrismSectionData target = data.getOrCreatePrismSection(targetKey);

        if (target.isResistanceDirty()) {
            rebuildSection(level, targetKey, target);
        }

        float resistance = getResistance(source, direction.getOpposite()) + getResistance(target, direction);
        double attenuation = Math.exp(-resistance / RESISTANCE_DIVISOR);
        float moved = (float) Math.min(amount * attenuation, amount);

        target.setRadiation(sanitize(target.radiation() + moved));

        return moved;
    }

    private static void rebuildSection(ServerLevel level, long key, RadiationLevelData.PrismSectionData section) {
        float[] xResistance = section.xResistance();
        float[] yResistance = section.yResistance();
        float[] zResistance = section.zResistance();

        Arrays.fill(xResistance, 0.0F);
        Arrays.fill(yResistance, 0.0F);
        Arrays.fill(zResistance, 0.0F);

        int baseX = SectionPos.sectionToBlockCoord(SectionPos.x(key));
        int baseY = SectionPos.sectionToBlockCoord(SectionPos.y(key));
        int baseZ = SectionPos.sectionToBlockCoord(SectionPos.z(key));

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    cursor.set(baseX + localX, baseY + localY, baseZ + localZ);

                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }

                    float resistance = getBlockResistance(state);

                    xResistance[localX] += resistance;
                    yResistance[localY] += resistance;
                    zResistance[localZ] += resistance;
                }
            }
        }

        section.markResistanceClean();
    }

    private static float getBlockResistance(BlockState state) {
        if (state.getBlock() instanceof SlidingSealDoorBlock && state.hasProperty(SlidingSealDoorBlock.PROGRESS)) {
            int progress = state.getValue(SlidingSealDoorBlock.PROGRESS);
            float closedAmount = 1.0F - progress / (float) SlidingSealDoorBlock.MAX_PROGRESS;
            return MAX_BLOCK_RESISTANCE * Math.max(0.0F, Math.min(closedAmount, 1.0F));
        }

        float resistance = state.getBlock().getExplosionResistance();
        if (state.getBlock() instanceof RadiationShielding shielding) {
            resistance = shielding.getRadiationResistance(state);
        }

        if (!Float.isFinite(resistance) || resistance <= 0.0F) {
            return 0.0F;
        }

        return Math.min(resistance, MAX_BLOCK_RESISTANCE);
    }

    private static float getResistance(RadiationLevelData.PrismSectionData section, Direction direction) {
        return switch (direction) {
            case EAST -> getResistanceFromArray(section.xResistance(), true);
            case WEST -> getResistanceFromArray(section.xResistance(), false);
            case UP -> getResistanceFromArray(section.yResistance(), true);
            case DOWN -> getResistanceFromArray(section.yResistance(), false);
            case SOUTH -> getResistanceFromArray(section.zResistance(), true);
            case NORTH -> getResistanceFromArray(section.zResistance(), false);
        };
    }

    private static float getResistanceFromArray(float[] resistance, boolean reverse) {
        float total = 0.0F;

        for (int i = 1; i < 16; i++) {
            int index = reverse ? 15 - i : i;
            total += resistance[index] / 15.0F * i;
        }

        return total;
    }

    private void selectDestructionTarget(ServerLevel level, RadiationLevelData data, List<Long> orderedKeys) {
        RandomSource random = level.random;
        DestructionTarget selectedTarget = null;

        for (long key : orderedKeys) {
            RadiationLevelData.PrismSectionData section = data.prismRadiation().get(key);
            if (section == null) {
                continue;
            }

            float radiation = sanitize(section.radiation());
            section.setRadiation(radiation);

            if (radiation >= 5.0F && random.nextInt(100) == 0) {
                selectedTarget = new DestructionTarget(key);
            }
        }

        if (selectedTarget != null) {
            destructionTargets.put(level, selectedTarget);
        }
    }

    private static void migrateLegacyRadiation(RadiationLevelData data) {
        if (!data.isLegacyPocketMigrated()) {
            for (Map.Entry<Long, double[]> entry : data.legacyPocketRadiation().entrySet()) {
                double maximum = 0.0D;

                for (double density : entry.getValue()) {
                    if (Double.isFinite(density) && density > maximum) {
                        maximum = density;
                    }
                }

                if (maximum <= 0.0D) {
                    continue;
                }

                RadiationLevelData.PrismSectionData section = data.getOrCreatePrismSection(entry.getKey());
                section.setRadiation(Math.max(section.radiation(), sanitize((float) maximum)));
            }

            data.setLegacyPocketMigrated();
        }

        if (!data.isLegacyPrismMigrated()) {
            for (Map.Entry<Long, RadiationLevelData.LegacyPrismChunkData> entry : data.legacyPrismRadiation().entrySet()) {
                ChunkPos chunkPos = new ChunkPos(entry.getKey());

                for (int sectionY = 0; sectionY < RadiationLevelData.LegacyPrismChunkData.SECTION_COUNT; sectionY++) {
                    float radiation = sanitize(entry.getValue().radiation[sectionY]);

                    if (radiation <= 0.0F) {
                        continue;
                    }

                    long key = SectionPos.asLong(chunkPos.x, sectionY, chunkPos.z);
                    RadiationLevelData.PrismSectionData section = data.getOrCreatePrismSection(key);
                    section.setRadiation(Math.max(section.radiation(), radiation));
                }
            }

            data.setLegacyPrismMigrated();
        }
    }

    private static void decaySectionSurface(ServerLevel level, long key) {
        if (!isSectionLoaded(level, key)) {
            return;
        }

        int baseX = SectionPos.sectionToBlockCoord(SectionPos.x(key));
        int baseY = SectionPos.sectionToBlockCoord(SectionPos.y(key));
        int baseZ = SectionPos.sectionToBlockCoord(SectionPos.z(key));

        RandomSource random = level.random;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int localY = 0; localY < 16; localY++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    if (random.nextInt(3) != 0) {
                        continue;
                    }

                    int x = baseX + localX;
                    int y = baseY + localY;
                    int z = baseZ + localZ;
                    int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;

                    if (y < topY - 1 || y > topY) {
                        continue;
                    }

                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);

                    if (state.is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(cursor, ModBlocks.WASTE_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
                    } else if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH)) {
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

    private static long sectionKey(BlockPos pos) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getY()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private static long offsetSection(long key, Direction direction) {
        return SectionPos.asLong(SectionPos.x(key) + direction.getStepX(), SectionPos.y(key) + direction.getStepY(), SectionPos.z(key) + direction.getStepZ());
    }

    private static boolean isSectionLoaded(ServerLevel level, long key) {
        int sectionY = SectionPos.y(key);
        int baseY = SectionPos.sectionToBlockCoord(sectionY);

        if (baseY >= level.getMaxBuildHeight() || baseY + 15 < level.getMinBuildHeight()) {
            return false;
        }

        return level.hasChunk(SectionPos.x(key), SectionPos.z(key));
    }

    private static boolean isInsideBuildHeight(ServerLevel level, int y) {
        return y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight();
    }

    private static float sanitize(float value) {
        if (!Float.isFinite(value) || value <= RAD_EPSILON) {
            return 0.0F;
        }

        return Math.min(value, MAX_RADIATION);
    }

    private record DestructionTarget(long sectionKey) {
    }
}