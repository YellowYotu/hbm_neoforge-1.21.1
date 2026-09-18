package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
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
import net.minecraft.client.Minecraft;
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
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.inputFluids()) {
            addFluidSearchIngredients(builder, RecipeIngredientRole.INPUT, fluid.type());
        }
        if (!recipe.resultStack().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 99).addItemStack(recipe.resultStack());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 152, 81).addItemStacks(JeiAnimationHelper.batteryStacks());
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.outputFluids()) {
            addFluidSearchIngredients(builder, RecipeIngredientRole.OUTPUT, fluid.type());
        }
    }

    @Override
    public void draw(ChemicalPlantRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        int energyHeight = JeiAnimationHelper.filledHeight(61, Math.max(20, recipe.duration()));
        if (energyHeight > 0) {
            graphics.blit(TEXTURE, 152, 79 - energyHeight, 176, 61 - energyHeight, 16, energyHeight, 256, 256);
        }
        int progressWidth = JeiAnimationHelper.filledWidth(70, Math.max(20, recipe.duration()));
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, 62, 126, 176, 61, progressWidth, 16, 256, 256);
        }
        graphics.renderItem(recipe.displayStack(), 7, 125);
        for (int i = 0; i < recipe.inputFluids().size() && i < 3; i++) {
            ChemicalPlantRecipes.FluidIngredient fluid = recipe.inputFluids().get(i);
            int x = 8 + i * 18;
            drawFluidTank(graphics, fluid.type(), fluid.amount(), x, 18, 16, 34, 24_000);
            drawFluidTooltip(graphics, fluid.type(), fluid.amount(), mouseX, mouseY, x, 18, 16, 34);
        }
        for (int i = 0; i < recipe.outputFluids().size() && i < 3; i++) {
            ChemicalPlantRecipes.FluidIngredient fluid = recipe.outputFluids().get(i);
            int x = 80 + i * 18;
            drawFluidTank(graphics, fluid.type(), fluid.amount(), x, 18, 16, 34, 24_000);
            drawFluidTooltip(graphics, fluid.type(), fluid.amount(), mouseX, mouseY, x, 18, 16, 34);
        }
    }

    private static void addFluidSearchIngredients(IRecipeLayoutBuilder builder, RecipeIngredientRole role, NTMFluidType type) {
        builder.addInvisibleIngredients(role).addItemStacks(List.of(
                ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_FULL.get(), type, 1_000),
                ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_LEAD_FULL.get(), type, 1_000),
                ItemPortableFluidContainer.configured(ModItems.FLUID_BARREL_FULL.get(), type, 16_000),
                ItemPortableFluidContainer.configured(ModItems.FLUID_PACK_FULL.get(), type, 32_000)
        ));
    }

    private static void drawFluidTank(GuiGraphics graphics, NTMFluidType type, int amount, int x, int y, int width, int height, int capacity) {
        if (type == null || amount <= 0) {
            return;
        }
        int fillHeight = Math.min(height, Math.max(1, amount * height / capacity));
        int top = y + height - fillHeight;
        for (int yy = top; yy < y + height; yy += 16) {
            int drawHeight = Math.min(16, y + height - yy);
            for (int xx = x; xx < x + width; xx += 16) {
                int drawWidth = Math.min(16, x + width - xx);
                graphics.blit(type.iconTexture(), xx, yy, 0, 0, drawWidth, drawHeight, 16, 16);
            }
        }
    }

    private static void drawFluidTooltip(GuiGraphics graphics, NTMFluidType type, int amount, double mouseX, double mouseY, int x, int y, int width, int height) {
        if (type == null || mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
            return;
        }
        graphics.renderTooltip(Minecraft.getInstance().font, List.of(type.displayName(), Component.literal(amount + " mB")), java.util.Optional.empty(), (int) mouseX, (int) mouseY);
    }
}
