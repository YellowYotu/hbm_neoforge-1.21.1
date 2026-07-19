package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModBlocks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    private static final int SECTION_BLOCK_COUNT = 4096;
    private static final int MAX_POCKETS = 2048;
    private static final double RAD_EPSILON = 1.0E-5D;
    private static final double RAD_MAX = Double.MAX_VALUE / 2.0D;
    private static final int[] LOCAL_DX = {-1, 1, 0, 0, 0, 0};
    private static final int[] LOCAL_DY = {0, 0, -1, 1, 0, 0};
    private static final int[] LOCAL_DZ = {0, 0, 0, 0, -1, 1};
    private final Map<ServerLevel, DestructionTarget> destructionTargets = new HashMap<>();
    private int updateTimer;

    @Override
    public float getRadiation(ServerLevel level, BlockPos pos) {
        if (!isInsideBuildHeight(level, pos.getY()) || !level.hasChunkAt(pos)) {
            return 0.0F;
        }

        RadiationLevelData.PocketSectionData section = getOrBuildSection(level, sectionKey(pos));
        if (section == null || section.pocketCount() == 0 || isShielding(level.getBlockState(pos), level, pos)) {
            return 0.0F;
        }

        int pocket = section.pocketByBlock()[localIndex(pos.getX(), pos.getY(), pos.getZ())];
        if (pocket < 0 || pocket >= section.densities().length) {
            return 0.0F;
        }
        return (float) Math.min(section.densities()[pocket], Float.MAX_VALUE);
    }

    @Override
    public void setRadiation(ServerLevel level, BlockPos pos, float radiation) {
        if (!isInsideBuildHeight(level, pos.getY()) || !level.hasChunkAt(pos)) {
            return;
        }

        RadiationLevelData.PocketSectionData section = getOrBuildSection(level, sectionKey(pos));
        if (section == null || section.pocketCount() == 0 || isShielding(level.getBlockState(pos), level, pos)) {
            return;
        }

        int pocket = section.pocketByBlock()[localIndex(pos.getX(), pos.getY(), pos.getZ())];
        if (pocket < 0 || pocket >= section.densities().length) {
            return;
        }

        section.densities()[pocket] = sanitize(radiation);
        ChunkRadiationManager.getData(level).setDirty();
    }

    @Override
    public void updateSystem(MinecraftServer server) {
        int tickRate = Math.max(1, RadiationConfig.POCKET_TICK_RATE.get());
        updateTimer++;
        if (updateTimer < tickRate) {
            return;
        }
        updateTimer = 0;

        double deltaTime = tickRate / 20.0D;
        double diffusivity = Math.max(RAD_EPSILON, RadiationConfig.POCKET_DIFFUSIVITY.get());
        double halfLife = Math.max(RAD_EPSILON, RadiationConfig.POCKET_HALF_LIFE_SECONDS.get());
        double diffusionDeltaTime = diffusivity * deltaTime;
        double retention = Math.exp(Math.log(0.5D) * (deltaTime / halfLife));

        for (ServerLevel level : server.getAllLevels()) {
            RadiationLevelData data = ChunkRadiationManager.getData(level);
            migrateLegacyPrism(data);
            if (collectCandidateSections(level, data).isEmpty()) {
                continue;
            }

            int epoch = (int) (level.getGameTime() / tickRate);
            Direction[] sweepOrder = getSweepOrder(epoch);
            boolean hasActiveSections = false;
            for (Direction direction : sweepOrder) {
                int startParity = switch (direction.getAxis()) {
                    case X -> epoch & 1;
                    case Z -> epoch >>> 1 & 1;
                    case Y -> epoch >>> 2 & 1;
                };
                hasActiveSections |= diffuseAxis(level, data, direction, diffusionDeltaTime, startParity);
            }
            if (!hasActiveSections) {
                continue;
            }

            Set<Long> finalCandidateKeys = collectCandidateSections(level, data);
            List<Long> finalOrderedKeys = finalCandidateKeys.stream().sorted(Comparator.naturalOrder()).toList();
            for (long key : finalOrderedKeys) {
                getOrBuildSection(level, key);
            }
            applyRetentionAndSelectDestruction(level, data, finalOrderedKeys, retention);
            data.setDirty();
        }
    }

    @Override
    public void clearSystem(ServerLevel level) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        data.pocketRadiation().clear();
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
            decayPocketSurface(level, target.sectionKey(), target.pocketIndex());
        }
    }

    void markSectionForRebuild(ServerLevel level, BlockPos pos) {
        RadiationLevelData data = ChunkRadiationManager.getData(level);
        markSectionDirty(data, sectionKey(pos));

        int localX = SectionPos.sectionRelative(pos.getX());
        int localY = SectionPos.sectionRelative(pos.getY());
        int localZ = SectionPos.sectionRelative(pos.getZ());
        int sectionX = SectionPos.blockToSectionCoord(pos.getX());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());

        if (localX == 0) {
            markSectionDirty(data, SectionPos.asLong(sectionX - 1, sectionY, sectionZ));
        } else if (localX == 15) {
            markSectionDirty(data, SectionPos.asLong(sectionX + 1, sectionY, sectionZ));
        }
        if (localY == 0) {
            markSectionDirty(data, SectionPos.asLong(sectionX, sectionY - 1, sectionZ));
        } else if (localY == 15) {
            markSectionDirty(data, SectionPos.asLong(sectionX, sectionY + 1, sectionZ));
        }
        if (localZ == 0) {
            markSectionDirty(data, SectionPos.asLong(sectionX, sectionY, sectionZ - 1));
        } else if (localZ == 15) {
            markSectionDirty(data, SectionPos.asLong(sectionX, sectionY, sectionZ + 1));
        }
        data.setDirty();
    }

    private static void markSectionDirty(RadiationLevelData data, long key) {
        RadiationLevelData.PocketSectionData section = data.pocketRadiation().get(key);
        if (section != null) {
            section.markGeometryDirty();
        }
    }

    private static Set<Long> collectCandidateSections(ServerLevel level, RadiationLevelData data) {
        Set<Long> candidates = new HashSet<>();
        List<Long> activeKeys = new ArrayList<>();
        Iterator<Map.Entry<Long, RadiationLevelData.PocketSectionData>> iterator = data.pocketRadiation().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, RadiationLevelData.PocketSectionData> entry = iterator.next();
            boolean loaded = isSectionLoaded(level, entry.getKey());
            if (!hasRadiation(entry.getValue().getDensitiesForSave())) {
                if (!loaded) {
                    iterator.remove();
                }
                continue;
            }
            if (!loaded) {
                entry.getValue().clearGeometry();
                continue;
            }
            activeKeys.add(entry.getKey());
            candidates.add(entry.getKey());
        }

        for (long key : activeKeys) {
            int sectionX = SectionPos.x(key);
            int sectionY = SectionPos.y(key);
            int sectionZ = SectionPos.z(key);
            for (Direction direction : Direction.values()) {
                long neighborKey = SectionPos.asLong(sectionX + direction.getStepX(), sectionY + direction.getStepY(), sectionZ + direction.getStepZ());
                if (isSectionLoaded(level, neighborKey)) {
                    candidates.add(neighborKey);
                    data.getOrCreatePocketSection(neighborKey);
                }
            }
        }
        return candidates;
    }

    private static boolean hasRadiation(double[] densities) {
        for (double density : densities) {
            if (density > RAD_EPSILON) {
                return true;
            }
        }
        return false;
    }

    private static boolean diffuseAxis(ServerLevel level, RadiationLevelData data, Direction direction, double diffusionDeltaTime, int startParity) {
        boolean hasActiveSections = false;
        for (int pass = 0; pass < 2; pass++) {
            Set<Long> candidateKeys = collectCandidateSections(level, data);
            if (candidateKeys.isEmpty()) {
                continue;
            }
            hasActiveSections = true;

            List<Long> orderedKeys = candidateKeys.stream().sorted(Comparator.naturalOrder()).toList();
            for (long key : orderedKeys) {
                getOrBuildSection(level, key);
            }

            int parity = startParity ^ pass;
            for (long key : orderedKeys) {
                int coordinate = switch (direction.getAxis()) {
                    case X -> SectionPos.x(key);
                    case Y -> SectionPos.y(key);
                    case Z -> SectionPos.z(key);
                };
                if ((coordinate & 1) != parity) {
                    continue;
                }

                long neighborKey = offsetSection(key, direction);
                RadiationLevelData.PocketSectionData sectionA = data.pocketRadiation().get(key);
                RadiationLevelData.PocketSectionData sectionB = data.pocketRadiation().get(neighborKey);
                if (sectionA == null || sectionB == null || !sectionA.isGeometryReady() || !sectionB.isGeometryReady()) {
                    continue;
                }
                exchangeBoundary(sectionA, sectionB, direction, diffusionDeltaTime);
            }
        }
        return hasActiveSections;
    }

    private static Direction[] getSweepOrder(int epoch) {
        return switch (Math.floorMod(epoch, 6)) {
            case 0 -> new Direction[]{Direction.EAST, Direction.SOUTH, Direction.UP};
            case 1 -> new Direction[]{Direction.EAST, Direction.UP, Direction.SOUTH};
            case 2 -> new Direction[]{Direction.UP, Direction.SOUTH, Direction.EAST};
            case 3 -> new Direction[]{Direction.UP, Direction.EAST, Direction.SOUTH};
            case 4 -> new Direction[]{Direction.SOUTH, Direction.EAST, Direction.UP};
            default -> new Direction[]{Direction.SOUTH, Direction.UP, Direction.EAST};
        };
    }

    private static void exchangeBoundary(RadiationLevelData.PocketSectionData sectionA, RadiationLevelData.PocketSectionData sectionB, Direction direction, double diffusionDeltaTime) {
        if (sectionA.pocketCount() == 0 || sectionB.pocketCount() == 0) {
            return;
        }

        Map<Long, Integer> areas = new HashMap<>();
        for (int first = 0; first < 16; first++) {
            for (int second = 0; second < 16; second++) {
                int indexA;
                int indexB;
                if (direction == Direction.EAST) {
                    indexA = localIndex(15, first, second);
                    indexB = localIndex(0, first, second);
                } else if (direction == Direction.SOUTH) {
                    indexA = localIndex(first, second, 15);
                    indexB = localIndex(first, second, 0);
                } else {
                    indexA = localIndex(first, 15, second);
                    indexB = localIndex(first, 0, second);
                }

                int pocketA = sectionA.pocketByBlock()[indexA];
                int pocketB = sectionB.pocketByBlock()[indexB];
                if (pocketA < 0 || pocketB < 0) {
                    continue;
                }
                long pair = ((long) pocketA << 32) | (pocketB & 0xFFFF_FFFFL);
                areas.merge(pair, 1, Integer::sum);
            }
        }

        int faceA = faceIndex(direction);
        int faceB = faceIndex(direction.getOpposite());
        List<Map.Entry<Long, Integer>> orderedAreas = new ArrayList<>(areas.entrySet());
        orderedAreas.sort(Map.Entry.comparingByKey());
        for (Map.Entry<Long, Integer> entry : orderedAreas) {
            int pocketA = (int) (entry.getKey() >>> 32);
            int pocketB = entry.getKey().intValue();
            exchangeExact(sectionA, pocketA, faceA, sectionB, pocketB, faceB, entry.getValue(), diffusionDeltaTime);
        }
    }

    private static void exchangeExact(RadiationLevelData.PocketSectionData sectionA, int pocketA, int faceA, RadiationLevelData.PocketSectionData sectionB, int pocketB, int faceB, int area, double diffusionDeltaTime) {
        double radiationA = sectionA.densities()[pocketA];
        double radiationB = sectionB.densities()[pocketB];
        if (radiationA == radiationB) {
            return;
        }

        double inverseVolumeA = 1.0D / sectionA.volumes()[pocketA];
        double inverseVolumeB = 1.0D / sectionB.volumes()[pocketB];
        double denominatorInverse = inverseVolumeA + inverseVolumeB;
        double distanceSum = sectionA.faceDistances()[pocketA * 6 + faceA] + sectionB.faceDistances()[pocketB * 6 + faceB];
        if (distanceSum <= 0.0D) {
            return;
        }

        double exponential = Math.exp(-((area / distanceSum) * denominatorInverse * diffusionDeltaTime));
        double equilibrium = (radiationA * inverseVolumeB + radiationB * inverseVolumeA) / denominatorInverse;
        sectionA.densities()[pocketA] = equilibrium + (radiationA - equilibrium) * exponential;
        sectionB.densities()[pocketB] = equilibrium + (radiationB - equilibrium) * exponential;
    }

    private void applyRetentionAndSelectDestruction(ServerLevel level, RadiationLevelData data, List<Long> orderedKeys, double retention) {
        RandomSource random = level.random;
        DestructionTarget selectedTarget = null;

        for (long key : orderedKeys) {
            RadiationLevelData.PocketSectionData section = data.pocketRadiation().get(key);
            if (section == null || !section.isGeometryReady()) {
                continue;
            }

            for (int pocket = 0; pocket < section.densities().length; pocket++) {
                double previous = section.densities()[pocket];
                if (previous <= 0.0D) {
                    continue;
                }
                double next = sanitize(previous * retention);
                section.densities()[pocket] = next;
                if (next >= 5.0D && random.nextInt(100) == 0) {
                    selectedTarget = new DestructionTarget(key, pocket);
                }
            }
        }

        if (selectedTarget != null) {
            destructionTargets.put(level, selectedTarget);
        }
    }

    private static RadiationLevelData.PocketSectionData getOrBuildSection(ServerLevel level, long key) {
        if (!isSectionLoaded(level, key)) {
            return null;
        }

        RadiationLevelData data = ChunkRadiationManager.getData(level);
        RadiationLevelData.PocketSectionData section = data.getOrCreatePocketSection(key);
        if (!section.isGeometryReady()) {
            rebuildSection(level, key, section);
            data.setDirty();
        }
        return section;
    }

    private static void rebuildSection(ServerLevel level, long key, RadiationLevelData.PocketSectionData section) {
        short[] oldPocketByBlock = section.pocketByBlock();
        int[] oldVolumes = section.volumes();
        double[] oldDensities = section.densities();
        double[] storedDensities = section.storedDensities();

        short[] pocketByBlock = new short[SECTION_BLOCK_COUNT];
        Arrays.fill(pocketByBlock, RadiationLevelData.PocketSectionData.NO_POCKET);
        boolean[] shielding = scanShielding(level, key);
        int[] queue = new int[SECTION_BLOCK_COUNT];
        int[] volumesScratch = new int[MAX_POCKETS];
        long[] sumX = new long[MAX_POCKETS];
        long[] sumY = new long[MAX_POCKETS];
        long[] sumZ = new long[MAX_POCKETS];
        int pocketCount = floodFillPockets(shielding, pocketByBlock, queue, volumesScratch, sumX, sumY, sumZ);

        int[] volumes = Arrays.copyOf(volumesScratch, pocketCount);
        double[] faceDistances = new double[pocketCount * 6];
        for (int pocket = 0; pocket < pocketCount; pocket++) {
            int volume = Math.max(1, volumes[pocket]);
            double centerX = sumX[pocket] / (double) volume;
            double centerY = sumY[pocket] / (double) volume;
            double centerZ = sumZ[pocket] / (double) volume;
            int base = pocket * 6;
            faceDistances[base] = centerY + 0.5D;
            faceDistances[base + 1] = 15.5D - centerY;
            faceDistances[base + 2] = centerZ + 0.5D;
            faceDistances[base + 3] = 15.5D - centerZ;
            faceDistances[base + 4] = centerX + 0.5D;
            faceDistances[base + 5] = 15.5D - centerX;
        }

        double[] densities = new double[pocketCount];
        if (oldPocketByBlock != null && oldVolumes != null && oldDensities != null) {
            remapPocketMass(oldPocketByBlock, oldVolumes, oldDensities, pocketByBlock, volumes, densities);
        } else {
            for (int pocket = 0; pocket < Math.min(storedDensities.length, densities.length); pocket++) {
                densities[pocket] = sanitize(storedDensities[pocket]);
            }
        }

        section.setGeometry(pocketByBlock, volumes, faceDistances, densities);
    }

    private static boolean[] scanShielding(ServerLevel level, long key) {
        boolean[] shielding = new boolean[SECTION_BLOCK_COUNT];
        int baseX = SectionPos.sectionToBlockCoord(SectionPos.x(key));
        int baseY = SectionPos.sectionToBlockCoord(SectionPos.y(key));
        int baseZ = SectionPos.sectionToBlockCoord(SectionPos.z(key));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int localY = 0; localY < 16; localY++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    cursor.set(baseX + localX, baseY + localY, baseZ + localZ);
                    BlockState state = level.getBlockState(cursor);
                    shielding[localIndex(localX, localY, localZ)] = isShielding(state, level, cursor);
                }
            }
        }
        return shielding;
    }

    private static boolean isShielding(BlockState state, ServerLevel level, BlockPos pos) {
        return state.getBlock() instanceof RadiationShielding shielding && shielding.isRadiationShielding(state, level, pos);
    }

    private static int floodFillPockets(boolean[] shielding, short[] pocketByBlock, int[] queue, int[] volumes, long[] sumX, long[] sumY, long[] sumZ) {
        int pocketCount = 0;
        for (int blockIndex = 0; blockIndex < SECTION_BLOCK_COUNT; blockIndex++) {
            if (shielding[blockIndex] || pocketByBlock[blockIndex] != RadiationLevelData.PocketSectionData.NO_POCKET) {
                continue;
            }

            int pocket = pocketCount >= MAX_POCKETS ? 0 : pocketCount++;
            int head = 0;
            int tail = 0;
            queue[tail++] = blockIndex;
            pocketByBlock[blockIndex] = (short) pocket;

            while (head < tail) {
                int current = queue[head++];
                int localX = current & 15;
                int localZ = current >>> 4 & 15;
                int localY = current >>> 8 & 15;
                volumes[pocket]++;
                sumX[pocket] += localX;
                sumY[pocket] += localY;
                sumZ[pocket] += localZ;

                for (int directionIndex = 0; directionIndex < 6; directionIndex++) {
                    int neighborX = localX + LOCAL_DX[directionIndex];
                    int neighborY = localY + LOCAL_DY[directionIndex];
                    int neighborZ = localZ + LOCAL_DZ[directionIndex];
                    if ((neighborX | neighborY | neighborZ) < 0 || neighborX >= 16 || neighborY >= 16 || neighborZ >= 16) {
                        continue;
                    }
                    int neighbor = localIndex(neighborX, neighborY, neighborZ);
                    if (shielding[neighbor] || pocketByBlock[neighbor] != RadiationLevelData.PocketSectionData.NO_POCKET) {
                        continue;
                    }
                    pocketByBlock[neighbor] = (short) pocket;
                    queue[tail++] = neighbor;
                }
            }
        }
        return pocketCount;
    }

    private static void remapPocketMass(short[] oldPocketByBlock, int[] oldVolumes, double[] oldDensities, short[] newPocketByBlock, int[] newVolumes, double[] newDensities) {
        if (newDensities.length == 0 || oldDensities.length == 0) {
            return;
        }

        double[] oldMass = new double[oldDensities.length];
        for (int oldPocket = 0; oldPocket < oldDensities.length; oldPocket++) {
            int volume = oldPocket < oldVolumes.length ? Math.max(1, oldVolumes[oldPocket]) : 1;
            oldMass[oldPocket] = oldDensities[oldPocket] * volume;
        }

        int[] oldOverlapTotals = new int[oldDensities.length];
        Map<Long, Integer> overlapCounts = new HashMap<>();
        for (int index = 0; index < SECTION_BLOCK_COUNT; index++) {
            int oldPocket = oldPocketByBlock[index];
            int newPocket = newPocketByBlock[index];
            if (oldPocket < 0 || oldPocket >= oldDensities.length || newPocket < 0 || newPocket >= newDensities.length) {
                continue;
            }
            oldOverlapTotals[oldPocket]++;
            long pair = ((long) oldPocket << 32) | (newPocket & 0xFFFF_FFFFL);
            overlapCounts.merge(pair, 1, Integer::sum);
        }

        double[] newMass = new double[newDensities.length];
        for (Map.Entry<Long, Integer> entry : overlapCounts.entrySet()) {
            int oldPocket = (int) (entry.getKey() >>> 32);
            int newPocket = entry.getKey().intValue();
            int totalOverlap = oldOverlapTotals[oldPocket];
            if (totalOverlap > 0) {
                newMass[newPocket] += oldMass[oldPocket] * entry.getValue() / totalOverlap;
            }
        }

        for (int newPocket = 0; newPocket < newDensities.length; newPocket++) {
            newDensities[newPocket] = sanitize(newMass[newPocket] / Math.max(1, newVolumes[newPocket]));
        }
    }

    private static void migrateLegacyPrism(RadiationLevelData data) {
        if (data.isLegacyPrismMigrated()) {
            return;
        }

        for (Map.Entry<Long, RadiationLevelData.LegacyPrismChunkData> entry : data.legacyPrismRadiation().entrySet()) {
            ChunkPos chunkPos = new ChunkPos(entry.getKey());
            for (int sectionY = 0; sectionY < RadiationLevelData.LegacyPrismChunkData.SECTION_COUNT; sectionY++) {
                float radiation = entry.getValue().radiation[sectionY];
                if (radiation <= 0.0F) {
                    continue;
                }
                long key = SectionPos.asLong(chunkPos.x, sectionY, chunkPos.z);
                RadiationLevelData.PocketSectionData section = data.getOrCreatePocketSection(key);
                double[] existing = section.getDensitiesForSave();
                double[] migrated = new double[Math.max(MAX_POCKETS, existing.length)];
                Arrays.fill(migrated, radiation);
                for (int pocket = 0; pocket < existing.length; pocket++) {
                    migrated[pocket] = Math.max(migrated[pocket], existing[pocket]);
                }
                data.pocketRadiation().put(key, new RadiationLevelData.PocketSectionData(migrated));
            }
        }
        data.setLegacyPrismMigrated();
    }

    private static void decayPocketSurface(ServerLevel level, long key, int targetPocket) {
        RadiationLevelData.PocketSectionData section = getOrBuildSection(level, key);
        if (section == null || targetPocket < 0 || targetPocket >= section.pocketCount()) {
            return;
        }

        int baseX = SectionPos.sectionToBlockCoord(SectionPos.x(key));
        int baseY = SectionPos.sectionToBlockCoord(SectionPos.y(key));
        int baseZ = SectionPos.sectionToBlockCoord(SectionPos.z(key));
        RandomSource random = level.random;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int index = 0; index < SECTION_BLOCK_COUNT; index++) {
            if (section.pocketByBlock()[index] != targetPocket || random.nextInt(3) != 0) {
                continue;
            }
            int localX = index & 15;
            int localZ = index >>> 4 & 15;
            int localY = index >>> 8 & 15;
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

    private static int localIndex(int x, int y, int z) {
        return (y & 15) << 8 | (z & 15) << 4 | x & 15;
    }

    private static int faceIndex(Direction direction) {
        return switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private static double sanitize(double value) {
        if (!Double.isFinite(value) || value < RAD_EPSILON) {
            return 0.0D;
        }
        return Math.min(value, RAD_MAX);
    }

    private record DestructionTarget(long sectionKey, int pocketIndex) {
    }
}
