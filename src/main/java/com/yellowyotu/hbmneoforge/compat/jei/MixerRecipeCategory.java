package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.MixerRecipes;
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

public final class MixerRecipeCategory implements IRecipeCategory<MixerRecipes.Recipe> {
    public static final RecipeType<MixerRecipes.Recipe> RECIPE_TYPE = RecipeType.create(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "mixer", MixerRecipes.Recipe.class);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_mixer.png");
    private final IDrawable icon;
    private final IDrawable background;

    public MixerRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.MIXER.get()));
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 176, 100);
    }

    @Override
    public RecipeType<MixerRecipes.Recipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm_neoforge.mixer");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipes.Recipe recipe, IFocusGroup focuses) {
        if (recipe.solidInput() != null) {
            builder.addSlot(RecipeIngredientRole.INPUT, 43, 77).addIngredients(recipe.solidInput().ingredient());
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 77).addItemStacks(JeiAnimationHelper.batteryStacks());
        if (recipe.input1() != null) {
            addFluidSearchIngredients(builder, RecipeIngredientRole.INPUT, recipe.input1().type());
        }
        if (recipe.input2() != null) {
            addFluidSearchIngredients(builder, RecipeIngredientRole.INPUT, recipe.input2().type());
        }
        addFluidSearchIngredients(builder, RecipeIngredientRole.OUTPUT, recipe.outputType());
    }

    @Override
    public void draw(MixerRecipes.Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        int powerHeight = JeiAnimationHelper.filledHeight(52, 160);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, 23, 75 - powerHeight, 176, 52 - powerHeight, 16, powerHeight, 256, 256);
        }
        int progressWidth = JeiAnimationHelper.filledWidth(53, Math.max(20, recipe.processTime()));
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, 62, 36, 192, 0, progressWidth, 44, 256, 256);
        }
        if (recipe.input1() != null) {
            drawFluidTank(graphics, recipe.input1().type(), recipe.input1().amount(), 43, 23, 7, 52, 16_000);
            drawFluidTooltip(graphics, recipe.input1().type(), recipe.input1().amount(), mouseX, mouseY, 43, 23, 7, 52);
        }
        if (recipe.input2() != null) {
            drawFluidTank(graphics, recipe.input2().type(), recipe.input2().amount(), 52, 23, 7, 52, 16_000);
            drawFluidTooltip(graphics, recipe.input2().type(), recipe.input2().amount(), mouseX, mouseY, 52, 23, 7, 52);
        }
        drawFluidTank(graphics, recipe.outputType(), recipe.outputAmount(), 117, 23, 16, 52, 24_000);
        drawFluidTooltip(graphics, recipe.outputType(), recipe.outputAmount(), mouseX, mouseY, 117, 23, 16, 52);
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
