package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MaterialFormRegistryTest {
    @Test void skipsExistingIdsAndPlansOnlyMissingConcreteForms() {
        List<String> planned = MaterialFormRegistry.plannedRegistryNames(Set.of("fragment_cobalt", "ingot_neodymium"));
        assertFalse(planned.contains("fragment_cobalt"));
        assertFalse(planned.contains("ingot_neodymium"));
        assertTrue(planned.contains("wire_dense_neodymium"));
        assertTrue(planned.contains("powder_niobium_tiny"));
        assertEquals(planned.size(), Set.copyOf(planned).size());
    }

    @Test void doesNotInventOrdinaryWireForDenseWireMaterials() {
        Set<String> names = Set.copyOf(MaterialFormRegistry.plannedRegistryNames(Set.of()));
        assertTrue(names.contains("wire_dense_neodymium"));
        assertTrue(names.contains("wire_dense_niobium"));
        assertFalse(names.contains("wire_neodymium"));
        assertFalse(names.contains("wire_niobium"));
    }

    @Test void registryNamesFollowExistingHbmConventions() {
        assertEquals("fragment_cobalt", MaterialFormRegistry.registryName("cobalt", MaterialShape.FRAGMENT));
        assertEquals("powder_boron_tiny", MaterialFormRegistry.registryName("boron", MaterialShape.TINY_POWDER));
        assertEquals("wire_dense_tungsten", MaterialFormRegistry.registryName("tungsten", MaterialShape.DENSE_WIRE));
        assertEquals("plate_titanium_cast", MaterialFormRegistry.registryName("titanium", MaterialShape.CAST_PLATE));
        assertEquals("plate_titanium_welded", MaterialFormRegistry.registryName("titanium", MaterialShape.WELDED_PLATE));
    }

    @Test void generatedFormsCarryTheirOriginalMaterialColor() {
        MaterialForm form = MaterialFormRegistry.formByRegistryName("wire_dense_neodymium").orElseThrow();
        assertEquals(0x8F8F5F, form.material().color());
    }
}
