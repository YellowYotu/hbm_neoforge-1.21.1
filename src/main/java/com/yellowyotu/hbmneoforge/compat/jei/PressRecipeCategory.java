package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
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

public final class PressRecipeCategory implements IRecipeCategory<MachinePressRecipes.Recipe> {
    public static final RecipeType<MachinePressRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "pressing", MachinePressRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/jei/press.png");
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableAnimated progress;

    public PressRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MACHINE_PRESS.get()));
        background = guiHelper.createDrawable(TEXTURE, 5, 11, 166, 65);
        IDrawableStatic progressStatic = guiHelper.createDrawable(TEXTURE, 0, 86, 18, 18);
        progress = guiHelper.createAnimatedDrawable(progressStatic, 20, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public RecipeType<MachinePressRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm_neoforge.press");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 166;
    }

    @Override
    public int getHeight() {
        return 65;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MachinePressRecipes.Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 42).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 6).addItemStacks(MachinePressRecipes.stampsFor(recipe.stampType()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 24).addItemStack(recipe.output());
    }

    @Override
    public void draw(MachinePressRecipes.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        progress.draw(graphics, 47, 24);
    }
}
