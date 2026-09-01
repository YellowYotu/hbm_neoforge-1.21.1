package com.yellowyotu.hbmneoforge.radiation;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

final class LegacyRadiationWorldData extends SavedData {

    private static final String DATA_NAME = "hbm_neoforge_radiation";
    static final Factory<LegacyRadiationWorldData> FACTORY = new Factory<>(LegacyRadiationWorldData::new, LegacyRadiationWorldData::load, DataFixTypes.LEVEL);

    private final List<Source> sources = new ArrayList<>();

    static String dataName() {
        return DATA_NAME;
    }

    List<Source> getSources() {
        return List.copyOf(sources);
    }

    static LegacyRadiationWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        LegacyRadiationWorldData data = new LegacyRadiationWorldData();
        ListTag list = tag.getList("sources", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag sourceTag = list.getCompound(i);
            BlockPos pos = new BlockPos(sourceTag.getInt("x"), sourceTag.getInt("y"), sourceTag.getInt("z"));
            float strength = sourceTag.getFloat("strength");
            if (strength > 0.0F && Float.isFinite(strength)) {
                data.sources.add(new Source(pos, strength));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Source source : sources) {
            CompoundTag sourceTag = new CompoundTag();
            sourceTag.putInt("x", source.pos().getX());
            sourceTag.putInt("y", source.pos().getY());
            sourceTag.putInt("z", source.pos().getZ());
            sourceTag.putFloat("strength", source.strength());
            list.add(sourceTag);
        }
        tag.put("sources", list);
        return tag;
    }

    record Source(BlockPos pos, float strength) {
    }
}
