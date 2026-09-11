package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes;
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

public final class AssemblyRecipeCategory implements IRecipeCategory<AssemblyMachineRecipes.Recipe> {
    public static final RecipeType<AssemblyMachineRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "assembly", AssemblyMachineRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/jei/assembly.png");
    private final IDrawable icon;
    private final IDrawable background;

    public AssemblyRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ASSEMBLY_MACHINE.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 90);
    }

    @Override
    public RecipeType<AssemblyMachineRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm_neoforge.assembly");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 90;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblyMachineRecipes.Recipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 35 + i % 4 * 18, 17 + i / 4 * 18).addItemStack(recipe.ingredients().get(i).displayStack());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 144, 35).addItemStack(recipe.resultStack());
    }

    @Override
    public void draw(AssemblyMachineRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
    }
}
