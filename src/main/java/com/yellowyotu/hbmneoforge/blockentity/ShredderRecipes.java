package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yellowyotu.hbmneoforge.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ShredderRecipes {
    public record Recipe(ItemStack input, ItemStack output) {
    }

    private static final List<Recipe> RECIPES = loadRecipes();

    private ShredderRecipes() {
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (Recipe recipe : RECIPES) {
            if (input.is(recipe.input().getItem())) {
                return recipe.output().copy();
            }
        }

        if (input.is(ItemTags.LOGS)) {
            return new ItemStack(ModItems.POWDER_SAWDUST.get(), 4);
        }
        if (input.is(ItemTags.PLANKS)) {
            return new ItemStack(ModItems.POWDER_SAWDUST.get());
        }
        if (input.is(ItemTags.SAPLINGS)) {
            return new ItemStack(Items.STICK);
        }
        if (input.is(ItemTags.WOOL)) {
            return new ItemStack(Items.STRING, 4);
        }

        return new ItemStack(ModItems.SCRAP.get());
    }

    public static boolean canShred(ItemStack input) {
        return !input.isEmpty() && !input.is(ModItems.BLADES_STEEL.get()) && !input.is(ModItems.BLADES_TITANIUM.get()) && !ItemBatteryPack.isBattery(input);
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    private static List<Recipe> loadRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("shredder");
        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("shredder", id);
            ItemStack input = MachineRecipeJsonLoader.readStack(json.getAsJsonObject("input"));
            ItemStack output = MachineRecipeJsonLoader.readStack(json.getAsJsonObject("output"));
            recipes.add(new Recipe(input, output));
        }
        return List.copyOf(recipes);
    }
}
