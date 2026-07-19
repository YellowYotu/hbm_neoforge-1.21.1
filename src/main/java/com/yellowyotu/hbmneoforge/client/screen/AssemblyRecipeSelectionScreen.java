package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes;
import com.yellowyotu.hbmneoforge.menu.AssemblyMachineMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;

public final class AssemblyRecipeSelectionScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_recipe_selector.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 132;
    private static final int COLUMNS = 8;
    private static final int ROWS = 5;
    private static final int RECIPES_PER_PAGE = COLUMNS * ROWS;

    private final Screen parent;
    private final AssemblyMachineMenu menu;
    private final List<Integer> filteredRecipes = new ArrayList<>();
    private EditBox search;
    private int left;
    private int top;
    private int page;

    public AssemblyRecipeSelectionScreen(Screen parent, AssemblyMachineMenu menu) {
        super(Component.translatable("gui.hbm_neoforge.assembler.recipe_catalog"));
        this.parent = parent;
        this.menu = menu;
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        search = new EditBox(font, left + 28, top + 111, 102, 12, Component.translatable("gui.hbm_neoforge.search"));
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setMaxLength(64);
        search.setResponder(this::filterRecipes);
        search.setFocused(false);
        addRenderableWidget(search);
        filterRecipes("");
    }

    private void filterRecipes(String value) {
        String query = value.trim().toLowerCase(Locale.ROOT);
        filteredRecipes.clear();
        for (int index = 0; index < AssemblyMachineRecipes.RECIPES.size(); index++) {
            AssemblyMachineRecipes.Recipe recipe = AssemblyMachineRecipes.RECIPES.get(index);
            String name = recipe.resultStack().getHoverName().getString().toLowerCase(Locale.ROOT);
            String registryId = BuiltInRegistries.ITEM.getKey(recipe.resultStack().getItem()).toString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query) || recipe.id().toLowerCase(Locale.ROOT).contains(query) || registryId.contains(query)) {
                filteredRecipes.add(index);
            }
        }
        page = 0;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen's default renderBackground() blurs the world behind it when a level is present.
        // AbstractContainerScreen (used by AssemblyMachineScreen) overrides this with a plain
        // transparent overlay instead, which is why the main assembler GUI never blurs. This
        // screen extends Screen directly, so without this override it fell back to the blurred
        // variant, making the world visibly blur every time the recipe catalog was opened.
        renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT, 256, 256);
        if (search.isFocused()) {
            graphics.blit(TEXTURE, left + 26, top + 108, 0, 132, 106, 16, 256, 256);
        }
        renderButtonHighlights(graphics, mouseX, mouseY);
        int first = page * 8;
        int last = Math.min(first + RECIPES_PER_PAGE, filteredRecipes.size());
        for (int visibleIndex = first; visibleIndex < last; visibleIndex++) {
            int slot = visibleIndex - first;
            int recipeIndex = filteredRecipes.get(visibleIndex);
            int x = left + 8 + slot % COLUMNS * 18;
            int y = top + 18 + slot / COLUMNS * 18;
            graphics.renderItem(AssemblyMachineRecipes.RECIPES.get(recipeIndex).resultStack(), x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderTooltip(font, AssemblyMachineRecipes.RECIPES.get(recipeIndex).resultStack(), mouseX, mouseY);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderButtonHighlights(GuiGraphics graphics, int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, left + 152, top + 18, 16, 16)) {
            graphics.blit(TEXTURE, left + 152, top + 18, 176, 0, 16, 16, 256, 256);
        }
        if (inside(mouseX, mouseY, left + 152, top + 36, 16, 16)) {
            graphics.blit(TEXTURE, left + 152, top + 36, 176, 16, 16, 16, 256, 256);
        }
        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            graphics.blit(TEXTURE, left + 152, top + 90, 176, 32, 16, 16, 256, 256);
        }
        if (inside(mouseX, mouseY, left + 134, top + 108, 16, 16)) {
            graphics.blit(TEXTURE, left + 134, top + 108, 176, 48, 16, 16, 256, 256);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, left + 152, top + 18, 16, 16)) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (inside(mouseX, mouseY, left + 152, top + 36, 16, 16)) {
            page = Math.min(maxPage(), page + 1);
            return true;
        }
        if (inside(mouseX, mouseY, left + 134, top + 108, 16, 16)) {
            search.setValue("");
            search.setFocused(true);
            return true;
        }
        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            minecraft.setScreen(parent);
            return true;
        }
        int first = page * 8;
        int last = Math.min(first + RECIPES_PER_PAGE, filteredRecipes.size());
        for (int visibleIndex = first; visibleIndex < last; visibleIndex++) {
            int slot = visibleIndex - first;
            int x = left + 8 + slot % COLUMNS * 18;
            int y = top + 18 + slot / COLUMNS * 18;
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100 + filteredRecipes.get(visibleIndex));
                minecraft.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        page = Mth.clamp(page - (int) Math.signum(scrollY), 0, maxPage());
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search != null && search.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                search.setFocused(false);
                return true;
            }
            if (keyCode == minecraft.options.keyInventory.getKey().getValue()) {
                return true;
            }
            if (search.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
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

    private int maxPage() {
        return Math.max(0, (filteredRecipes.size() - 1) / 8);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}