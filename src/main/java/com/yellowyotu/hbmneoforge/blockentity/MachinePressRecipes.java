package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.item.ItemStamp;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MachinePressRecipes {
    private static final String RECIPE_ROOT = "data/" + HBMsNuclearTechModUnofficialNeoForgeEdition.MODID + "/machine_recipe/press/";
    private static final List<Recipe> RECIPES = loadRecipes();

    private MachinePressRecipes() {
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    public static ItemStack getResult(ItemStack input, ItemStack stamp) {
        if (!(stamp.getItem() instanceof ItemStamp itemStamp)) {
            return ItemStack.EMPTY;
        }
        for (Recipe recipe : RECIPES) {
            if (recipe.stampType() == itemStamp.getStampType(stamp) && input.is(recipe.input().getItem())) {
                return recipe.output().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<ItemStack> stampsFor(ItemStamp.StampType type) {
        return switch (type) {
            case FLAT -> List.of(new ItemStack(ModItems.STAMP_IRON_FLAT.get()), new ItemStack(ModItems.STAMP_STEEL_FLAT.get()));
            case PLATE -> List.of(new ItemStack(ModItems.IRON_PLATE_STAMP.get()), new ItemStack(ModItems.STAMP_STEEL_PLATE.get()));
            case WIRE -> List.of(new ItemStack(ModItems.STAMP_IRON_WIRE.get()), new ItemStack(ModItems.STAMP_STEEL_WIRE.get()));
            case CIRCUIT -> List.of(new ItemStack(ModItems.STAMP_IRON_CIRCUIT.get()), new ItemStack(ModItems.STAMP_STEEL_CIRCUIT.get()));
            case PRINTING1, PRINTING2, PRINTING3, PRINTING4, PRINTING5, PRINTING6, PRINTING7, PRINTING8 -> {
                ItemStack book = new ItemStack(ModItems.STAMP_BOOK.get());
                book.setDamageValue(type.ordinal() - ItemStamp.StampType.PRINTING1.ordinal());
                yield List.of(book);
            }
        };
    }

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = readJsonArray(RECIPE_ROOT + "index.json");
        for (int i = 0; i < index.size(); i++) {
            String name = index.get(i).getAsString();
            JsonObject json = readJsonObject(RECIPE_ROOT + name + ".json");
            Item input = getItem(json.get("input").getAsString());
            ItemStamp.StampType stampType = ItemStamp.StampType.valueOf(json.get("stamp").getAsString().toUpperCase());
            JsonObject outputJson = json.getAsJsonObject("output");
            Item output = getItem(outputJson.get("item").getAsString());
            int count = outputJson.has("count") ? outputJson.get("count").getAsInt() : 1;
            recipes.add(new Recipe(new ItemStack(input), stampType, new ItemStack(output, count)));
        }
        return List.copyOf(recipes);
    }

    private static Item getItem(String id) {
        ResourceLocation location = toResourceLocation(id);
        Item item = BuiltInRegistries.ITEM.get(location);
        if (item == null) {
            throw new IllegalStateException("Unknown press recipe item: " + id);
        }
        return item;
    }

    private static ResourceLocation toResourceLocation(String id) {
        int separator = id.indexOf(':');
        if (separator < 0) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", id);
        }
        return ResourceLocation.fromNamespaceAndPath(id.substring(0, separator), id.substring(separator + 1));
    }

    private static JsonArray readJsonArray(String path) {
        try (InputStream stream = openResource(path); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read press recipe index: " + path, exception);
        }
    }

    private static JsonObject readJsonObject(String path) {
        try (InputStream stream = openResource(path); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read press recipe: " + path, exception);
        }
    }

    private static InputStream openResource(String path) {
        InputStream stream = MachinePressRecipes.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing press recipe resource: " + path);
        }
        return stream;
    }

    public record Recipe(ItemStack input, ItemStamp.StampType stampType, ItemStack output) {
    }
}
