package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.FluidPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FluidPipeScreen extends AbstractContainerScreen<FluidPipeMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/machine/gui_fluid.png");
    private int page;

    public FluidPipeScreen(FluidPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 54;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        NTMFluidType selected = NTMFluidType.byOrdinalSafe(menu.filterOrdinal());
        NTMFluidType[] values = NTMFluidType.values();
        int start = page * 9;
        for (int i = 0; i < 9; i++) {
            int ordinal = start + i;
            if (ordinal >= values.length) { break; }
            NTMFluidType type = values[ordinal];
            int x = leftPos + 7 + i * 18;
            int y = topPos + 29;
            g.blit(type.iconTexture(), x + 1, y + 1, 0, 0, 16, 16, 16, 16);
            if (type == selected) {
                g.blit(TEXTURE, x, y, 176, 0, 18, 18, 256, 256);
            }
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        NTMFluidType filter = NTMFluidType.byOrdinalSafe(menu.filterOrdinal());
        Component name = filter == null ? Component.translatable("gui.hbm_neoforge.fluid.any") : filter.displayName();
        g.drawCenteredString(font, name, imageWidth / 2, 10, 0xFFFFFF);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= topPos + 29 && mouseY < topPos + 47 && mouseX >= leftPos + 7 && mouseX < leftPos + 169) {
            int slot = (int)((mouseX - (leftPos + 7)) / 18);
            int ordinal = page * 9 + slot;
            if (ordinal >= 0 && ordinal < NTMFluidType.values().length) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100 + ordinal);
                return true;
            }
        }
        if (button == 1) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 3);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int pages = Math.max(1, (NTMFluidType.values().length + 8) / 9);
        if (scrollY < 0) { page = Math.min(pages - 1, page + 1); }
        if (scrollY > 0) { page = Math.max(0, page - 1); }
        return true;
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
