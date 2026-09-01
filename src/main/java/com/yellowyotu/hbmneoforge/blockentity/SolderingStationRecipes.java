package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class SolderingStationRecipes {
    public record Ingredient(Supplier<ItemStack> stack, int count) {
        public ItemStack displayStack() {
            ItemStack result = stack.get().copy();
            result.setCount(count);
            return result;
        }
    }

    public record FluidRequirement(ItemSolderingFluidCell.FluidType type, int amount) {
    }

    public record Recipe(String id, Supplier<ItemStack> result, int duration, int energyPerTick, FluidRequirement fluid, List<Ingredient> toppings, List<Ingredient> pcb, List<Ingredient> solder) {
        public ItemStack resultStack() {
            return result.get().copy();
        }
    }

    public static final List<Recipe> RECIPES = loadRecipes();

    private SolderingStationRecipes() {
    }

    public static Recipe find(ItemStackHandler inventory) {
        for (Recipe recipe : RECIPES) {
            if (matchesGroup(inventory, 0, 3, recipe.toppings()) && matchesGroup(inventory, 3, 2, recipe.pcb()) && matchesGroup(inventory, 5, 1, recipe.solder())) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isValidTopping(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.TOPPINGS);
    }

    public static boolean isValidPcb(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.PCB);
    }

    public static boolean isValidSolder(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.SOLDER);
    }

    private static boolean isValidForAnyGroup(ItemStack stack, Group group) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Recipe recipe : RECIPES) {
            List<Ingredient> ingredients = switch (group) {
                case TOPPINGS -> recipe.toppings();
                case PCB -> recipe.pcb();
                case SOLDER -> recipe.solder();
            };
            for (Ingredient ingredient : ingredients) {
                if (ItemStack.isSameItemSameComponents(stack, ingredient.stack().get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesGroup(ItemStackHandler inventory, int firstSlot, int slotCount, List<Ingredient> ingredients) {
        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (int slot = firstSlot; slot < firstSlot + slotCount; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty()) {
                continue;
            }

            Ingredient match = null;
            for (Ingredient ingredient : remaining) {
                if (input.getCount() >= ingredient.count() && ItemStack.isSameItemSameComponents(input, ingredient.stack().get())) {
                    match = ingredient;
                    break;
                }
            }
            if (match == null) {
                return false;
            }
            remaining.remove(match);
        }
        return remaining.isEmpty();
    }

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("soldering");
        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("soldering", id);
            ItemStack resultStack = MachineRecipeJsonLoader.readStack(json.getAsJsonObject("result"));
            int duration = json.get("duration").getAsInt();
            int energyPerTick = json.get("energy_per_tick").getAsInt();
            FluidRequirement fluid = json.has("fluid") ? readFluid(json.getAsJsonObject("fluid")) : null;
            List<Ingredient> toppings = readIngredients(json.getAsJsonArray("toppings"));
            List<Ingredient> pcb = readIngredients(json.getAsJsonArray("pcb"));
            List<Ingredient> solder = readIngredients(json.getAsJsonArray("solder"));
            recipes.add(new Recipe(id, () -> resultStack.copy(), duration, energyPerTick, fluid, toppings, pcb, solder));
        }
        return List.copyOf(recipes);
    }

    private static FluidRequirement readFluid(JsonObject json) {
        ItemSolderingFluidCell.FluidType type = ItemSolderingFluidCell.FluidType.valueOf(json.get("type").getAsString().toUpperCase());
        return new FluidRequirement(type, json.get("amount").getAsInt());
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

    private enum Group {
        TOPPINGS,
        PCB,
        SOLDER
    }
}
