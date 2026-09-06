package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.BlastFurnaceRecipes;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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

public final class BlastFurnaceRecipeCategory implements IRecipeCategory<BlastFurnaceRecipes.Recipe> {
    public static final RecipeType<BlastFurnaceRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "blast_furnace", BlastFurnaceRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_blast_furnace.png");
    private final IDrawable icon;
    private final IDrawableStatic background;

    public BlastFurnaceRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BLAST_FURNACE.get()));
        background = guiHelper.createDrawable(TEXTURE, 55, 10, 105, 58);
    }

    @Override
    public RecipeType<BlastFurnaceRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm_neoforge.blast_furnace");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 105;
    }

    @Override
    public int getHeight() {
        return 58;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceRecipes.Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 9).addItemStacks(toStacks(recipe.first()));
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 45).addItemStacks(toStacks(recipe.second()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 27).addItemStack(recipe.output());
    }

    @Override
    public void draw(BlastFurnaceRecipes.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
    }

    private static List<ItemStack> toStacks(BlastFurnaceRecipes.Input input) {
        return input.items().stream().map(ItemStack::new).toList();
    }
}
