package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.foundry.CrucibleRecipeRegistry;
import com.yellowyotu.hbmneoforge.menu.CrucibleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CrucibleRecipeSelectionScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_recipe_selector.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 132;

    private final Screen parent;
    private final CrucibleMenu menu;
    private int selectedRecipe;
    private int left;
    private int top;

    public CrucibleRecipeSelectionScreen(Screen parent, CrucibleMenu menu) {
        super(Component.literal("Crucible Recipes"));
        this.parent = parent;
        this.menu = menu;
        this.selectedRecipe = indexOf(menu.getRecipeName());
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT, 256, 256);

        for (int i = 0; i < CrucibleRecipeRegistry.recipes().size(); i++) {
            int x = left + 8 + i * 18;
            int y = top + 18;
            CrucibleRecipeRegistry.Recipe recipe = CrucibleRecipeRegistry.recipes().get(i);
            if (i == selectedRecipe) {
                graphics.fill(x, y, x + 16, y + 16, 0x55FFFFFF);
            }
            graphics.renderItem(recipe.icon(), x, y);
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                graphics.renderComponentTooltip(font, recipe.tooltip(), mouseX, mouseY);
            }
        }

        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            graphics.blit(TEXTURE, left + 152, top + 90, 176, 32, 16, 16, 256, 256);
        }

        int noneX = left + 151;
        int noneY = top + 71;
        graphics.renderItem(new ItemStack(ModItems.TEMPLATE_FOLDER.get()), noneX + 1, noneY + 1);
        if (inside(mouseX, mouseY, noneX, noneY, 18, 18)) {
            graphics.renderTooltip(font, Component.literal("No recipe / free smelting"), mouseX, mouseY);
        }

        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Close"), mouseX, mouseY);
        } else if (selectedRecipe >= 0) {
            CrucibleRecipeRegistry.Recipe recipe = CrucibleRecipeRegistry.recipes().get(selectedRecipe);
            graphics.renderComponentTooltip(font, recipe.tooltip(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < CrucibleRecipeRegistry.recipes().size(); i++) {
            int x = left + 8 + i * 18;
            int y = top + 18;
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                selectedRecipe = selectedRecipe == i ? -1 : i;
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId, selectedRecipe < 0 ? 99 : 100 + selectedRecipe);
                return true;
            }
        }

        if (inside(mouseX, mouseY, left + 151, top + 71, 18, 18)) {
            selectedRecipe = -1;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 99);
            return true;
        }

        // Original CE close X in gui_recipe_selector.png.
        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            minecraft.setScreen(parent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int indexOf(String name) {
        for (int i = 0; i < CrucibleRecipeRegistry.recipes().size(); i++) {
            if (CrucibleRecipeRegistry.recipes().get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
