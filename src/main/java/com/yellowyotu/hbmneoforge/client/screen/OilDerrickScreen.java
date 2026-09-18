package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.OilDerrickBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.OilDerrickMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class OilDerrickScreen extends AbstractContainerScreen<OilDerrickMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/machine/gui_well.png");

    public OilDerrickScreen(OilDerrickMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 184;
        imageHeight = 190;
        inventoryLabelX = 12;
        inventoryLabelY = 96;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int energyHeight = menu.getMaxEnergy() <= 0 ? 0 : menu.getEnergy() * 34 / menu.getMaxEnergy();
        if (energyHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 56 - energyHeight, 184, 34 - energyHeight, 16, energyHeight, 256, 256);
        }
        int indicator = menu.getIndicator();
        if (indicator != 0) {
            graphics.blit(TEXTURE, leftPos + 50, topPos + 19, 184 + (indicator - 1) * 14, 34, 14, 14, 256, 256);
        }
        graphics.blit(TEXTURE, leftPos + 48, topPos + 44, 200, 0, 18, 34, 256, 256);
        drawFluidTank(graphics, NTMFluidType.OIL, menu.getOilAmount(), leftPos + 76, topPos + 22, 16, 52);
        drawFluidTank(graphics, NTMFluidType.GAS, menu.getGasAmount(), leftPos + 112, topPos + 22, 16, 52);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 126 - font.width(title) / 2, 10, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 12, imageHeight - 94, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (inside(mouseX, mouseY, leftPos + 8, topPos + 22, 16, 34)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
        }
        if (inside(mouseX, mouseY, leftPos + 76, topPos + 22, 16, 52)) {
            graphics.renderTooltip(font, List.of(NTMFluidType.OIL.displayName(), Component.literal(menu.getOilAmount() + " / " + OilDerrickBlockEntity.TANK_CAPACITY + " mB")), java.util.Optional.empty(), mouseX, mouseY);
        }
        if (inside(mouseX, mouseY, leftPos + 112, topPos + 22, 16, 52)) {
            graphics.renderTooltip(font, List.of(NTMFluidType.GAS.displayName(), Component.literal(menu.getGasAmount() + " / " + OilDerrickBlockEntity.TANK_CAPACITY + " mB")), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private static void drawFluidTank(GuiGraphics graphics, NTMFluidType type, int amount, int x, int y, int width, int height) {
        if (amount <= 0) {
            return;
        }
        int fillHeight = Math.min(height, Math.max(1, amount * height / OilDerrickBlockEntity.TANK_CAPACITY));
        int top = y + height - fillHeight;
        for (int yy = top; yy < y + height; yy += 16) {
            int drawHeight = Math.min(16, y + height - yy);
            for (int xx = x; xx < x + width; xx += 16) {
                int drawWidth = Math.min(16, x + width - xx);
                graphics.blit(type.iconTexture(), xx, yy, 0, 0, drawWidth, drawHeight, 16, 16);
            }
        }
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
