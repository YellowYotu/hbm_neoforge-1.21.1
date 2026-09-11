package com.yellowyotu.hbmneoforge.client.screen;

import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import com.yellowyotu.hbmneoforge.menu.MachinePressMenu;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.Mth;

public final class MachinePressScreen
        extends AbstractContainerScreen<MachinePressMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                    "textures/gui/gui_press.png"
            );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public MachinePressScreen(
            @Nonnull MachinePressMenu menu,
            @Nonnull Inventory playerInventory,
            @Nonnull Component title
    ) {
        super(menu, playerInventory, title);

        imageWidth = 176;
        imageHeight = 214;

        titleLabelY = 5;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(
            @Nonnull GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        drawFuelFlame(graphics);
        drawPressIndicator(graphics);
        drawSpeedNeedle(graphics);
    }

    private void drawFuelFlame(GuiGraphics graphics) {
        int burnTime = menu.getBurnTime();

        if (burnTime <= 0) {
            return;
        }

        float fuelFraction = Mth.clamp(
                burnTime / 1600.0F,
                0.0F,
                1.0F
        );

        int fullHeight = 14;
        int visibleHeight = Math.max(
                1,
                Mth.ceil(fullHeight * fuelFraction)
        );

        int x = leftPos + 28;
        int y = topPos + 37;

        graphics.enableScissor(
                x,
                y + fullHeight - visibleHeight,
                x + 14,
                y + fullHeight
        );

        graphics.blit(
                TEXTURE,
                x,
                y,
                0,
                214,
                14,
                14,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        graphics.disableScissor();
    }

    private void drawPressIndicator(GuiGraphics graphics) {
        int pressHeight =
                menu.getPressProgress() * 16
                        / MachinePressBlockEntity.MAX_PRESS;

        if (pressHeight <= 0) {
            return;
        }

        graphics.blit(
                TEXTURE,
                leftPos + 79,
                topPos + 35,
                15,
                214,
                18,
                pressHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    private void drawSpeedNeedle(GuiGraphics graphics) {
        float speedFraction =
                menu.getSpeed()
                        / (float) MachinePressBlockEntity.MAX_SPEED;

        speedFraction =
                Math.max(0.0F, Math.min(1.0F, speedFraction));

        /*
         * Стрелка двигается примерно от синей зоны
         * через жёлтую к оранжевой.
         */
        float angle =
                -135.0F + speedFraction * 270.0F;

        graphics.pose().pushPose();

        graphics.pose().translate(
                leftPos + 34.0F,
                topPos + 25.0F,
                100.0F
        );

        graphics.pose().mulPose(
                Axis.ZP.rotationDegrees(angle)
        );

        /*
         * Тёмная стрелка длиной 9 пикселей.
         */
        graphics.fill(
                -1,
                -9,
                1,
                2,
                0xFF202020
        );

        /*
         * Центральная точка стрелки.
         */
        graphics.fill(
                -2,
                -2,
                2,
                2,
                0xFF303030
        );

        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(
            @Nonnull GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawCenteredString(
                font,
                title,
                imageWidth / 2,
                titleLabelY,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                playerInventoryTitle,
                inventoryLabelX,
                inventoryLabelY,
                0x404040,
                false
        );
    }

    @Override
    public void render(
            @Nonnull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }
}