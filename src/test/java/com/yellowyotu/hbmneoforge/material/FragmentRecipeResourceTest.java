package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FragmentRecipeResourceTest {
    private static final Path SHREDDER = Path.of("src/main/resources/data/hbm_neoforge/machine_recipe/shredder");
    private static final Set<String> SHREDDABLE = Set.of(
            "neodymium", "cobalt", "niobium", "cerium", "lanthanium", "actinium", "boron", "meteorite");

    @Test
    void everyHbmCeShreddableFragmentHasItsOriginalTinyPowderRecipe() throws Exception {
        String index = Files.readString(SHREDDER.resolve("index.json"));
        for (String material : SHREDDABLE) {
            Path recipe = SHREDDER.resolve("fragment_" + material + ".json");
            assertTrue(Files.isRegularFile(recipe), () -> "Missing fragment shredder recipe: " + material);
            String json = Files.readString(recipe);
            assertTrue(json.contains("hbm_neoforge:fragment_" + material));
            assertTrue(json.contains("hbm_neoforge:powder_" + material + "_tiny"));
            assertTrue(index.contains('"' + "fragment_" + material + '"'));
        }
    }
}
