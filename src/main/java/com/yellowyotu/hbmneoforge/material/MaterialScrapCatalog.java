package com.yellowyotu.hbmneoforge.material;

import java.util.List;

public final class MaterialScrapCatalog {
    private MaterialScrapCatalog() {}

    public static boolean supports(HBMMaterialDefinition material) {
        return material.kind() == MaterialKind.SMELTABLE || material.kind() == MaterialKind.ADDITIVE;
    }

    public static List<HBMMaterialDefinition> all() {
        return HBMMaterialCatalog.all().stream().filter(MaterialScrapCatalog::supports).toList();
    }
}
