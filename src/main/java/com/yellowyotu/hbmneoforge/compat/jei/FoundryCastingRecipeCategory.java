package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
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

public final class FoundryCastingRecipeCategory implements IRecipeCategory<FoundryCastingRecipeCategory.Recipe> {
    public static final RecipeType<Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "crucible_casting", Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/jei/gui_nei_foundry.png");
    private final IDrawable icon;
    private final IDrawable background;

    public FoundryCastingRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FOUNDRY_MOLD.get()));
        background = guiHelper.createDrawable(TEXTURE, 5, 11, 166, 65);
    }

    @Override public RecipeType<Recipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.literal("Crucible Casting"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 166; }
    @Override public int getHeight() { return 65; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 24).addItemStack(recipe.molten());
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 6).addItemStack(recipe.mold());
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 42).addItemStack(recipe.base());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 24).addItemStack(recipe.output());
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        JeiAnimationHelper.drawProgressArrow(graphics, 67, 28, 40);
    }

    public record Recipe(ItemStack molten, ItemStack mold, ItemStack base, ItemStack output) {
    }
}
