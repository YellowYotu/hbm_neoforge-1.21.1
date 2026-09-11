package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerBlockEntity;
import com.yellowyotu.hbmneoforge.menu.WoodBurnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class WoodBurnerScreen extends AbstractContainerScreen<WoodBurnerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_wood_burner.png");
    public WoodBurnerScreen(WoodBurnerMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageWidth = 176; imageHeight = 186; inventoryLabelX = 8; inventoryLabelY = 92; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) { graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256); if (menu.isEnabled()) { graphics.blit(TEXTURE, leftPos + 53, topPos + 17, 196, 0, 16, 15, 256, 256); } if (menu.getMaxBurnTime() > 0 && menu.getBurnTime() > 0) { int h = Mth.clamp(menu.getBurnTime() * 52 / menu.getMaxBurnTime(), 0, 52); graphics.blit(TEXTURE, leftPos + 17, topPos + 70 - h, 192, 52 - h, 4, h, 256, 256); } int p = Mth.clamp(menu.getPower() * 34 / WoodBurnerBlockEntity.MAX_POWER, 0, 34); if (p > 0) { graphics.blit(TEXTURE, leftPos + 143, topPos + 52 - p, 176, 52 - p, 16, p, 256, 256); } }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { graphics.drawString(font, title, 70 - font.width(title) / 2, 6, 0xFFFFFF, false); graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderBackground(graphics, mouseX, mouseY, partialTick); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY); if (mouseX >= leftPos + 143 && mouseX < leftPos + 159 && mouseY >= topPos + 18 && mouseY < topPos + 52) { graphics.renderTooltip(font, Component.literal(menu.getPower() + " / " + WoodBurnerBlockEntity.MAX_POWER + " HE"), mouseX, mouseY); } if (mouseX >= leftPos + 53 && mouseX < leftPos + 69 && mouseY >= topPos + 17 && mouseY < topPos + 32) { graphics.renderTooltip(font, Component.translatable(menu.isEnabled() ? "gui.hbm_neoforge.generator.on" : "gui.hbm_neoforge.generator.off"), mouseX, mouseY); } }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { if (button == 0 && mouseX >= leftPos + 53 && mouseX < leftPos + 69 && mouseY >= topPos + 17 && mouseY < topPos + 32) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0); return true; } return super.mouseClicked(mouseX, mouseY, button); }
}
