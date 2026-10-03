package com.yellowyotu.hbmneoforge.material;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialResourceCoverageTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hbm_neoforge");

    @Test void everyGeneratedFormHasAnItemModel() {
        for (MaterialForm form : HBMMaterialCatalog.forms()) {
            Path model = ASSETS.resolve("models/item/" + form.registryName() + ".json");
            assertTrue(Files.isRegularFile(model), () -> "Missing item model: " + form.registryName());
        }
    }

    @Test void everyMaterialBlockHasPlacedBlockResourcesAndLoot() {
        for (MaterialForm form : HBMMaterialCatalog.forms()) {
            if (form.shape() != MaterialShape.BLOCK) continue;
            String id = form.registryName();
            assertTrue(Files.isRegularFile(ASSETS.resolve("blockstates/" + id + ".json")),
                    () -> "Missing blockstate: " + id);
            assertTrue(Files.isRegularFile(ASSETS.resolve("models/block/" + id + ".json")),
                    () -> "Missing block model: " + id);
            assertTrue(Files.isRegularFile(Path.of("src/main/resources/data/hbm_neoforge/loot_table/blocks/" + id + ".json")),
                    () -> "Missing block loot table: " + id);
        }
    }

    @Test void originalTexturesUsedByGeneratedModelsExist() throws Exception {
        for (MaterialForm form : HBMMaterialCatalog.forms()) {
            Path model = ASSETS.resolve("models/item/" + form.registryName() + ".json");
            if (!Files.isRegularFile(model)) continue;
            String json = Files.readString(model);
            String marker = "hbm_neoforge:item/";
            int start = json.indexOf(marker);
            if (start < 0) continue;
            start += marker.length();
            int end = json.indexOf('"', start);
            String texture = json.substring(start, end);
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/item/" + texture + ".png")),
                    () -> "Missing texture: " + texture + " for " + form.registryName());
        }
    }

    @Test void everyMaterialAndShapeHasEnglishAndRussianLocalization() throws Exception {
        String english = Files.readString(ASSETS.resolve("lang/en_us.json"));
        String russian = Files.readString(ASSETS.resolve("lang/ru_ru.json"));
        for (HBMMaterialDefinition material : HBMMaterialCatalog.all()) {
            String key = "material.hbm_neoforge." + material.id();
            assertTrue(english.contains('"' + key + '"'), () -> "Missing English material name: " + key);
            assertTrue(russian.contains('"' + key + '"'), () -> "Missing Russian material name: " + key);
        }
        for (MaterialShape shape : MaterialShape.values()) {
            if (HBMMaterialCatalog.forms().stream().noneMatch(form -> form.shape() == shape)) continue;
            String key = "material_form.hbm_neoforge." + shape.name().toLowerCase();
            assertTrue(english.contains('"' + key + '"'), () -> "Missing English shape name: " + key);
            assertTrue(russian.contains('"' + key + '"'), () -> "Missing Russian shape name: " + key);
        }
    }
}
