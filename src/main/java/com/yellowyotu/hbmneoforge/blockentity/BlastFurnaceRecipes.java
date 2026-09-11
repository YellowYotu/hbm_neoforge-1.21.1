package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BlastFurnaceRecipes {
    private static final List<Recipe> RECIPES = loadRecipes();

    private BlastFurnaceRecipes() {
    }

    public static Recipe find(ItemStack first, ItemStack second) {
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(first, second)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("blast_furnace");
        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("blast_furnace", id);
            recipes.add(new Recipe(id, readInput(json.getAsJsonObject("input_a")), readInput(json.getAsJsonObject("input_b")), MachineRecipeJsonLoader.readStack(json.getAsJsonObject("output"))));
        }
        return List.copyOf(recipes);
    }

    private static Input readInput(JsonObject json) {
        JsonArray itemsJson = json.getAsJsonArray("items");
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < itemsJson.size(); i++) {
            items.add(MachineRecipeJsonLoader.getItem(itemsJson.get(i).getAsString()));
        }
        return new Input(List.copyOf(items));
    }

    public record Recipe(String id, Input first, Input second, ItemStack output) {
        public boolean matches(ItemStack a, ItemStack b) {
            return first.matches(a) && second.matches(b) || first.matches(b) && second.matches(a);
        }
    }

    public record Input(List<Item> items) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && items.stream().anyMatch(stack::is);
        }
    }
}
