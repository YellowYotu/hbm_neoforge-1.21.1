package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.ArcWelderMenu;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class ArcWelderScreen extends AbstractContainerScreen<ArcWelderMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_arc_welder.png");
    private static final int TEXTURE_SIZE = 256;

    public ArcWelderScreen(@Nonnull ArcWelderMenu menu, @Nonnull Inventory playerInventory, @Nonnull Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelX = 28;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 110;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        drawEnergy(graphics);
        drawProgress(graphics);
        drawPowerIndicator(graphics);
        drawFluid(graphics);
    }

    private void drawEnergy(GuiGraphics graphics) {
        if (menu.getEnergy() <= 0 || menu.getMaxEnergy() <= 0) {
            return;
        }
        int height = Mth.clamp(menu.getEnergy() * 52 / menu.getMaxEnergy(), 0, 52);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 70 - height, 176, 52 - height, 16, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawProgress(GuiGraphics graphics) {
        if (menu.getProcessTime() <= 0 || menu.getProgress() <= 0) {
            return;
        }
        int width = Mth.clamp(menu.getProgress() * 33 / menu.getProcessTime(), 0, 33);
        graphics.blit(TEXTURE, leftPos + 72, topPos + 37, 192, 0, width, 14, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawPowerIndicator(GuiGraphics graphics) {
        if (menu.getEnergy() >= menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 52, 9, 12, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private void drawFluid(GuiGraphics graphics) {
        NTMFluidType type = menu.getFluidType();
        if (type == null || menu.getFluidAmount() <= 0) {
            return;
        }
        int width = Mth.clamp(menu.getFluidAmount() * 34 / ArcWelderBlockEntity.FLUID_CAPACITY, 1, 34);
        int startX = leftPos + 35;
        int startY = topPos + 79;
        for (int x = 0; x < width; x += 16) {
            int drawWidth = Math.min(16, width - x);
            graphics.blit(type.iconTexture(), startX + x, startY, 0, 0, drawWidth, 16, 16, 16);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
        } else if (isHovering(35, 63, 34, 32, mouseX, mouseY)) {
            NTMFluidType type = menu.getFluidType();
            Component fluidName = type == null ? Component.translatable("fluid.hbm_neoforge.none") : type.displayName();
            graphics.renderComponentTooltip(font, List.of(fluidName, Component.literal(menu.getFluidAmount() + " / " + ArcWelderBlockEntity.FLUID_CAPACITY + " mB").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }
}
