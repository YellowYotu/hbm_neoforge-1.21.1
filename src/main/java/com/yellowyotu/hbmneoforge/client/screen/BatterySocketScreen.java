package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.menu.BatterySocketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class BatterySocketScreen extends AbstractContainerScreen<BatterySocketMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/storage/gui_battery_socket.png");

    public BatterySocketScreen(BatterySocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 181;
        inventoryLabelY = 87;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int capacity = Math.max(1, menu.getCapacity());
        int fill = Math.clamp(menu.getEnergy() * 52 / capacity, 0, 52);
        if (fill > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 69 - fill, 176, 52 - fill, 34, fill, 256, 256);
        }
        drawMode(graphics, leftPos + 106, topPos + 16, menu.getModeWithoutSignal());
        drawMode(graphics, leftPos + 106, topPos + 52, menu.getModeWithSignal());
    }

    private void drawMode(GuiGraphics graphics, int x, int y, int mode) {
        graphics.blit(TEXTURE, x, y, 176, 52 + mode * 18, 18, 18, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (mouseX >= leftPos + 62 && mouseX < leftPos + 96 && mouseY >= topPos + 17 && mouseY < topPos + 69) {
            int energy = menu.getEnergy();
            int capacity = Math.max(1, menu.getCapacity());
            int percent = energy * 100 / capacity;
            graphics.renderComponentTooltip(font, java.util.List.of(Component.literal(energy + "/" + capacity + "HE (" + percent + "%)"), Component.literal((menu.getDelta() >= 0 ? "+" : "") + menu.getDelta() + "HE/t")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, 106, 16, 18, 18)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            return true;
        }
        if (inside(mouseX, mouseY, 106, 52, 18, 18)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width && mouseY >= topPos + y && mouseY < topPos + y + height;
    }
}
