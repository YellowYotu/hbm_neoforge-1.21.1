package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.menu.FatManMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FatManScreen extends AbstractContainerScreen<FatManMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
            "textures/gui/weapon/fatmanschematic.png");

    public FatManScreen(FatManMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        if (hasLens(1)) {
            graphics.blit(TEXTURE, leftPos + 82, topPos + 19, 176, 0, 24, 24, 256, 256);
        }
        if (hasLens(2)) {
            graphics.blit(TEXTURE, leftPos + 106, topPos + 19, 200, 0, 24, 24, 256, 256);
        }
        if (hasLens(3)) {
            graphics.blit(TEXTURE, leftPos + 82, topPos + 43, 176, 24, 24, 24, 256, 256);
        }
        if (hasLens(4)) {
            graphics.blit(TEXTURE, leftPos + 106, topPos + 43, 200, 24, 24, 24, 256, 256);
        }
        if (isReady()) {
            graphics.blit(TEXTURE, leftPos + 134, topPos + 35, 176, 48, 16, 16, 256, 256);
        }
    }

    private boolean hasLens(int slot) {
        return menu.getSlot(slot).getItem().is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
    }

    private boolean isReady() {
        return menu.getSlot(0).getItem().is(ModItems.MAN_IGNITER.get())
                && hasLens(1)
                && hasLens(2)
                && hasLens(3)
                && hasLens(4)
                && menu.getSlot(5).getItem().is(ModItems.MAN_CORE.get());
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
    }
}
