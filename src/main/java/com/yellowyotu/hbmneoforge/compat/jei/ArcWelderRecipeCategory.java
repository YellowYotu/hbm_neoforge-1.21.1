package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderRecipes;
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

public final class ArcWelderRecipeCategory implements IRecipeCategory<ArcWelderRecipes.Recipe> {
    public static final RecipeType<ArcWelderRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "arc_welder", ArcWelderRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_arc_welder.png");
    private static final int TEXTURE_SIZE = 256;
    private final IDrawable icon;
    private final IDrawable background;

    public ArcWelderRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ARC_WELDER.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 100);
    }

    @Override public RecipeType<ArcWelderRecipes.Recipe> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.hbm_neoforge.arc_welder"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 176; }
    @Override public int getHeight() { return 100; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ArcWelderRecipes.Recipe recipe, IFocusGroup focuses) {
        int[] xs = {16, 34, 52};
        for (int i = 0; i < recipe.ingredients().size() && i < 3; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, xs[i], 35).addItemStack(recipe.ingredients().get(i).displayStack());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 35).addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.INPUT, 151, 71).addItemStacks(JeiAnimationHelper.batteryStacks());
        if (recipe.fluid() != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(List.of(
                    ItemPortableFluidContainer.configured(ModItems.FLUID_TANK_FULL.get(), recipe.fluid(), 1_000),
                    ItemPortableFluidContainer.configured(ModItems.FLUID_BARREL_FULL.get(), recipe.fluid(), 16_000)));
        }
    }

    @Override
    public void draw(ArcWelderRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, -1, -1);
        int energyHeight = JeiAnimationHelper.filledHeight(52, Math.max(20, recipe.duration()));
        if (energyHeight > 0) {
            graphics.blit(TEXTURE, 151, 69 - energyHeight, 176, 52 - energyHeight, 16, energyHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        graphics.blit(TEXTURE, 156, 4, 176, 52, 9, 12, TEXTURE_SIZE, TEXTURE_SIZE);
        int progressWidth = JeiAnimationHelper.filledWidth(33, Math.max(20, recipe.duration()));
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, 71, 37, 192, 0, progressWidth, 14, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        if (recipe.fluid() != null) {
            drawFluidTank(graphics, recipe.fluid(), recipe.fluidAmount(), 34, 78, 34, 16, 24_000);
            if (mouseX >= 34 && mouseX < 68 && mouseY >= 78 && mouseY < 94) {
                graphics.renderTooltip(Minecraft.getInstance().font, List.of(recipe.fluid().displayName(), Component.literal(recipe.fluidAmount() + " mB")), java.util.Optional.empty(), (int) mouseX, (int) mouseY);
            }
        }
    }

    private static void drawFluidTank(GuiGraphics graphics, NTMFluidType type, int amount, int x, int y, int width, int height, int capacity) {
        if (type == null || amount <= 0) return;
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
}
