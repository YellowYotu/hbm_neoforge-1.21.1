package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIcon;
import com.yellowyotu.hbmneoforge.item.ItemPortableFluidContainer;
import com.yellowyotu.hbmneoforge.blockentity.BlastFurnaceRecipes;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilRecipes;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressRecipes;
import com.yellowyotu.hbmneoforge.blockentity.ShredderRecipes;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationRecipes;
import com.yellowyotu.hbmneoforge.client.screen.AssemblyMachineScreen;
import com.yellowyotu.hbmneoforge.client.screen.BlastFurnaceScreen;
import com.yellowyotu.hbmneoforge.client.screen.ChemicalPlantScreen;
import com.yellowyotu.hbmneoforge.client.screen.HBMAnvilScreen;
import com.yellowyotu.hbmneoforge.client.screen.MachinePressScreen;
import com.yellowyotu.hbmneoforge.client.screen.ShredderScreen;
import com.yellowyotu.hbmneoforge.client.screen.SolderingStationScreen;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
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
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        var iconInterpreter = fluidSubtypeInterpreter(true);
        var containerInterpreter = fluidSubtypeInterpreter(false);
        registration.registerSubtypeInterpreter(ModItems.FLUID_ICON.get(), iconInterpreter);
        registration.registerSubtypeInterpreter(ModItems.FLUID_TANK_FULL.get(), containerInterpreter);
        registration.registerSubtypeInterpreter(ModItems.FLUID_TANK_LEAD_FULL.get(), containerInterpreter);
        registration.registerSubtypeInterpreter(ModItems.FLUID_BARREL_FULL.get(), containerInterpreter);
        registration.registerSubtypeInterpreter(ModItems.FLUID_PACK_FULL.get(), containerInterpreter);
    }

    private static mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<ItemStack> fluidSubtypeInterpreter(boolean icon) {
        return new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack ingredient, mezz.jei.api.ingredients.subtypes.UidContext context) {
                NTMFluidType type = icon ? ItemFluidIcon.getFluidType(ingredient) : ItemPortableFluidContainer.getFluidType(ingredient);
                return type == null ? "" : type.id();
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, mezz.jei.api.ingredients.subtypes.UidContext context) {
                NTMFluidType type = icon ? ItemFluidIcon.getFluidType(ingredient) : ItemPortableFluidContainer.getFluidType(ingredient);
                return type == null ? "" : type.id();
            }
        };
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> stacks = new java.util.ArrayList<>();
        for (NTMFluidType type : NTMFluidType.values()) {
            stacks.add(ItemFluidIcon.make(type, 0));
            stacks.add(ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_FULL.get(), type, 1_000));
            stacks.add(ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_LEAD_FULL.get(), type, 1_000));
            stacks.add(ItemPortableFluidContainer.configured(ModItems.FLUID_BARREL_FULL.get(), type, 16_000));
            stacks.add(ItemPortableFluidContainer.configured(ModItems.FLUID_PACK_FULL.get(), type, 32_000));
        }
        registration.addExtraItemStacks(stacks);
    }
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PressRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ShredderRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SolderingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AnvilRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new AssemblyRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BlastFurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ChemicalPlantRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FoundryCastingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PressRecipeCategory.RECIPE_TYPE, MachinePressRecipes.all());
        registration.addRecipes(ShredderRecipeCategory.RECIPE_TYPE, ShredderRecipes.all());
        registration.addRecipes(SolderingRecipeCategory.RECIPE_TYPE, SolderingStationRecipes.RECIPES);
        registration.addRecipes(AnvilRecipeCategory.RECIPE_TYPE, HBMAnvilRecipes.all());
        registration.addRecipes(AssemblyRecipeCategory.RECIPE_TYPE, com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes.RECIPES);
        registration.addRecipes(BlastFurnaceRecipeCategory.RECIPE_TYPE, BlastFurnaceRecipes.all());
        registration.addRecipes(ChemicalPlantRecipeCategory.RECIPE_TYPE, com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes.RECIPES);
        registration.addRecipes(FoundryCastingRecipeCategory.RECIPE_TYPE, FoundryCastingRecipes.all());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MACHINE_PRESS.get()), PressRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SHREDDER.get()), ShredderRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SOLDERING_STATION.get()), SolderingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ANVIL_IRON.get()), AnvilRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ASSEMBLY_MACHINE.get()), AssemblyRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLAST_FURNACE.get()), BlastFurnaceRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHEMICAL_PLANT.get()), ChemicalPlantRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUCIBLE.get()), FoundryCastingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FOUNDRY_MOLD.get()), FoundryCastingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FOUNDRY_BASIN.get()), FoundryCastingRecipeCategory.RECIPE_TYPE);
    }
}
