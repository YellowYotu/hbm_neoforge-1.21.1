package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialScrapsCoverageTest {
    @Test
    void everySmeltableAndAdditiveMaterialHasAScrapVariant() {
        long expected = HBMMaterialCatalog.all().stream()
                .filter(material -> material.kind() != MaterialKind.NON_SMELTABLE)
                .count();
        long actual = MaterialScrapCatalog.all().size();
        assertEquals(expected, actual);
    }
}
