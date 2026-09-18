package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.MixerBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.MixerRecipes;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.MixerMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MixerScreen extends AbstractContainerScreen<MixerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_mixer.png");

    public MixerScreen(MixerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        inventoryLabelY = 110;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        tankTooltip(graphics, mouseX, mouseY, 43, 23, 7, 52, menu.getInput1Type(), menu.getInput1Amount(), MixerBlockEntity.INPUT_CAPACITY);
        tankTooltip(graphics, mouseX, mouseY, 52, 23, 7, 52, menu.getInput2Type(), menu.getInput2Amount(), MixerBlockEntity.INPUT_CAPACITY);
        tankTooltip(graphics, mouseX, mouseY, 117, 23, 16, 52, menu.getOutputType(), menu.getOutputAmount(), MixerBlockEntity.OUTPUT_CAPACITY);
        if (isHovering(23, 22, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + "/" + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
        }
        if (isHovering(62, 22, 12, 12, mouseX, mouseY)) {
            List<MixerRecipes.Recipe> recipes = MixerRecipes.getRecipes(menu.getOutputType());
            if (recipes.size() > 1) {
                MixerRecipes.Recipe recipe = recipes.get(Math.floorMod(menu.getRecipeIndex(), recipes.size()));
                List<Component> lines = new ArrayList<>();
                lines.add(Component.literal("Current recipe (" + (Math.floorMod(menu.getRecipeIndex(), recipes.size()) + 1) + "/" + recipes.size() + ")"));
                if (recipe.input1() != null) lines.add(Component.literal("- ").append(recipe.input1().type().displayName()));
                if (recipe.input2() != null) lines.add(Component.literal("- ").append(recipe.input2().type().displayName()));
                if (recipe.solidInput() != null) lines.add(Component.literal("- ").append(recipe.solidInput().displayStack().getHoverName()));
                lines.add(Component.literal("Click to change"));
                graphics.renderTooltip(font, lines, java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int powerHeight = menu.getMaxEnergy() <= 0 ? 0 : menu.getEnergy() * 53 / menu.getMaxEnergy();
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 23, topPos + 75 - powerHeight, 176, 52 - powerHeight, 16, powerHeight, 256, 256);
        }
        int progressWidth = menu.getMaxProgress() <= 0 ? 0 : menu.getProgress() * 53 / menu.getMaxProgress();
        if (progressWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 36, 192, 0, progressWidth, 44, 256, 256);
        }
        renderTank(graphics, 43, 23, 7, 52, menu.getInput1Type(), menu.getInput1Amount(), MixerBlockEntity.INPUT_CAPACITY);
        renderTank(graphics, 52, 23, 7, 52, menu.getInput2Type(), menu.getInput2Amount(), MixerBlockEntity.INPUT_CAPACITY);
        renderTank(graphics, 117, 23, 16, 52, menu.getOutputType(), menu.getOutputAmount(), MixerBlockEntity.OUTPUT_CAPACITY);
    }

    private void renderTank(GuiGraphics graphics, int x, int y, int width, int height, NTMFluidType type, int amount, int capacity) {
        if (type == null || amount <= 0) return;
        int fillHeight = Math.min(height, Math.max(1, amount * height / capacity));
        graphics.fill(leftPos + x, topPos + y + height - fillHeight, leftPos + x + width, topPos + y + height, 0xCC000000 | type.color());
    }

    private void tankTooltip(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, NTMFluidType type, int amount, int capacity) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) return;
        if (type == null) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.fluid.empty"), mouseX, mouseY);
        } else {
            graphics.renderTooltip(font, List.of(type.displayName(), Component.literal(amount + "/" + capacity + " mB")), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(62, 22, 12, 12, mouseX, mouseY)) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 110, 0x404040, false);
    }
}
