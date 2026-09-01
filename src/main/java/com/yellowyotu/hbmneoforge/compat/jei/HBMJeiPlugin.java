package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.BlastFurnaceRecipes;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilRecipes;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressRecipes;
import com.yellowyotu.hbmneoforge.blockentity.ShredderRecipes;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationRecipes;
import com.yellowyotu.hbmneoforge.client.screen.AssemblyMachineScreen;
import com.yellowyotu.hbmneoforge.client.screen.BlastFurnaceScreen;
import com.yellowyotu.hbmneoforge.client.screen.HBMAnvilScreen;
import com.yellowyotu.hbmneoforge.client.screen.MachinePressScreen;
import com.yellowyotu.hbmneoforge.client.screen.ShredderScreen;
import com.yellowyotu.hbmneoforge.client.screen.SolderingStationScreen;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class HBMJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PressRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ShredderRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SolderingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AnvilRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AssemblyRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BlastFurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PressRecipeCategory.RECIPE_TYPE, MachinePressRecipes.all());
        registration.addRecipes(ShredderRecipeCategory.RECIPE_TYPE, ShredderRecipes.all());
        registration.addRecipes(SolderingRecipeCategory.RECIPE_TYPE, SolderingStationRecipes.RECIPES);
        registration.addRecipes(AnvilRecipeCategory.RECIPE_TYPE, HBMAnvilRecipes.all());
        registration.addRecipes(AssemblyRecipeCategory.RECIPE_TYPE, com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes.RECIPES);
        registration.addRecipes(BlastFurnaceRecipeCategory.RECIPE_TYPE, BlastFurnaceRecipes.all());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SolderingStationScreen.class, 72, 29, 32, 13, SolderingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(MachinePressScreen.class, 47, 24, 20, 20, PressRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(ShredderScreen.class, 63, 89, 54, 18, ShredderRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(HBMAnvilScreen.class, 52, 53, 18, 18, AnvilRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(AssemblyMachineScreen.class, 62, 126, 68, 16, AssemblyRecipeCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(BlastFurnaceScreen.class, 101, 35, 25, 17, BlastFurnaceRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PRESS.get()), PressRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SHREDDER.get()), ShredderRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SOLDERING_STATION.get()), SolderingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ANVIL_IRON.get()), AnvilRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_MACHINE.get()), AssemblyRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLAST_FURNACE.get()), BlastFurnaceRecipeCategory.RECIPE_TYPE);
    }
}
