package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationRecipes;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
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

public final class SolderingRecipeCategory implements IRecipeCategory<SolderingStationRecipes.Recipe> {
    public static final RecipeType<SolderingStationRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "soldering", SolderingStationRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_soldering_station.png");
    private static final int TEXTURE_SIZE = 256;
    private final IDrawable icon;
    private final IDrawableStatic background;

    public SolderingRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SOLDERING_STATION.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 100);
    }

    @Override public RecipeType<SolderingStationRecipes.Recipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.hbm_neoforge.soldering_station"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 100; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SolderingStationRecipes.Recipe recipe, IFocusGroup focuses) {
        addIngredientGroup(builder, recipe.toppings(), 17, 18, 3);
        addIngredientGroup(builder, recipe.pcb(), 17, 36, 2);
        addIngredientGroup(builder, recipe.solder(), 53, 36, 1);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 107, 27).addItemStack(recipe.resultStack());
        builder.addSlot(RecipeIngredientRole.INPUT, 152, 72).addItemStacks(JeiAnimationHelper.batteryStacks());
        if (recipe.fluid() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 17, 63).addItemStack(fluidCell(recipe.fluid().type()));
        }
    }

    private static void addIngredientGroup(IRecipeLayoutBuilder builder, List<SolderingStationRecipes.Ingredient> ingredients, int startX, int y, int maxSlots) {
        for (int i = 0; i < Math.min(ingredients.size(), maxSlots); i++) {
            SolderingStationRecipes.Ingredient ingredient = ingredients.get(i);
            ItemStack stack = ingredient.stack().get().copy();
            stack.setCount(ingredient.count());
            builder.addSlot(RecipeIngredientRole.INPUT, startX + i * 18, y).addItemStack(stack);
        }
    }

    private static ItemStack fluidCell(ItemSolderingFluidCell.FluidType type) {
        return switch (type) {
            case SULFURIC_ACID -> new ItemStack(ModItems.CELL_SULFURIC_ACID.get());
            case PEROXIDE -> new ItemStack(ModItems.CELL_PEROXIDE.get());
            case SOLVENT -> new ItemStack(ModItems.CELL_SOLVENT.get());
            case HELIUM4 -> new ItemStack(ModItems.CELL_HELIUM4.get());
            case PERFLUOROMETHYL -> new ItemStack(ModItems.CELL_PERFLUOROMETHYL.get());
            case PERFLUOROMETHYL_COLD -> new ItemStack(ModItems.CELL_PERFLUOROMETHYL_COLD.get());
        };
    }

    @Override
    public void draw(SolderingStationRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);

        int energyHeight = JeiAnimationHelper.filledHeight(52, Math.max(20, recipe.duration()));
        if (energyHeight > 0) {
            graphics.blit(TEXTURE, 152, 70 - energyHeight, 176, 52 - energyHeight, 16, energyHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        graphics.blit(TEXTURE, 156, 4, 176, 52, 9, 12, TEXTURE_SIZE, TEXTURE_SIZE);

        int progressWidth = JeiAnimationHelper.filledWidth(33, Math.max(20, recipe.duration()));
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, 72, 28, 192, 0, progressWidth, 14, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
