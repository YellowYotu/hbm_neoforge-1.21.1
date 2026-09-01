package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AssemblyMachineRecipes {
    public record Ingredient(Supplier<ItemStack> stack, int count) {
        public ItemStack displayStack() {
            ItemStack result = stack.get().copy();
            result.setCount(count);
            return result;
        }
    }

    public record Recipe(String id, Supplier<ItemStack> result, int duration, int energyPerTick, List<Ingredient> ingredients) {
        public ItemStack resultStack() {
            return result.get().copy();
        }
    }

    public static final List<Recipe> RECIPES = loadRecipes();

    private AssemblyMachineRecipes() {
    }

    public static Recipe get(int index) {
        return RECIPES.get(Math.floorMod(index, RECIPES.size()));
    }

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("assembly");
        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("assembly", id);
            ItemStack resultStack = MachineRecipeJsonLoader.readStack(json.getAsJsonObject("result"));
            int duration = json.get("duration").getAsInt();
            int energyPerTick = json.get("energy_per_tick").getAsInt();
            List<Ingredient> ingredients = readIngredients(json.getAsJsonArray("ingredients"));
            recipes.add(new Recipe(id, stackSupplier(resultStack), duration, energyPerTick, ingredients));
        }
        return List.copyOf(recipes);
    }

    private static List<Ingredient> readIngredients(JsonArray json) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
            JsonObject ingredient = json.get(i).getAsJsonObject();
            Item item = MachineRecipeJsonLoader.getItem(ingredient.get("item").getAsString());
            int count = ingredient.has("count") ? ingredient.get("count").getAsInt() : 1;
            ingredients.add(new Ingredient(() -> new ItemStack(item), count));
        }
        return List.copyOf(ingredients);
    }

    private static Supplier<ItemStack> stackSupplier(ItemStack stack) {
        return () -> stack.copy();
    }
}
