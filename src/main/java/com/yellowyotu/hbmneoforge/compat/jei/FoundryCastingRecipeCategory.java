package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class FoundryCastingRecipeCategory implements IRecipeCategory<FoundryCastingRecipeCategory.Recipe> {
    public static final RecipeType<Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "crucible_casting", Recipe.class);
    private final IDrawable icon;

    public FoundryCastingRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FOUNDRY_MOLD.get()));
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crucible Casting");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 74;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 29).addItemStack(recipe.molten());
        builder.addSlot(RecipeIngredientRole.CATALYST, 66, 8).addItemStack(recipe.mold());
        builder.addSlot(RecipeIngredientRole.CATALYST, 66, 48).addItemStack(recipe.base());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 29).addItemStack(recipe.output());
    }

    public record Recipe(ItemStack molten, ItemStack mold, ItemStack base, ItemStack output) {
    }
}
