package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ArcWelderRecipes {
    public static final List<Recipe> RECIPES = List.of(
            recipe(new ItemStack(ModItems.MOTOR.get(), 2), 100, 400, ingredient(ModItems.PLATE_STEEL.get(), 2), ingredient(ModItems.WIRE_DENSE_MINGRADE.get(), 2)),
            recipe(new ItemStack(ModItems.WIRE_DENSE_COPPER.get()), 100, 10_000, ingredient(ModItems.WIRE_COPPER.get(), 8)),
            recipe(new ItemStack(ModItems.WIRE_DENSE_MINGRADE.get()), 100, 10_000, ingredient(ModItems.WIRE_RED_COPPER.get(), 8)),
            recipe(new ItemStack(ModItems.WIRE_DENSE_GOLD.get()), 100, 10_000, ingredient(ModItems.WIRE_GOLD.get(), 8)),
            recipe(new ItemStack(ModItems.PLATE_WELDED_IRON.get()), 100, 100, ingredient(ModItems.PLATE_CAST_IRON.get(), 2)),
            recipe(new ItemStack(ModItems.PLATE_WELDED_STEEL.get()), 100, 500, ingredient(ModItems.PLATE_STEEL_CAST.get(), 2)),
            recipe(new ItemStack(ModItems.PLATE_WELDED_COPPER.get()), 200, 1_000, ingredient(ModItems.PLATE_COPPER_CAST.get(), 2)),
            recipe(new ItemStack(ModItems.PLATE_WELDED_TITANIUM.get()), 600, 50_000, ingredient(ModItems.PLATE_TITANIUM_CAST.get(), 2)),
            recipe(new ItemStack(ModItems.PLATE_WELDED_ALUMINIUM.get()), 300, 10_000, ingredient(ModItems.PLATE_ALUMINIUM_CAST.get(), 2)),
            recipeWithFluid(new ItemStack(ModItems.PLATE_WELDED_TUNGSTEN.get()), 1_200, 250_000, NTMFluidType.OXYGEN, 1_000, ingredient(ModItems.PLATE_TUNGSTEN_CAST.get(), 2))
    );

    private ArcWelderRecipes() {
    }

    public static List<Recipe> all() {
        return RECIPES;
    }


    public static boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream().flatMap(recipe -> recipe.ingredients().stream()).anyMatch(ingredient -> stack.is(ingredient.item()));
    }

    public static boolean isValidInputForSlot(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot < 0 || slot > 2) {
            return false;
        }
        return RECIPES.stream().anyMatch(recipe -> slot < recipe.ingredients().size() && stack.is(recipe.ingredients().get(slot).item()));
    }

    public static Recipe find(ItemStackHandler inventory) {
        List<ItemStack> inputs = new ArrayList<>(3);
        inputs.add(inventory.getStackInSlot(0));
        inputs.add(inventory.getStackInSlot(1));
        inputs.add(inventory.getStackInSlot(2));

        for (Recipe recipe : RECIPES) {
            if (recipe.matches(inputs)) {
                return recipe;
            }
        }
        return null;
    }

    private static Ingredient ingredient(net.minecraft.world.item.Item item, int count) {
        return new Ingredient(item, count);
    }

    private static Recipe recipe(ItemStack output, int duration, int consumption, Ingredient... ingredients) {
        return new Recipe(List.of(ingredients), null, 0, output, duration, consumption);
    }

    private static Recipe recipeWithFluid(ItemStack output, int duration, int consumption, NTMFluidType fluid, int fluidAmount, Ingredient... ingredients) {
        return new Recipe(List.of(ingredients), fluid, fluidAmount, output, duration, consumption);
    }

    public record Ingredient(net.minecraft.world.item.Item item, int count) {
        public boolean matches(ItemStack stack) {
            return stack.is(item) && stack.getCount() >= count;
        }

        public ItemStack displayStack() {
            return new ItemStack(item, count);
        }
    }

    public record Recipe(List<Ingredient> ingredients, NTMFluidType fluid, int fluidAmount, ItemStack output, int duration, int consumption) {
        public boolean matches(List<ItemStack> inputs) {
            List<Ingredient> remaining = new ArrayList<>(ingredients);
            for (ItemStack stack : inputs) {
                if (stack.isEmpty()) {
                    continue;
                }
                int match = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    if (remaining.get(i).matches(stack)) {
                        match = i;
                        break;
                    }
                }
                if (match < 0) {
                    return false;
                }
                remaining.remove(match);
            }
            return remaining.isEmpty();
        }
    }
}
