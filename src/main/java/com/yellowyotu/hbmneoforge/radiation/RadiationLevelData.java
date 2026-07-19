package com.yellowyotu.hbmneoforge.radiation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class RadiationLevelData extends SavedData {

    private static final String DATA_NAME = "hbm_neoforge_chunk_radiation";
    private static final String SIMPLE_RADIATION_KEY = "simple_radiation";
    private static final String LEGACY_PRISM_RADIATION_KEY = "prism_radiation";
    private static final String POCKET_RADIATION_KEY = "pocket_radiation";
    private static final String LEGACY_MIGRATED_KEY = "legacy_sources_migrated";
    private static final String LEGACY_PRISM_MIGRATED_KEY = "legacy_prism_migrated";

    public static final Factory<RadiationLevelData> FACTORY = new Factory<>(RadiationLevelData::new, RadiationLevelData::load, DataFixTypes.LEVEL);

    private final Map<Long, Float> simpleRadiation = new HashMap<>();
    private final Map<Long, PocketSectionData> pocketRadiation = new HashMap<>();
    private final Map<Long, LegacyPrismChunkData> legacyPrismRadiation = new HashMap<>();
    private boolean legacySourcesMigrated;
    private boolean legacyPrismMigrated;

    public static String dataName() {
        return DATA_NAME;
    }

    Map<Long, Float> simpleRadiation() {
        return simpleRadiation;
    }

    Map<Long, PocketSectionData> pocketRadiation() {
        return pocketRadiation;
    }

    PocketSectionData getOrCreatePocketSection(long sectionKey) {
        return pocketRadiation.computeIfAbsent(sectionKey, ignored -> new PocketSectionData());
    }

    Map<Long, LegacyPrismChunkData> legacyPrismRadiation() {
        return legacyPrismRadiation;
    }

    boolean isLegacyPrismMigrated() {
        return legacyPrismMigrated;
    }

    void setLegacyPrismMigrated() {
        legacyPrismMigrated = true;
        legacyPrismRadiation.clear();
        setDirty();
    }

    public boolean isLegacySourcesMigrated() {
        return legacySourcesMigrated;
    }

    public void setLegacySourcesMigrated() {
        legacySourcesMigrated = true;
        setDirty();
    }

    public static RadiationLevelData load(CompoundTag tag, HolderLookup.Provider registries) {
        RadiationLevelData data = new RadiationLevelData();
        data.legacySourcesMigrated = tag.getBoolean(LEGACY_MIGRATED_KEY);
        data.legacyPrismMigrated = tag.getBoolean(LEGACY_PRISM_MIGRATED_KEY);

        ListTag simpleList = tag.getList(SIMPLE_RADIATION_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < simpleList.size(); i++) {
            CompoundTag entryTag = simpleList.getCompound(i);
            data.simpleRadiation.put(entryTag.getLong("chunk"), entryTag.getFloat("radiation"));
        }

        ListTag pocketList = tag.getList(POCKET_RADIATION_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < pocketList.size(); i++) {
            CompoundTag sectionTag = pocketList.getCompound(i);
            long sectionKey = sectionTag.getLong("section");
            long[] densityBits = sectionTag.getLongArray("densities");
            double[] densities = new double[densityBits.length];
            for (int densityIndex = 0; densityIndex < densityBits.length; densityIndex++) {
                double density = Double.longBitsToDouble(densityBits[densityIndex]);
                densities[densityIndex] = Double.isFinite(density) && density > 0.0D ? density : 0.0D;
            }
            data.pocketRadiation.put(sectionKey, new PocketSectionData(densities));
        }

        if (!data.legacyPrismMigrated) {
            ListTag prismList = tag.getList(LEGACY_PRISM_RADIATION_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < prismList.size(); i++) {
                CompoundTag chunkTag = prismList.getCompound(i);
                LegacyPrismChunkData chunkData = new LegacyPrismChunkData();
                ListTag sectionList = chunkTag.getList("sections", Tag.TAG_COMPOUND);

                for (int sectionIndex = 0; sectionIndex < sectionList.size(); sectionIndex++) {
                    CompoundTag sectionTag = sectionList.getCompound(sectionIndex);
                    int index = sectionTag.getInt("index");
                    if (index < 0 || index >= LegacyPrismChunkData.SECTION_COUNT) {
                        continue;
                    }
                    float radiation = sectionTag.getFloat("radiation");
                    if (Float.isFinite(radiation) && radiation > 0.0F) {
                        chunkData.radiation[index] = radiation;
                    }
                }

                data.legacyPrismRadiation.put(chunkTag.getLong("chunk"), chunkData);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(LEGACY_MIGRATED_KEY, legacySourcesMigrated);
        tag.putBoolean(LEGACY_PRISM_MIGRATED_KEY, legacyPrismMigrated);

        ListTag simpleList = new ListTag();
        for (Map.Entry<Long, Float> entry : simpleRadiation.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("chunk", entry.getKey());
            entryTag.putFloat("radiation", entry.getValue());
            simpleList.add(entryTag);
        }
        tag.put(SIMPLE_RADIATION_KEY, simpleList);

        ListTag pocketList = new ListTag();
        for (Map.Entry<Long, PocketSectionData> entry : pocketRadiation.entrySet()) {
            double[] densities = entry.getValue().getDensitiesForSave();
            boolean hasRadiation = false;
            long[] densityBits = new long[densities.length];
            for (int densityIndex = 0; densityIndex < densities.length; densityIndex++) {
                double density = Double.isFinite(densities[densityIndex]) && densities[densityIndex] > 0.0D ? densities[densityIndex] : 0.0D;
                densityBits[densityIndex] = Double.doubleToRawLongBits(density);
                hasRadiation |= density > 0.0D;
            }
            if (!hasRadiation) {
                continue;
            }

            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putLong("section", entry.getKey());
            sectionTag.putLongArray("densities", densityBits);
            pocketList.add(sectionTag);
        }
        tag.put(POCKET_RADIATION_KEY, pocketList);

        if (!legacyPrismMigrated && !legacyPrismRadiation.isEmpty()) {
            ListTag prismList = new ListTag();
            for (Map.Entry<Long, LegacyPrismChunkData> entry : legacyPrismRadiation.entrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("chunk", entry.getKey());
                ListTag sectionList = new ListTag();
                for (int sectionIndex = 0; sectionIndex < LegacyPrismChunkData.SECTION_COUNT; sectionIndex++) {
                    float radiation = entry.getValue().radiation[sectionIndex];
                    if (radiation <= 0.0F) {
                        continue;
                    }
                    CompoundTag sectionTag = new CompoundTag();
                    sectionTag.putInt("index", sectionIndex);
                    sectionTag.putFloat("radiation", radiation);
                    sectionList.add(sectionTag);
                }
                if (!sectionList.isEmpty()) {
                    chunkTag.put("sections", sectionList);
                    prismList.add(chunkTag);
                }
            }
            tag.put(LEGACY_PRISM_RADIATION_KEY, prismList);
        }

        return tag;
    }

    static final class PocketSectionData {

        static final short NO_POCKET = -1;
        private double[] storedDensities;
        private short[] pocketByBlock;
        private int[] volumes;
        private double[] faceDistances;
        private double[] densities;
        private boolean geometryDirty = true;

        PocketSectionData() {
            this(new double[0]);
        }

        PocketSectionData(double[] storedDensities) {
            this.storedDensities = Arrays.copyOf(storedDensities, storedDensities.length);
        }

        boolean isGeometryReady() {
            return pocketByBlock != null && !geometryDirty;
        }

        void markGeometryDirty() {
            geometryDirty = true;
        }

        short[] pocketByBlock() {
            return pocketByBlock;
        }

        int[] volumes() {
            return volumes;
        }

        double[] faceDistances() {
            return faceDistances;
        }

        double[] densities() {
            return densities;
        }

        double[] storedDensities() {
            return storedDensities;
        }

        int pocketCount() {
            return densities == null ? storedDensities.length : densities.length;
        }

        void setGeometry(short[] pocketByBlock, int[] volumes, double[] faceDistances, double[] densities) {
            this.pocketByBlock = pocketByBlock;
            this.volumes = volumes;
            this.faceDistances = faceDistances;
            this.densities = densities;
            this.storedDensities = new double[0];
            this.geometryDirty = false;
        }

        void clearGeometry() {
            if (densities != null) {
                storedDensities = Arrays.copyOf(densities, densities.length);
            }
            pocketByBlock = null;
            volumes = null;
            faceDistances = null;
            densities = null;
            geometryDirty = true;
        }

        double[] getDensitiesForSave() {
            return densities != null ? densities : storedDensities;
        }
    }

    static final class LegacyPrismChunkData {

        static final int SECTION_COUNT = 16;
        final float[] radiation = new float[SECTION_COUNT];
    }
}
