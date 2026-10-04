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
    private final IDrawableStatic slotBackground;

    public AnvilRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.ANVIL_IRON.get()));
        slotBackground = guiHelper.getSlotDrawable();

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
        int inputX;
        int inputY;
        int inputColumns;
        int outputX;
        int outputY;
        int outputColumns;
        int anvilX;
        int anvilY = 32;
        switch (recipe.overlayType()) {
            case SMITHING -> { inputX = 48; inputY = 24; inputColumns = 1; outputX = 102; outputY = 24; outputColumns = 1; anvilX = 75; }
            case RECYCLING -> { inputX = 12; inputY = 24; inputColumns = 1; outputX = 48; outputY = 6; outputColumns = 6; anvilX = 30; }
            case CONSTRUCTION -> { inputX = 12; inputY = 6; inputColumns = 6; outputX = 138; outputY = 24; outputColumns = 1; anvilX = 120; }
            default -> { inputX = 3; inputY = 6; inputColumns = 4; outputX = 93; outputY = 6; outputColumns = 4; anvilX = 75; }
        }

        java.util.ArrayList<ItemStack> inputs = new java.util.ArrayList<>();
        inputs.add(withCount(recipe.primaryDisplay().get(), recipe.primaryCount()));
        if (recipe.secondaryCount() > 0) inputs.add(withCount(recipe.secondaryDisplay().get(), recipe.secondaryCount()));
        for (HBMAnvilRecipes.Input input : recipe.extraInputs()) inputs.add(withCount(input.display().get(), input.count()));
        for (int index = 0; index < inputs.size(); index++) {
            builder.addSlot(RecipeIngredientRole.INPUT,
                    inputX + index % inputColumns * 18,
                    inputY + index / inputColumns * 18)
                    .setBackground(slotBackground, -1, -1)
                    .addItemStack(inputs.get(index));
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, anvilX, anvilY)
                .setBackground(slotBackground, -1, -1)
                .addItemStack(anvilForTier(recipe.tier()));

        for (int index = 0; index < recipe.outputs().size(); index++) {
            HBMAnvilRecipes.Output output = recipe.outputs().get(index);
            builder.addSlot(RecipeIngredientRole.OUTPUT,
                            outputX + index % outputColumns * 18,
                            outputY + index / outputColumns * 18)
                    .setBackground(slotBackground, -1, -1)
                    .addItemStack(output.stack().get());
        }
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

        switch (recipe.overlayType()) {
            case SMITHING -> {
                graphics.blit(TEXTURE, 47, 23, 113, 105, 18, 18, 256, 256);
                graphics.blit(TEXTURE, 101, 23, 113, 105, 18, 18, 256, 256);
                graphics.blit(TEXTURE, 74, 14, 149, 96, 18, 36, 256, 256);
            }
            case RECYCLING -> {
                graphics.blit(TEXTURE, 11, 23, 113, 105, 18, 18, 256, 256);
                graphics.blit(TEXTURE, 47, 5, 5, 87, 108, 54, 256, 256);
                graphics.blit(TEXTURE, 29, 14, 185, 96, 18, 36, 256, 256);
            }
            case CONSTRUCTION -> {
                graphics.blit(TEXTURE, 11, 5, 5, 87, 108, 54, 256, 256);
                graphics.blit(TEXTURE, 137, 23, 113, 105, 18, 18, 256, 256);
                graphics.blit(TEXTURE, 119, 14, 167, 96, 18, 36, 256, 256);
            }
            default -> {
                graphics.blit(TEXTURE, 2, 5, 5, 87, 72, 54, 256, 256);
                graphics.blit(TEXTURE, 92, 5, 5, 87, 72, 54, 256, 256);
                graphics.blit(TEXTURE, 74, 14, 131, 96, 18, 36, 256, 256);
            }
        }
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
