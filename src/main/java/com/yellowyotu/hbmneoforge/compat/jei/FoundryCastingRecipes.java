package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class FoundryCastingRecipes {
    private FoundryCastingRecipes() {
    }

    public static List<FoundryCastingRecipeCategory.Recipe> all() {
        List<FoundryCastingRecipeCategory.Recipe> recipes = new ArrayList<>();
        List<String> materials = List.of("iron", "gold", "copper", "steel", "titanium", "tungsten", "aluminium", "lead", "beryllium", "cobalt", "boron", "uranium", "plutonium", "red_copper", "dura_steel", "gunmetal");
        ItemStack[] molds = {
                new ItemStack(ModItems.MOLD_NUGGET.get()), new ItemStack(ModItems.MOLD_BILLET.get()), new ItemStack(ModItems.MOLD_INGOT.get()),
                new ItemStack(ModItems.MOLD_PLATE.get()), new ItemStack(ModItems.MOLD_WIRE.get()), new ItemStack(ModItems.MOLD_SHELL.get()),
                new ItemStack(ModItems.MOLD_PIPE.get()), new ItemStack(ModItems.MOLD_INGOTS.get()), new ItemStack(ModItems.MOLD_PLATES.get()),
                new ItemStack(ModItems.MOLD_BLOCK.get()), new ItemStack(ModItems.MOLD_PLATE_CAST.get()), new ItemStack(ModItems.MOLD_PLATES_CAST.get())
        };
        for (String material : materials) {
            ItemStack molten = materialStack(material);
            if (molten.isEmpty()) {
                continue;
            }
            for (ItemStack mold : molds) {
                boolean large = FoundryMaterialRegistry.isMold(mold, true);
                FoundryMaterialRegistry.MoldResult result = FoundryMaterialRegistry.getMoldResult(mold, material);
                if (result == null || result.output().isEmpty()) {
                    continue;
                }
                recipes.add(new FoundryCastingRecipeCategory.Recipe(molten.copy(), mold.copy(), new ItemStack(large ? ModBlocks.FOUNDRY_BASIN.get() : ModBlocks.FOUNDRY_MOLD.get()), result.output().copy()));
            }
        }
        return recipes;
    }

    private static ItemStack materialStack(String material) {
        String normalized = switch (material) {
            case "red_copper" -> "ingot_red_copper";
            case "dura_steel" -> "ingot_dura_steel";
            default -> "ingot_" + material;
        };
        var mod = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("hbm_neoforge", normalized));
        if (mod != null && mod != net.minecraft.world.item.Items.AIR) {
            return new ItemStack(mod);
        }
        var vanilla = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(material + "_ingot"));
        return vanilla == null || vanilla == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(vanilla);
    }
}
