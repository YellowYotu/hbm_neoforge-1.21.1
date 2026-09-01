package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.FluidStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FluidStorageScreen extends AbstractContainerScreen<FluidStorageMenu> {
    private static final ResourceLocation TANK = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/storage/gui_tank.png");
    private static final ResourceLocation BARREL = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/storage/gui_barrel.png");

    public FluidStorageScreen(FluidStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 74;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        boolean tank = menu.kindOrdinal() == 3;
        ResourceLocation texture = tank ? TANK : BARREL;
        g.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int mode = Math.floorMod(menu.modeOrdinal(), 4);
        g.blit(texture, leftPos + 151, topPos + 34, 176, mode * 18, 18, 18, 256, 256);

        int capacity = Math.max(1, menu.capacity());
        int fluidHeight = Math.min(52, (int) (52L * menu.amount() / capacity));
        NTMFluidType type = NTMFluidType.byOrdinalSafe(menu.fluidOrdinal());
        if (fluidHeight > 0 && type != null) {
            g.fill(leftPos + 71, topPos + 69 - fluidHeight, leftPos + 105, topPos + 69, 0xD0000000 | type.color());
        }

        // Small, unobtrusive direct filter controls. Original identifier slots continue to work too.
        g.drawString(font, "<", leftPos + 58, topPos + 39, 0x404040, false);
        g.drawString(font, ">", leftPos + 110, topPos + 39, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawCenteredString(font, title, imageWidth / 2, 6, 0x404040);
        g.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= leftPos + 151 && mouseX < leftPos + 169 && mouseY > topPos + 35 && mouseY <= topPos + 53) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            return true;
        }
        if (mouseX >= leftPos + 54 && mouseX < leftPos + 68 && mouseY >= topPos + 34 && mouseY < topPos + 52) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            return true;
        }
        if (mouseX >= leftPos + 108 && mouseX < leftPos + 122 && mouseY >= topPos + 34 && mouseY < topPos + 52) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 71 && mouseX < leftPos + 105 && mouseY >= topPos + 17 && mouseY < topPos + 69) {
            NTMFluidType type = NTMFluidType.byOrdinalSafe(menu.fluidOrdinal());
            Component name = type == null ? Component.translatable("fluid.hbm_neoforge.none") : type.displayName();
            g.renderTooltip(font, Component.literal(menu.amount() + " / " + menu.capacity() + " mB").append(" ").append(name), mouseX, mouseY);
        }
        renderTooltip(g, mouseX, mouseY);
    }
}
