package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes;
import com.yellowyotu.hbmneoforge.menu.AssemblyMachineMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AssemblyMachineScreen extends AbstractContainerScreen<AssemblyMachineMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_assembler.png");

    public AssemblyMachineScreen(AssemblyMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 256;
        inventoryLabelY = 162;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.assembler.select_recipe"), mouseX, mouseY);
        }
        if (isHovering(152, 18, 16, 61, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + "/" + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
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
        if (menu.getSelectedRecipe() >= 0) {
            AssemblyMachineRecipes.Recipe recipe = AssemblyMachineRecipes.get(menu.getSelectedRecipe());
            graphics.renderItem(recipe.resultStack(), leftPos + 8, topPos + 126);
            renderGhostIngredients(graphics, recipe);
        }
        if (menu.isProcessing()) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 121, 195, 0, 3, 6, 256, 256);
            graphics.blit(TEXTURE, leftPos + 56, topPos + 121, 195, 0, 3, 6, 256, 256);
        }
    }

    private void renderGhostIngredients(GuiGraphics graphics, AssemblyMachineRecipes.Recipe recipe) {
        for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size() && ingredientIndex < 12; ingredientIndex++) {
            if (menu.getSlot(AssemblyMachineBlockEntity.INPUT_START + ingredientIndex).hasItem()) {
                continue;
            }
            int x = leftPos + 8 + ingredientIndex % 3 * 18;
            int y = topPos + 18 + ingredientIndex / 3 * 18;
            ItemStack stack = recipe.ingredients().get(ingredientIndex).displayStack();
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.42F);
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            minecraft.setScreen(new AssemblyRecipeSelectionScreen(this, menu));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 70 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 162, 0x404040, false);
    }
}
