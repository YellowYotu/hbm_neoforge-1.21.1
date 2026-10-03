package com.yellowyotu.hbmneoforge.material;

import com.yellowyotu.hbmneoforge.item.MaterialFormItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;

public final class NeoForgeMaterialFormRegistry {
    private NeoForgeMaterialFormRegistry() {}

    public static Map<String, DeferredItem<Item>> registerMissing(DeferredRegister.Items items) {
        Set<String> existing = new HashSet<>();
        items.getEntries().forEach(entry -> existing.add(entry.getId().getPath()));
        LinkedHashMap<String, DeferredItem<Item>> result = new LinkedHashMap<>();
        for (String id : MaterialFormRegistry.plannedRegistryNames(existing)) {
            MaterialForm form = MaterialFormRegistry.formByRegistryName(id).orElseThrow();
            result.put(id, items.register(id, () -> new MaterialFormItem(form, new Item.Properties())));
        }
        return Collections.unmodifiableMap(result);
    }
}
