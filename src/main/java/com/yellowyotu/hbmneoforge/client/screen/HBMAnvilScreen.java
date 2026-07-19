package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilRecipes;
import com.yellowyotu.hbmneoforge.menu.HBMAnvilMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class HBMAnvilScreen extends AbstractContainerScreen<HBMAnvilMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/anvil.png");
    private static final int RECIPES_PER_PAGE = 10;
    private final List<Integer> filteredRecipes = new ArrayList<>();
    private EditBox search;
    private int page;
    private int selected = -1;

    public HBMAnvilScreen(HBMAnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 222;
        inventoryLabelX = 8;
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 10, topPos + 109, 84, 14, Component.translatable("gui.hbm_neoforge.anvil.search"));
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setMaxLength(25);
        search.setResponder(this::filterRecipes);
        addRenderableWidget(search);
        filterRecipes("");
    }

    private void filterRecipes(String value) {
        String query = value.toLowerCase(Locale.ROOT).trim();
        filteredRecipes.clear();
        for (int i = 0; i < HBMAnvilRecipes.size(); i++) {
            HBMAnvilRecipes.Recipe recipe = HBMAnvilRecipes.get(i);
            if (query.isEmpty() || matchesSearch(recipe, query)) {
                filteredRecipes.add(i);
            }
        }
        page = 0;
        if (!filteredRecipes.contains(selected)) {
            selected = -1;
        }
    }

    private boolean matchesSearch(HBMAnvilRecipes.Recipe recipe, String query) {
        if (recipe.name().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (recipe.primaryDisplay().get().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (recipe.secondaryCount() > 0 && recipe.secondaryDisplay().get().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        return recipe.displayResult().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 176, imageHeight, 256, 256);
        graphics.blit(TEXTURE, leftPos + 125, topPos + 17, 125, 17, 54, 108, 256, 256);
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 108, 168, 222, 88, 16, 256, 256);
        }
        renderHoverOverlays(graphics, mouseX, mouseY);
        renderRecipeGrid(graphics);
        renderRecipeDetails(graphics);
    }

    private void renderHoverOverlays(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isInside(mouseX, mouseY, 7, 71, 9, 36)) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 71, 176, 186, 9, 36, 256, 256);
        }
        if (isInside(mouseX, mouseY, 106, 71, 9, 36)) {
            graphics.blit(TEXTURE, leftPos + 106, topPos + 71, 185, 186, 9, 36, 256, 256);
        }
        if (isInside(mouseX, mouseY, 52, 53, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 52, topPos + 53, 176, 150, 18, 18, 256, 256);
        }
    }

    private void renderRecipeGrid(GuiGraphics graphics) {
        int firstVisible = page * 2;
        int lastVisible = Math.min(firstVisible + RECIPES_PER_PAGE, filteredRecipes.size());
        for (int visibleListIndex = firstVisible; visibleListIndex < lastVisible; visibleListIndex++) {
            int recipeIndex = filteredRecipes.get(visibleListIndex);
            int visibleIndex = visibleListIndex - firstVisible;
            int x = leftPos + 16 + 18 * (visibleIndex / 2);
            int y = topPos + 71 + 18 * (visibleIndex % 2);
            HBMAnvilRecipes.Recipe recipe = HBMAnvilRecipes.get(recipeIndex);
            graphics.renderItem(recipe.displayResult(), x + 1, y + 1);
            int overlayU = 18 + 18 * recipe.overlayType().ordinal();
            graphics.blit(TEXTURE, x, y, overlayU, 222, 18, 18, 256, 256);
            if (selected == recipeIndex) {
                graphics.blit(TEXTURE, x, y, 0, 222, 18, 18, 256, 256);
            }
        }
    }

    private void renderRecipeDetails(GuiGraphics graphics) {
        if (selected < 0 || selected >= HBMAnvilRecipes.size()) {
            return;
        }
        HBMAnvilRecipes.Recipe recipe = HBMAnvilRecipes.get(selected);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Inputs:").withStyle(ChatFormatting.YELLOW));
        lines.add(inputLine(recipe.primaryDisplay().get(), recipe.primaryCount()));
        if (recipe.secondaryCount() > 0) {
            lines.add(inputLine(recipe.secondaryDisplay().get(), recipe.secondaryCount()));
        }
        lines.add(Component.empty());
        lines.add(Component.literal("Outputs:").withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal(">" + recipe.displayResult().getCount() + "x ").append(recipe.displayResult().getHoverName()));
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 130, topPos + 25, 300.0F);
        graphics.pose().scale(0.72F, 0.72F, 1.0F);
        int y = 0;
        for (Component line : lines) {
            graphics.drawString(font, line, 0, y, 0xFFFFFF, false);
            y += 10;
        }
        graphics.pose().popPose();
    }

    private Component inputLine(ItemStack stack, int count) {
        return Component.literal(">" + count + "x ").append(stack.getHoverName());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component anvilTitle = Component.literal("Tier 1 Anvil");
        graphics.drawString(font, anvilTitle, 61 - font.width(anvilTitle) / 2, 8, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isInside(mouseX, mouseY, 7, 71, 9, 36)) {
                page = Math.max(0, page - 1);
                return true;
            }
            if (isInside(mouseX, mouseY, 106, 71, 9, 36)) {
                page = Math.min(maxPage(), page + 1);
                return true;
            }
            if (isInside(mouseX, mouseY, 52, 53, 18, 18)) {
                if (selected >= 0) {
                    craftSelectedRecipe();
                }
                return true;
            }
            int clickedRecipe = findRecipeAt(mouseX, mouseY);
            if (clickedRecipe >= 0) {
                selected = selected == clickedRecipe ? -1 : clickedRecipe;
                if (selected >= 0) {
                    selectRecipe(selected);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (scrollY < 0.0D) {
            page = Math.min(maxPage(), page + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private int findRecipeAt(double mouseX, double mouseY) {
        int firstVisible = page * 2;
        int lastVisible = Math.min(firstVisible + RECIPES_PER_PAGE, filteredRecipes.size());
        for (int visibleListIndex = firstVisible; visibleListIndex < lastVisible; visibleListIndex++) {
            int visibleIndex = visibleListIndex - firstVisible;
            int x = 16 + 18 * (visibleIndex / 2);
            int y = 71 + 18 * (visibleIndex % 2);
            if (isInside(mouseX, mouseY, x, y, 18, 18)) {
                return filteredRecipes.get(visibleListIndex);
            }
        }
        return -1;
    }

    private void craftSelectedRecipe() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, HBMAnvilRecipes.size());
        }
    }

    private void selectRecipe(int recipeIndex) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, recipeIndex);
        }
    }

    private int maxPage() {
        return Math.max(0, Mth.ceil((filteredRecipes.size() - RECIPES_PER_PAGE) / 2.0D));
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        double relativeX = mouseX - leftPos;
        double relativeY = mouseY - topPos;
        return relativeX >= x && relativeX < x + width && relativeY >= y && relativeY < y + height;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search != null && search.isFocused() && keyCode != 256) {
            search.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (search != null && search.isFocused()) {
            return search.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
