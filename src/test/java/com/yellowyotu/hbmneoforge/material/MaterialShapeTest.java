package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialShapeTest {
    @Test
    void usesExactHbmCeFoundryUnits() {
        assertEquals(432, MaterialShape.WELDED_PLATE.units());
        assertEquals(55_296, MaterialShape.HEAVY_COMPONENT.units());
        assertEquals(3, MaterialShape.PART.units());
    }
}
