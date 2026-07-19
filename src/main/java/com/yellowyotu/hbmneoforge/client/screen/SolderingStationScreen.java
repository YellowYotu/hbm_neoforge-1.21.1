package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
import com.yellowyotu.hbmneoforge.menu.SolderingStationMenu;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class SolderingStationScreen extends AbstractContainerScreen<SolderingStationMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_soldering_station.png");
    private static final int TEXTURE_SIZE = 256;

    public SolderingStationScreen(@Nonnull SolderingStationMenu menu, @Nonnull Inventory playerInventory, @Nonnull Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelY = -1000;
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
        drawCollisionIndicator(graphics);
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
        graphics.blit(TEXTURE, leftPos + 72, topPos + 28, 192, 0, width, 14, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawPowerIndicator(GuiGraphics graphics) {
        if (menu.getEnergy() >= menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 156, topPos + 4, 176, 52, 9, 12, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private void drawFluid(GuiGraphics graphics) {
        if (menu.getFluidAmount() <= 0 || menu.getFluidTypeId() < 0) {
            return;
        }
        int width = Mth.clamp(menu.getFluidAmount() * 34 / SolderingStationBlockEntity.FLUID_CAPACITY, 1, 34);
        int color = menu.getFluidColor();
        graphics.fill(leftPos + 35, topPos + 79, leftPos + 35 + width, topPos + 95, color);
        graphics.fill(leftPos + 35, topPos + 79, leftPos + 35 + width, topPos + 81, 0x55FFFFFF);
    }

    private void drawCollisionIndicator(GuiGraphics graphics) {
        if (menu.isCollisionPreventionEnabled()) {
            graphics.fill(leftPos + 7, topPos + 68, leftPos + 13, topPos + 74, 0xFF4BB543);
        } else {
            graphics.fill(leftPos + 7, topPos + 68, leftPos + 13, topPos + 74, 0xFF9F2525);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderMachineTooltips(graphics, mouseX, mouseY);
    }

    private void renderMachineTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(152, 18, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " HE"), mouseX, mouseY);
            return;
        }
        if (isHovering(35, 63, 34, 32, mouseX, mouseY)) {
            Component fluid = menu.getFluidTypeId() < 0 ? Component.translatable("gui.hbm_neoforge.soldering_station.fluid_empty") : Component.translatable(fluidTranslationKey(menu.getFluidTypeId()));
            graphics.renderComponentTooltip(font, List.of(fluid, Component.literal(menu.getFluidAmount() + " / " + SolderingStationBlockEntity.FLUID_CAPACITY + " mB").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        if (isHovering(5, 66, 10, 10, mouseX, mouseY)) {
            Component state = Component.translatable(menu.isCollisionPreventionEnabled() ? "gui.hbm_neoforge.soldering_station.collision_on" : "gui.hbm_neoforge.soldering_station.collision_off").withStyle(menu.isCollisionPreventionEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED);
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.hbm_neoforge.soldering_station.collision"), state, Component.translatable("gui.hbm_neoforge.soldering_station.collision_hint").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        if (isHovering(78, 67, 8, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.hbm_neoforge.soldering_station.upgrades"), Component.translatable("gui.hbm_neoforge.soldering_station.upgrades_hint").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }

    private static String fluidTranslationKey(int id) {
        ItemSolderingFluidCell.FluidType[] values = ItemSolderingFluidCell.FluidType.values();
        if (id < 0 || id >= values.length) {
            return "gui.hbm_neoforge.soldering_station.fluid_empty";
        }
        return "fluid.hbm_neoforge." + values[id].name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(5, 66, 10, 10, mouseX, mouseY)) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
