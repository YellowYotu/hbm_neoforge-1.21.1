package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.BlastFurnaceBlockEntity;
import com.yellowyotu.hbmneoforge.menu.BlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_blast_furnace.png");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.getFuel() > 0) {
            int fuelHeight = Mth.clamp(menu.getFuel() * 52 / BlastFurnaceBlockEntity.MAX_FUEL, 0, 52);
            graphics.blit(TEXTURE, leftPos + 44, topPos + 70 - fuelHeight, 201, 53 - fuelHeight, 16, fuelHeight, 256, 256);
        }
        int progressWidth = Mth.clamp(menu.getProgress() * 24 / BlastFurnaceBlockEntity.PROCESSING_TIME, 0, 24);
        graphics.blit(TEXTURE, leftPos + 101, topPos + 35, 176, 14, progressWidth + 1, 17, 256, 256);
        if (menu.getFuel() > 0 && menu.getProgress() > 0) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 37, 176, 0, 14, 14, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
