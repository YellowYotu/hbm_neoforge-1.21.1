package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
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
import net.minecraft.world.item.Items;

public final class BlastFurnaceRecipeCategory implements IRecipeCategory<BlastFurnaceRecipes.Recipe> {
    public static final RecipeType<BlastFurnaceRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "blast_furnace", BlastFurnaceRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_blast_furnace.png");
    private final IDrawable icon;
    private final IDrawableStatic background;

    public BlastFurnaceRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BLAST_FURNACE.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 72);
    }

    @Override public RecipeType<BlastFurnaceRecipes.Recipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.hbm_neoforge.blast_furnace"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 72; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceRecipes.Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 18).addItemStacks(toStacks(recipe.first()));
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 54).addItemStacks(toStacks(recipe.second()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 36).addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.CATALYST, 8, 36).addItemStacks(List.of(
                new ItemStack(Items.COAL),
                new ItemStack(Items.CHARCOAL),
                new ItemStack(Items.COAL_BLOCK),
                new ItemStack(Items.BLAZE_ROD),
                new ItemStack(Items.BLAZE_POWDER),
                new ItemStack(ModItems.COKE_COAL.get()),
                new ItemStack(ModItems.SOLID_FUEL.get())
        ));
    }

    @Override
    public void draw(BlastFurnaceRecipes.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);

        int fuelHeight = JeiAnimationHelper.filledHeight(52, 120);
        if (fuelHeight > 0) {
            graphics.blit(TEXTURE, 44, 70 - fuelHeight, 201, 53 - fuelHeight, 16, fuelHeight, 256, 256);
        }

        int progressWidth = JeiAnimationHelper.filledWidth(24, 80);
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, 101, 35, 176, 14, progressWidth + 1, 17, 256, 256);
        }

        graphics.blit(TEXTURE, 63, 37, 176, 0, 14, 14, 256, 256);
    }

    private static List<ItemStack> toStacks(BlastFurnaceRecipes.Input input) {
        return input.items().stream().map(ItemStack::new).toList();
    }
}
