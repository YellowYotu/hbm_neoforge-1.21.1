package com.yellowyotu.hbmneoforge.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.CrucibleBlockEntity;
import com.yellowyotu.hbmneoforge.foundry.CrucibleRecipeRegistry;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import com.yellowyotu.hbmneoforge.menu.CrucibleMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class CrucibleScreen extends AbstractContainerScreen<CrucibleMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_crucible.png");

    public CrucibleScreen(CrucibleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 214;
        inventoryLabelX = 8;
        inventoryLabelY = 120;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress = Math.min(33, menu.getProgress() * 33 / CrucibleBlockEntity.PROCESS_HEAT);
        int heat = Math.min(33, menu.getHeat() * 33 / CrucibleBlockEntity.MAX_HEAT);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 126, topPos + 82, 176, 0, progress, 5, 256, 256);
        }
        if (heat > 0) {
            graphics.blit(TEXTURE, leftPos + 126, topPos + 91, 176, 5, heat, 5, 256, 256);
        }

        drawMaterialStack(graphics, menu.getWasteStack(), CrucibleBlockEntity.WASTE_CAPACITY, 17, 97);
        drawMaterialStack(graphics, menu.getRecipeStack(), CrucibleBlockEntity.RECIPE_CAPACITY, 62, 97);

        CrucibleRecipeRegistry.Recipe recipe = CrucibleRecipeRegistry.get(menu.getRecipeName());
        ItemStack icon = recipe == null ? new ItemStack(ModItems.TEMPLATE_FOLDER.get()) : recipe.icon();
        graphics.renderItem(icon, leftPos + 107, topPos + 81);
    }

    private void drawMaterialStack(GuiGraphics graphics, Map<String, Integer> stack, int capacity, int x, int bottomY) {
        if (stack.isEmpty()) {
            return;
        }
        int lastHeight = 0;
        int lastQuant = 0;
        for (Map.Entry<String, Integer> entry : stack.entrySet()) {
            int targetHeight = Math.min(79, (lastQuant + entry.getValue()) * 79 / capacity);
            int segmentHeight = targetHeight - lastHeight;
            if (segmentHeight <= 0) {
                lastQuant += entry.getValue();
                continue;
            }
            int color = FoundryMaterialRegistry.color(entry.getKey());
            float r = ((color >> 16) & 255) / 255.0F;
            float g = ((color >> 8) & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            graphics.setColor(r, g, b, 1.0F);
            graphics.blit(TEXTURE, leftPos + x, topPos + bottomY - targetHeight,
                    176, 89 - targetHeight, 34, segmentHeight, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.30F);
            graphics.blit(TEXTURE, leftPos + x, topPos + bottomY - targetHeight,
                    176, 89 - targetHeight, 34, segmentHeight, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            lastQuant += entry.getValue();
            lastHeight = targetHeight;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (inside(mouseX, mouseY, 16, 17, 36, 81)) {
            graphics.renderComponentTooltip(font, stackTooltip(menu.getWasteStack()), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, 61, 17, 36, 81)) {
            graphics.renderComponentTooltip(font, stackTooltip(menu.getRecipeStack()), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, 106, 80, 18, 18)) {
            CrucibleRecipeRegistry.Recipe recipe = CrucibleRecipeRegistry.get(menu.getRecipeName());
            if (recipe == null) {
                graphics.renderTooltip(font, Component.literal("Select recipe"), mouseX, mouseY);
            } else {
                graphics.renderComponentTooltip(font, recipe.tooltip(), mouseX, mouseY);
            }
        } else if (inside(mouseX, mouseY, 125, 90, 34, 7)) {
            graphics.renderTooltip(font, Component.literal(menu.getHeat() + " / " + CrucibleBlockEntity.MAX_HEAT + " TU"), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, 125, 81, 34, 7)) {
            graphics.renderTooltip(font, Component.literal(menu.getProgress() + " / " + CrucibleBlockEntity.PROCESS_HEAT + " TU"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside((int) mouseX, (int) mouseY, 106, 80, 18, 18)) {
            minecraft.setScreen(new CrucibleRecipeSelectionScreen(this, menu));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    private static List<Component> stackTooltip(Map<String, Integer> stack) {
        List<Component> lines = new ArrayList<>();
        if (stack.isEmpty()) {
            lines.add(Component.literal("Empty"));
            return lines;
        }
        for (Map.Entry<String, Integer> entry : stack.entrySet()) {
            lines.add(Component.literal(CrucibleRecipeRegistry.displayMaterial(entry.getKey()) + ": " + entry.getValue() + " q"));
        }
        return lines;
    }
}
