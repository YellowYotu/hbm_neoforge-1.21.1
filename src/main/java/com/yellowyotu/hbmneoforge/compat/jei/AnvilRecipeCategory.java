package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilRecipes;
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

public final class AnvilRecipeCategory implements IRecipeCategory<HBMAnvilRecipes.Recipe> {
    public static final RecipeType<HBMAnvilRecipes.Recipe> RECIPE_TYPE =
            RecipeType.create(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                    "anvil",
                    HBMAnvilRecipes.Recipe.class);

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                    "textures/gui/jei/anvil.png");

    private final IDrawable icon;
    private final IDrawableStatic background;

    public AnvilRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ANVIL_IRON.get()));

        background = guiHelper.createDrawable(
                TEXTURE,
                5,
                11,
                166,
                65);
    }

    @Override
    public RecipeType<HBMAnvilRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm_neoforge.anvil");
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
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            HBMAnvilRecipes.Recipe recipe,
            IFocusGroup focuses) {

        if (recipe.overlayType() == HBMAnvilRecipes.OverlayType.SMITHING) {
            builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            48,
                            24)
                    .addItemStack(
                            withCount(
                                    recipe.primaryDisplay().get(),
                                    recipe.primaryCount()));

            builder.addSlot(
                            RecipeIngredientRole.CATALYST,
                            75,
                            32)
                    .addItemStack(
                            anvilForTier(recipe.tier()));

            builder.addSlot(
                            RecipeIngredientRole.OUTPUT,
                            102,
                            24)
                    .addItemStack(
                            recipe.displayResult());

            return;
        }

        int slotX = 12;
        int slotY = 6;

        builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        slotX,
                        slotY)
                .addItemStack(
                        withCount(
                                recipe.primaryDisplay().get(),
                                recipe.primaryCount()));

        slotX += 18;

        if (recipe.secondaryCount() > 0) {
            builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            slotX,
                            slotY)
                    .addItemStack(
                            withCount(
                                    recipe.secondaryDisplay().get(),
                                    recipe.secondaryCount()));

            slotX += 18;
        }

        for (HBMAnvilRecipes.Input input : recipe.extraInputs()) {
            builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            slotX,
                            slotY)
                    .addItemStack(
                            withCount(
                                    input.display().get(),
                                    input.count()));

            slotX += 18;

            if (slotX > 102) {
                slotX = 12;
                slotY += 18;
            }
        }

        builder.addSlot(
                        RecipeIngredientRole.CATALYST,
                        120,
                        32)
                .addItemStack(
                        anvilForTier(recipe.tier()));

        builder.addSlot(
                        RecipeIngredientRole.OUTPUT,
                        138,
                        24)
                .addItemStack(
                        recipe.displayResult());
    }

    private ItemStack anvilForTier(int tier) {
        if (tier >= 2) {
            return new ItemStack(ModBlocks.ANVIL_STEEL.get());
        }

        return new ItemStack(ModBlocks.ANVIL_IRON.get());
    }

    @Override
    public void draw(
            HBMAnvilRecipes.Recipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {

        background.draw(graphics, 0, 0);

        if (recipe.overlayType() == HBMAnvilRecipes.OverlayType.SMITHING) {
            graphics.blit(
                    TEXTURE,
                    47,
                    23,
                    113,
                    105,
                    18,
                    18,
                    256,
                    256);

            graphics.blit(
                    TEXTURE,
                    101,
                    23,
                    113,
                    105,
                    18,
                    18,
                    256,
                    256);

            graphics.blit(
                    TEXTURE,
                    74,
                    14,
                    149,
                    96,
                    18,
                    36,
                    256,
                    256);

            return;
        }

        graphics.blit(
                TEXTURE,
                11,
                5,
                5,
                87,
                108,
                54,
                256,
                256);

        graphics.blit(
                TEXTURE,
                137,
                23,
                113,
                105,
                18,
                18,
                256,
                256);

        graphics.blit(
                TEXTURE,
                119,
                14,
                167,
                96,
                18,
                36,
                256,
                256);
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}