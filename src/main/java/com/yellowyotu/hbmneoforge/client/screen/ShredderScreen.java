package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.ShredderBlockEntity;
import com.yellowyotu.hbmneoforge.menu.ShredderMenu;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class ShredderScreen extends AbstractContainerScreen<ShredderMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/gui_shredder.png");
    private static final int TEXTURE_SIZE = 256;

    public ShredderScreen(@Nonnull ShredderMenu menu, @Nonnull Inventory playerInventory, @Nonnull Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 233;
        titleLabelY = 5;
        inventoryLabelX = 8;
        inventoryLabelY = 139;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        drawEnergy(graphics);
        drawProgress(graphics);
        drawBladeState(graphics, menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_LEFT), leftPos + 43, topPos + 71, 176);
        drawBladeState(graphics, menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_RIGHT), leftPos + 79, topPos + 71, 194);
        drawBladeErrorIcon(graphics);
    }


    private void drawBladeErrorIcon(GuiGraphics graphics) {
        boolean invalidBlades = menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_LEFT) == 0 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_LEFT) == 3 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_RIGHT) == 0 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_RIGHT) == 3;
        if (invalidBlades) {
            graphics.blit(TEXTURE, leftPos - 16, topPos + 36, 194, 36, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private void drawEnergy(GuiGraphics graphics) {
        if (menu.getEnergy() <= 0 || menu.getMaxEnergy() <= 0) {
            return;
        }
        int height = Mth.clamp(menu.getEnergy() * 88 / menu.getMaxEnergy(), 0, 88);
        graphics.blit(TEXTURE, leftPos + 8, topPos + 106 - height, 176, 160 - height, 16, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawProgress(GuiGraphics graphics) {
        if (menu.getMaxProgress() <= 0) {
            return;
        }
        int width = Mth.clamp(menu.getProgress() * 34 / menu.getMaxProgress(), 0, 34);
        if (width > 0) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 89, 176, 54, width + 1, 18, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private void drawBladeState(GuiGraphics graphics, int state, int x, int y, int textureX) {
        if (state <= 0) {
            return;
        }
        int textureY = switch (state) {
            case 1 -> 0;
            case 2 -> 18;
            default -> 36;
        };
        graphics.blit(TEXTURE, x, y, textureX, textureY, 18, 18, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(8, 18, 16, 88, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
        }

        boolean invalidBlades = menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_LEFT) == 0 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_LEFT) == 3 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_RIGHT) == 0 || menu.getBladeState(ShredderBlockEntity.SLOT_BLADE_RIGHT) == 3;
        if (invalidBlades && isHovering(-16, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.shredder.blades_error"), mouseX, mouseY);
        }
    }
}
