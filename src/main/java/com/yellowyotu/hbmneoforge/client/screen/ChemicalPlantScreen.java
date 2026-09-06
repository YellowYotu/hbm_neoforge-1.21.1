package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.ChemicalPlantMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class ChemicalPlantScreen extends AbstractContainerScreen<ChemicalPlantMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_chemplant.png");

    public ChemicalPlantScreen(ChemicalPlantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 256;
        inventoryLabelY = 162;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        for (int i = 0; i < 3; i++) {
            renderTankTooltip(graphics, mouseX, mouseY, true, i, 8 + i * 18);
            renderTankTooltip(graphics, mouseX, mouseY, false, i, 80 + i * 18);
        }
        if (isHovering(152, 18, 16, 61, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + "/" + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
        }
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            ChemicalPlantRecipes.Recipe recipe = menu.getRecipe();
            if (recipe == null) {
                graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.chemical_plant.select_recipe"), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font, recipeTooltip(recipe), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, boolean input, int slot, int x) {
        if (!isHovering(x, 18, 16, 34, mouseX, mouseY)) {
            return;
        }
        NTMFluidType type = menu.getFluidType(input, slot);
        int amount = menu.getFluidAmount(input, slot);
        if (type == null) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.fluid.empty"), mouseX, mouseY);
        } else {
            graphics.renderTooltip(font, java.util.List.of(type.displayName(), Component.literal(amount + "/24000 mB")), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int powerHeight = menu.getMaxEnergy() <= 0 ? 0 : menu.getEnergy() * 61 / menu.getMaxEnergy();
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 79 - powerHeight, 176, 61 - powerHeight, 16, powerHeight, 256, 256);
        }
        int progressWidth = menu.getMaxProgress() <= 0 ? 0 : menu.getProgress() * 70 / menu.getMaxProgress();
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 126, 176, 61, progressWidth, 16, 256, 256);
        }
        for (int i = 0; i < 3; i++) {
            renderTank(graphics, true, i, 8 + i * 18);
            renderTank(graphics, false, i, 80 + i * 18);
        }
        ChemicalPlantRecipes.Recipe recipe = menu.getRecipe();
        if (recipe != null) {
            graphics.renderItem(recipe.displayStack(), leftPos + 8, topPos + 126);
            for (int i = 0; i < recipe.ingredients().size() && i < 3; i++) {
                int menuSlot = 1 + i;
                if (!menu.getSlot(menuSlot).hasItem()) {
                    ItemStack stack = recipe.ingredients().get(i).displayStack();
                    graphics.setColor(1.0F, 1.0F, 1.0F, 0.42F);
                    graphics.renderItem(stack, leftPos + 8 + i * 18, topPos + 99);
                    graphics.renderItemDecorations(font, stack, leftPos + 8 + i * 18, topPos + 99);
                    graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }

    private void renderTank(GuiGraphics graphics, boolean input, int slot, int x) {
        NTMFluidType type = menu.getFluidType(input, slot);
        if (type == null) {
            return;
        }
        int amount = menu.getFluidAmount(input, slot);
        int height = Math.min(34, Math.max(1, amount * 34 / 24_000));
        int color = 0xCC000000 | type.color();
        graphics.fill(leftPos + x, topPos + 52 - height, leftPos + x + 16, topPos + 52, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            minecraft.setScreen(new ChemicalRecipeSelectionScreen(this, menu));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 70 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 162, 0x404040, false);
    }
    static java.util.List<Component> recipeTooltip(ChemicalPlantRecipes.Recipe recipe) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(recipe.displayStack().getHoverName());
        if (!recipe.ingredients().isEmpty() || !recipe.inputFluids().isEmpty()) {
            lines.add(Component.translatable("gui.hbm_neoforge.chemical_plant.requires"));
            for (ChemicalPlantRecipes.Ingredient ingredient : recipe.ingredients()) {
                lines.add(Component.literal("  " + ingredient.count() + "x ").append(ingredient.displayStack().getHoverName()));
            }
            for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.inputFluids()) {
                lines.add(Component.literal("  " + fluid.amount() + " mB ").append(fluid.type().displayName()));
            }
        }
        if (!recipe.resultStack().isEmpty() || !recipe.outputFluids().isEmpty()) {
            lines.add(Component.translatable("gui.hbm_neoforge.chemical_plant.produces"));
            if (!recipe.resultStack().isEmpty()) {
                lines.add(Component.literal("  " + recipe.resultStack().getCount() + "x ").append(recipe.resultStack().getHoverName()));
            }
            for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.outputFluids()) {
                lines.add(Component.literal("  " + fluid.amount() + " mB ").append(fluid.type().displayName()));
            }
        }
        return lines;
    }

}
