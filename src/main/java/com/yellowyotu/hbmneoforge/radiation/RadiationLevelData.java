package com.yellowyotu.hbmneoforge.radiation;

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
    private static final String PRISM_SECTION_RADIATION_KEY = "prism_section_radiation";
    private static final String LEGACY_PRISM_RADIATION_KEY = "prism_radiation";
    private static final String LEGACY_POCKET_RADIATION_KEY = "pocket_radiation";
    private static final String LEGACY_MIGRATED_KEY = "legacy_sources_migrated";
    private static final String LEGACY_PRISM_MIGRATED_KEY = "legacy_prism_migrated";
    private static final String LEGACY_POCKET_MIGRATED_KEY = "legacy_pocket_migrated";

    public static final Factory<RadiationLevelData> FACTORY = new Factory<>(RadiationLevelData::new, RadiationLevelData::load, DataFixTypes.LEVEL);

    private final Map<Long, Float> simpleRadiation = new HashMap<>();
    private final Map<Long, PrismSectionData> prismRadiation = new HashMap<>();
    private final Map<Long, double[]> legacyPocketRadiation = new HashMap<>();
    private final Map<Long, LegacyPrismChunkData> legacyPrismRadiation = new HashMap<>();
    private boolean legacySourcesMigrated;
    private boolean legacyPrismMigrated;
    private boolean legacyPocketMigrated;

    public static String dataName() {
        return DATA_NAME;
    }

    Map<Long, Float> simpleRadiation() {
        return simpleRadiation;
    }

    Map<Long, PrismSectionData> prismRadiation() {
        return prismRadiation;
    }

    PrismSectionData getOrCreatePrismSection(long sectionKey) {
        return prismRadiation.computeIfAbsent(sectionKey, ignored -> new PrismSectionData());
    }

    Map<Long, double[]> legacyPocketRadiation() {
        return legacyPocketRadiation;
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

    boolean isLegacyPocketMigrated() {
        return legacyPocketMigrated;
    }

    void setLegacyPocketMigrated() {
        legacyPocketMigrated = true;
        legacyPocketRadiation.clear();
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
        data.legacyPocketMigrated = tag.getBoolean(LEGACY_POCKET_MIGRATED_KEY);

        ListTag simpleList = tag.getList(SIMPLE_RADIATION_KEY, Tag.TAG_COMPOUND);

        for (int i = 0; i < simpleList.size(); i++) {
            CompoundTag entryTag = simpleList.getCompound(i);
            float radiation = sanitizeRadiation(entryTag.getFloat("radiation"));

            if (radiation > 0.0F) {
                data.simpleRadiation.put(entryTag.getLong("chunk"), radiation);
            }
        }

        ListTag prismSectionList = tag.getList(PRISM_SECTION_RADIATION_KEY, Tag.TAG_COMPOUND);

        for (int i = 0; i < prismSectionList.size(); i++) {
            CompoundTag sectionTag = prismSectionList.getCompound(i);
            float radiation = sanitizeRadiation(sectionTag.getFloat("radiation"));

            if (radiation <= 0.0F) {
                continue;
            }

            long sectionKey = sectionTag.getLong("section");
            data.prismRadiation.put(sectionKey, new PrismSectionData(radiation));
        }

        if (!data.legacyPocketMigrated) {
            ListTag pocketList = tag.getList(LEGACY_POCKET_RADIATION_KEY, Tag.TAG_COMPOUND);

            for (int i = 0; i < pocketList.size(); i++) {
                CompoundTag sectionTag = pocketList.getCompound(i);
                long sectionKey = sectionTag.getLong("section");
                long[] densityBits = sectionTag.getLongArray("densities");
                double[] densities = new double[densityBits.length];

                for (int densityIndex = 0; densityIndex < densityBits.length; densityIndex++) {
                    double density = Double.longBitsToDouble(densityBits[densityIndex]);
                    densities[densityIndex] = Double.isFinite(density) && density > 0.0D ? density : 0.0D;
                }

                data.legacyPocketRadiation.put(sectionKey, densities);
            }
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

                    float radiation = sanitizeRadiation(sectionTag.getFloat("radiation"));

                    if (radiation > 0.0F) {
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
        tag.putBoolean(LEGACY_POCKET_MIGRATED_KEY, legacyPocketMigrated);

        ListTag simpleList = new ListTag();

        for (Map.Entry<Long, Float> entry : simpleRadiation.entrySet()) {
            float radiation = sanitizeRadiation(entry.getValue());

            if (radiation <= 0.0F) {
                continue;
            }

            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("chunk", entry.getKey());
            entryTag.putFloat("radiation", radiation);
            simpleList.add(entryTag);
        }

        tag.put(SIMPLE_RADIATION_KEY, simpleList);

        ListTag prismSectionList = new ListTag();

        for (Map.Entry<Long, PrismSectionData> entry : prismRadiation.entrySet()) {
            float radiation = sanitizeRadiation(entry.getValue().radiation());

            if (radiation <= 0.0F) {
                continue;
            }

            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putLong("section", entry.getKey());
            sectionTag.putFloat("radiation", radiation);
            prismSectionList.add(sectionTag);
        }

        tag.put(PRISM_SECTION_RADIATION_KEY, prismSectionList);

        if (!legacyPocketMigrated && !legacyPocketRadiation.isEmpty()) {
            ListTag pocketList = new ListTag();

            for (Map.Entry<Long, double[]> entry : legacyPocketRadiation.entrySet()) {
                long[] densityBits = new long[entry.getValue().length];
                boolean hasRadiation = false;

                for (int densityIndex = 0; densityIndex < entry.getValue().length; densityIndex++) {
                    double density = entry.getValue()[densityIndex];

                    if (!Double.isFinite(density) || density <= 0.0D) {
                        density = 0.0D;
                    } else {
                        hasRadiation = true;
                    }

                    densityBits[densityIndex] = Double.doubleToRawLongBits(density);
                }

                if (!hasRadiation) {
                    continue;
                }

                CompoundTag sectionTag = new CompoundTag();
                sectionTag.putLong("section", entry.getKey());
                sectionTag.putLongArray("densities", densityBits);
                pocketList.add(sectionTag);
            }

            tag.put(LEGACY_POCKET_RADIATION_KEY, pocketList);
        }

        if (!legacyPrismMigrated && !legacyPrismRadiation.isEmpty()) {
            ListTag prismList = new ListTag();

            for (Map.Entry<Long, LegacyPrismChunkData> entry : legacyPrismRadiation.entrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                ListTag sectionList = new ListTag();

                for (int sectionIndex = 0; sectionIndex < LegacyPrismChunkData.SECTION_COUNT; sectionIndex++) {
                    float radiation = sanitizeRadiation(entry.getValue().radiation[sectionIndex]);

                    if (radiation <= 0.0F) {
                        continue;
                    }

                    CompoundTag sectionTag = new CompoundTag();
                    sectionTag.putInt("index", sectionIndex);
                    sectionTag.putFloat("radiation", radiation);
                    sectionList.add(sectionTag);
                }

                if (sectionList.isEmpty()) {
                    continue;
                }

                chunkTag.putLong("chunk", entry.getKey());
                chunkTag.put("sections", sectionList);
                prismList.add(chunkTag);
            }

            tag.put(LEGACY_PRISM_RADIATION_KEY, prismList);
        }

        return tag;
    }

    private static float sanitizeRadiation(float radiation) {
        if (!Float.isFinite(radiation) || radiation <= 0.0F) {
            return 0.0F;
        }

        return Math.min(radiation, 1_000_000.0F);
    }

    static final class PrismSectionData {

        private float radiation;
        private final float[] xResistance = new float[16];
        private final float[] yResistance = new float[16];
        private final float[] zResistance = new float[16];
        private boolean resistanceDirty = true;

        PrismSectionData() {
        }

        PrismSectionData(float radiation) {
            this.radiation = sanitizeRadiation(radiation);
        }

        float radiation() {
            return radiation;
        }

        void setRadiation(float radiation) {
            this.radiation = sanitizeRadiation(radiation);
        }

        float[] xResistance() {
            return xResistance;
        }

        float[] yResistance() {
            return yResistance;
        }

        float[] zResistance() {
            return zResistance;
        }

        boolean isResistanceDirty() {
            return resistanceDirty;
        }

        void markResistanceDirty() {
            resistanceDirty = true;
        }

        void markResistanceClean() {
            resistanceDirty = false;
        }
    }

    static final class LegacyPrismChunkData {

        static final int SECTION_COUNT = 16;
        final float[] radiation = new float[SECTION_COUNT];
    }
}