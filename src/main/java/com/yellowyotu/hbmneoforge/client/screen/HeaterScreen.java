package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.menu.HeaterMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class HeaterScreen extends AbstractContainerScreen<HeaterMenu> {
    private static final ResourceLocation FIREBOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/machine/gui_firebox.png");
    private static final ResourceLocation OVEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/machine/gui_heating_oven.png");
    public HeaterScreen(HeaterMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 168; inventoryLabelX = 8; inventoryLabelY = 76; }
    private ResourceLocation texture() { return menu.isHeatingOven() ? OVEN_TEXTURE : FIREBOX_TEXTURE; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = texture();
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int heatWidth = menu.getMaxHeat() <= 0 ? 0 : Mth.clamp(menu.getHeatStored() * 69 / menu.getMaxHeat(), 0, 69);
        if (heatWidth > 0) { graphics.blit(texture, leftPos + 81, topPos + 28, 176, 0, heatWidth, 5, 256, 256); }
        int burnWidth = menu.getMaxBurnTime() <= 0 ? 0 : Mth.clamp(menu.getBurnTime() * 70 / menu.getMaxBurnTime(), 0, 70);
        if (burnWidth > 0) { graphics.blit(texture, leftPos + 81, topPos + 37, 176, 5, burnWidth, 5, 256, 256); }
        if (menu.isBurning()) { graphics.blit(texture, leftPos + 25, topPos + 26, 176, 10, 18, 18, 256, 256); }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, menu.isHeatingOven() ? 0xFFFFFF : 0x404040, false); graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderBackground(graphics, mouseX, mouseY, partialTick); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); if (mouseX >= leftPos + 80 && mouseX < leftPos + 151 && mouseY >= topPos + 27 && mouseY < topPos + 34) { graphics.renderComponentTooltip(font, List.of(Component.literal(String.format("%,d / %,d TU", menu.getHeatStored(), menu.getMaxHeat()))), mouseX, mouseY); } if (mouseX >= leftPos + 80 && mouseX < leftPos + 151 && mouseY >= topPos + 35 && mouseY < topPos + 44) { graphics.renderComponentTooltip(font, List.of(Component.literal(String.format("%,d TU/t", menu.getBurnHeat())), Component.literal((menu.getBurnTime() / 20) + " s")), mouseX, mouseY); } }
}
