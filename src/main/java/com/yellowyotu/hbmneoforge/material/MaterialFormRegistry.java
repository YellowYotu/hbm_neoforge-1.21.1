package com.yellowyotu.hbmneoforge.material;

import java.util.*;

public final class MaterialFormRegistry {
    private static final Map<String, MaterialForm> FORMS_BY_ID;
    static {
        LinkedHashMap<String, MaterialForm> forms = new LinkedHashMap<>();
        HBMMaterialCatalog.forms().forEach(form -> forms.put(form.registryName(), form));
        FORMS_BY_ID = Collections.unmodifiableMap(forms);
    }
    private MaterialFormRegistry() {}

    public static String registryName(String materialId, MaterialShape shape) {
        return new MaterialForm(HBMMaterialCatalog.get(materialId).orElseThrow(), shape).registryName();
    }

    public static List<String> plannedRegistryNames(Set<String> existingIds) {
        return HBMMaterialCatalog.forms().stream().map(MaterialForm::registryName)
                .filter(id -> !existingIds.contains(id)).distinct().toList();
    }

    public static Optional<MaterialForm> formByRegistryName(String id) {
        return Optional.ofNullable(FORMS_BY_ID.get(id));
    }

}
