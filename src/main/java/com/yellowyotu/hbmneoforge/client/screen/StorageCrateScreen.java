package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.menu.StorageCrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class StorageCrateScreen extends AbstractContainerScreen<StorageCrateMenu> {
    private static final ResourceLocation IRON = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/storage/gui_crate_iron.png");
    private static final ResourceLocation STEEL = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/storage/gui_crate_steel.png");

    public StorageCrateScreen(StorageCrateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = menu.getRows() == 6 ? 222 : 186;
        inventoryLabelY = menu.getRows() == 6 ? 140 : 104;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = menu.getRows() == 6 ? STEEL : IRON;
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int color = menu.getRows() == 6 ? 0x1C1C1C : 0x404040;
        graphics.drawString(font, title, 8, 6, color, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
