package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.item.ItemStamp;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MachinePressRecipes {
    private static final List<Recipe> RECIPES = createRecipes();

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
            if (recipe.stampType() == itemStamp.getStampType() && input.is(recipe.input().getItem())) {
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
        };
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(new Recipe(new ItemStack(Items.IRON_INGOT), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_IRON.get())));
        recipes.add(new Recipe(new ItemStack(Items.COPPER_INGOT), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_COPPER.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_COPPER.get()), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_COPPER.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_STEEL.get()), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_STEEL.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_TITANIUM.get()), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_TITANIUM.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_ALUMINIUM.get()), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_ALUMINIUM.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_LEAD.get()), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_LEAD.get())));
        recipes.add(new Recipe(new ItemStack(Items.GOLD_INGOT), ItemStamp.StampType.PLATE, new ItemStack(ModItems.PLATE_GOLD.get())));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_ALUMINIUM.get()), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_ALUMINIUM.get(), 8)));
        recipes.add(new Recipe(new ItemStack(Items.COPPER_INGOT), ItemStamp.StampType.FLAT, new ItemStack(ModItems.WIRE_RED_COPPER.get(), 8)));
        recipes.add(new Recipe(new ItemStack(Items.JUNGLE_LOG), ItemStamp.StampType.FLAT, new ItemStack(ModItems.BALL_RESIN.get())));
        recipes.add(new Recipe(new ItemStack(Items.COPPER_INGOT), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_COPPER.get(), 8)));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_COPPER.get()), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_COPPER.get(), 8)));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_TUNGSTEN.get()), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_TUNGSTEN.get(), 8)));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_LEAD.get()), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_FINE_LEAD.get(), 8)));
        recipes.add(new Recipe(new ItemStack(ModItems.INGOT_GRAPHITE.get()), ItemStamp.StampType.WIRE, new ItemStack(ModItems.WIRE_CARBON.get(), 8)));
        return List.copyOf(recipes);
    }

    public record Recipe(ItemStack input, ItemStamp.StampType stampType, ItemStack output) {
    }
}
