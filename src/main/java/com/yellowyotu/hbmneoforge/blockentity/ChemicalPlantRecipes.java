package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIcon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ChemicalPlantRecipes {
    public record Ingredient(Item item, int count) {
        public ItemStack displayStack() { return new ItemStack(item, count); }
    }
    public record FluidIngredient(NTMFluidType type, int amount) {}
    public record Recipe(String id, int duration, int energyPerTick, List<Ingredient> ingredients, List<FluidIngredient> inputFluids, ItemStack result, List<FluidIngredient> outputFluids) {
        public ItemStack resultStack() { return result.copy(); }
        public ItemStack displayStack() {
            if (!result.isEmpty()) {
                return result.copy();
            }
            if (outputFluids.isEmpty()) {
                return ItemStack.EMPTY;
            }
            FluidIngredient fluid = outputFluids.getFirst();
            return ItemFluidIcon.make(fluid.type(), fluid.amount());
        }
    }

    public static final List<Recipe> RECIPES = loadRecipes();

    private ChemicalPlantRecipes() {}

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("chemical_plant");
        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("chemical_plant", id);
            List<Ingredient> ingredients = new ArrayList<>();
            if (json.has("ingredients")) {
                JsonArray input = json.getAsJsonArray("ingredients");
                for (int j = 0; j < input.size(); j++) {
                    JsonObject ingredient = input.get(j).getAsJsonObject();
                    ingredients.add(new Ingredient(MachineRecipeJsonLoader.getItem(ingredient.get("item").getAsString()), ingredient.has("count") ? ingredient.get("count").getAsInt() : 1));
                }
            }
            List<FluidIngredient> inputFluids = readFluids(json, "input_fluids");
            if (json.has("water")) {
                inputFluids = new ArrayList<>(inputFluids);
                inputFluids.add(new FluidIngredient(NTMFluidType.WATER, json.get("water").getAsInt()));
            }
            ItemStack result = json.has("result") ? MachineRecipeJsonLoader.readStack(json.getAsJsonObject("result")) : ItemStack.EMPTY;
            recipes.add(new Recipe(id, json.get("duration").getAsInt(), json.get("energy_per_tick").getAsInt(), List.copyOf(ingredients), List.copyOf(inputFluids), result, List.copyOf(readFluids(json, "output_fluids"))));
        }
        return List.copyOf(recipes);
    }

    private static List<FluidIngredient> readFluids(JsonObject json, String key) {
        List<FluidIngredient> fluids = new ArrayList<>();
        if (!json.has(key)) {
            return fluids;
        }
        JsonArray array = json.getAsJsonArray(key);
        for (int i = 0; i < array.size(); i++) {
            JsonObject fluid = array.get(i).getAsJsonObject();
            NTMFluidType type = NTMFluidType.byId(fluid.get("fluid").getAsString());
            if (type != null) {
                fluids.add(new FluidIngredient(type, fluid.get("amount").getAsInt()));
            }
        }
        return fluids;
    }
}
