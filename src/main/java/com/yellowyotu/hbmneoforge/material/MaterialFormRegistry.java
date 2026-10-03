package com.yellowyotu.hbmneoforge.material;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public final class MaterialFormRegistry {
    private MaterialFormRegistry() {}

    public static String registryName(String materialId, MaterialShape shape) {
        return new MaterialForm(HBMMaterialCatalog.get(materialId).orElseThrow(), shape).registryName();
    }

    public static List<String> plannedRegistryNames(Set<String> existingIds) {
        return HBMMaterialCatalog.forms().stream().map(MaterialForm::registryName)
                .filter(id -> !existingIds.contains(id)).distinct().toList();
    }

    public static Map<String, DeferredItem<Item>> registerMissing(DeferredRegister.Items items) {
        Set<String> existing = new HashSet<>();
        items.getEntries().forEach(entry -> existing.add(entry.getId().getPath()));
        LinkedHashMap<String, DeferredItem<Item>> result = new LinkedHashMap<>();
        for (String id : plannedRegistryNames(existing)) {
            result.put(id, items.registerSimpleItem(id, new Item.Properties()));
        }
        return Collections.unmodifiableMap(result);
    }
}
