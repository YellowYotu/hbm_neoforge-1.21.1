package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class MachineRecipeJsonLoader {
    private static final String RECIPE_ROOT = "data/" + HBMsNuclearTechModUnofficialNeoForgeEdition.MODID + "/machine_recipe/";

    private MachineRecipeJsonLoader() {
    }

    static JsonArray readIndex(String folder) {
        return readJsonArray(RECIPE_ROOT + folder + "/index.json");
    }

    static JsonObject readRecipe(String folder, String name) {
        return readJsonObject(RECIPE_ROOT + folder + "/" + name + ".json");
    }

    static ItemStack readStack(JsonObject json) {
        Item item = getItem(json.get("item").getAsString());
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        return new ItemStack(item, count);
    }

    static Item getItem(String id) {
        ResourceLocation location = toResourceLocation(id);
        Item item = BuiltInRegistries.ITEM.get(location);
        if (item == Items.AIR && !location.equals(ResourceLocation.withDefaultNamespace("air"))) {
            throw new IllegalStateException("Unknown machine recipe item: " + id);
        }
        return item;
    }

    private static ResourceLocation toResourceLocation(String id) {
        int separator = id.indexOf(':');
        if (separator < 0) {
            return ResourceLocation.withDefaultNamespace(id);
        }
        return ResourceLocation.fromNamespaceAndPath(id.substring(0, separator), id.substring(separator + 1));
    }

    private static JsonArray readJsonArray(String path) {
        try (InputStream stream = openResource(path); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read machine recipe index: " + path, exception);
        }
    }

    private static JsonObject readJsonObject(String path) {
        try (InputStream stream = openResource(path); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read machine recipe: " + path, exception);
        }
    }

    private static InputStream openResource(String path) {
        InputStream stream = MachineRecipeJsonLoader.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing machine recipe resource: " + path);
        }
        return stream;
    }
}
