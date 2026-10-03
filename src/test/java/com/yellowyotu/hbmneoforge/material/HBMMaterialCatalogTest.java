package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.yellowyotu.hbmneoforge.material.MaterialShape.*;
import static org.junit.jupiter.api.Assertions.*;

class HBMMaterialCatalogTest {
    @Test void matchesRepresentativeAutogenShapes() {
        assertEquals(Set.of(FRAGMENT, WIRE, DUST, PLATE, DENSE_WIRE, CAST_PLATE, WELDED_PLATE, SHELL, PIPE, BLOCK), shapes("copper"));
        assertEquals(Set.of(FRAGMENT, NUGGET, TINY_POWDER, INGOT, DUST, DENSE_WIRE, BLOCK), shapes("neodymium"));
        assertEquals(Set.of(FRAGMENT, NUGGET, TINY_POWDER, DUST, DENSE_WIRE, BLOCK), shapes("niobium"));
        assertEquals(Set.of(FRAGMENT, NUGGET, TINY_POWDER, BILLET, DUST, BLOCK), shapes("cobalt"));
        assertEquals(Set.of(FRAGMENT, TINY_POWDER, DUST, BLOCK), shapes("boron"));
        assertEquals(Set.of(FRAGMENT, BLOCK), shapes("lanthanium"));
    }

    @Test void containsAllNineDedicatedFragments() {
        assertTrue(HBMMaterialCatalog.dedicatedFragmentMaterials().containsAll(Set.of(
                "neodymium", "cobalt", "niobium", "cerium", "lanthanium",
                "actinium", "meteorite", "boron", "coltan")));
        assertEquals(9, HBMMaterialCatalog.dedicatedFragmentMaterials().size());
    }

    @Test void neverGeneratesBedrockOreFragments() {
        assertTrue(HBMMaterialCatalog.forms().stream().noneMatch(form -> form.registryName().contains("bedrock")));
    }

    @Test void catalogAndFormsAreDeterministicAndUnique() {
        List<String> first = HBMMaterialCatalog.forms().stream().map(MaterialForm::registryName).toList();
        List<String> second = HBMMaterialCatalog.forms().stream().map(MaterialForm::registryName).toList();
        assertEquals(first, second);
        assertEquals(first.size(), Set.copyOf(first).size());
    }

    private static Set<MaterialShape> shapes(String id) {
        return HBMMaterialCatalog.get(id).orElseThrow().shapes();
    }
}
