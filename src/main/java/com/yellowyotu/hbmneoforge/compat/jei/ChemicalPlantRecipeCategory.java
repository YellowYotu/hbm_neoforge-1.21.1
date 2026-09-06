package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIcon;
import com.yellowyotu.hbmneoforge.item.ItemPortableFluidContainer;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ChemicalPlantRecipeCategory implements IRecipeCategory<ChemicalPlantRecipes.Recipe> {
    public static final RecipeType<ChemicalPlantRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "chemical_plant", ChemicalPlantRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_chemplant.png");
    private final IDrawable icon;
    private final IDrawable background;

    public ChemicalPlantRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CHEMICAL_PLANT.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 145);
    }

    @Override public RecipeType<ChemicalPlantRecipes.Recipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.hbm_neoforge.chemical_plant"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 145; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalPlantRecipes.Recipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.ingredients().size() && i < 3; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + i * 18, 99).addItemStack(recipe.ingredients().get(i).displayStack());
        }
        for (int i = 0; i < recipe.inputFluids().size() && i < 3; i++) {
            addFluid(builder, RecipeIngredientRole.INPUT, recipe.inputFluids().get(i), 8 + i * 18, 36);
        }
        if (!recipe.resultStack().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 99).addItemStack(recipe.resultStack());
        }
        for (int i = 0; i < recipe.outputFluids().size() && i < 3; i++) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, recipe.outputFluids().get(i), 80 + i * 18, 36);
        }
    }

    private static void addFluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, ChemicalPlantRecipes.FluidIngredient fluid, int x, int y) {
        builder.addSlot(role, x, y).addItemStack(ItemFluidIcon.make(fluid.type(), fluid.amount()));
        builder.addInvisibleIngredients(role).addItemStacks(fluidContainers(fluid.type()));
    }

    private static List<ItemStack> fluidContainers(NTMFluidType type) {
        return List.of(
            ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_FULL.get(), type, 1_000),
            ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_LEAD_FULL.get(), type, 1_000),
            ItemPortableFluidContainer.configured(ModItems.FLUID_BARREL_FULL.get(), type, 16_000),
            ItemPortableFluidContainer.configured(ModItems.FLUID_PACK_FULL.get(), type, 32_000)
        );
    }

    @Override
    public void draw(ChemicalPlantRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        graphics.renderItem(recipe.displayStack(), 8, 126);
    }
}
