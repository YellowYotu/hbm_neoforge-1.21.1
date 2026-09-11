package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes;
import com.yellowyotu.hbmneoforge.menu.ChemicalPlantMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class ChemicalRecipeSelectionScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/processing/gui_recipe_selector.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 132;
    private static final int COLUMNS = 8;
    private static final int VISIBLE_RECIPES = 40;
    private static final int PAGE_STEP = 8;

    private final Screen parent;
    private final ChemicalPlantMenu menu;
    private final List<Integer> filteredRecipes = new ArrayList<>();
    private EditBox search;
    private int left;
    private int top;
    private int page;
    private int selectedRecipe;

    public ChemicalRecipeSelectionScreen(Screen parent, ChemicalPlantMenu menu) {
        super(Component.translatable("gui.hbm_neoforge.chemical_plant.recipe_catalog"));
        this.parent = parent;
        this.menu = menu;
        selectedRecipe = menu.getActiveRecipe();
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
        addRenderableWidget(search);
        filterRecipes("");
    }

    private void filterRecipes(String value) {
        String query = value.trim().toLowerCase(Locale.ROOT);
        filteredRecipes.clear();
        for (int index = 0; index < ChemicalPlantRecipes.RECIPES.size(); index++) {
            ChemicalPlantRecipes.Recipe recipe = ChemicalPlantRecipes.RECIPES.get(index);
            String name = recipe.displayStack().getHoverName().getString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query) || recipe.id().toLowerCase(Locale.ROOT).contains(query) || recipeMatchesInputs(recipe, query)) {
                filteredRecipes.add(index);
            }
        }
        page = 0;
    }

    private static boolean recipeMatchesInputs(ChemicalPlantRecipes.Recipe recipe, String query) {
        if (query.isEmpty()) {
            return true;
        }
        for (ChemicalPlantRecipes.Ingredient ingredient : recipe.ingredients()) {
            if (ingredient.displayStack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.inputFluids()) {
            if (fluid.type().displayName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.outputFluids()) {
            if (fluid.type().displayName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderTransparentBackground(graphics); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT, 256, 256);
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, left + 26, top + 108, 0, 132, 106, 16, 256, 256);
        }
        renderHoverButton(graphics, mouseX, mouseY, 152, 18, 176, 0);
        renderHoverButton(graphics, mouseX, mouseY, 152, 36, 176, 16);
        renderHoverButton(graphics, mouseX, mouseY, 152, 90, 176, 32);
        renderHoverButton(graphics, mouseX, mouseY, 134, 108, 176, 48);
        renderHoverButton(graphics, mouseX, mouseY, 8, 108, 176, 64);

        int first = firstVisibleIndex();
        int last = Math.min(first + VISIBLE_RECIPES, filteredRecipes.size());
        for (int visibleIndex = first; visibleIndex < last; visibleIndex++) {
            int slot = visibleIndex - first;
            int recipeIndex = filteredRecipes.get(visibleIndex);
            int x = left + 8 + slot % COLUMNS * 18;
            int y = top + 18 + slot / COLUMNS * 18;
            if (recipeIndex == selectedRecipe) {
                graphics.blit(TEXTURE, left + 7 + slot % COLUMNS * 18, top + 17 + slot / COLUMNS * 18, 192, 0, 18, 18, 256, 256);
            }
            ItemStack stack = ChemicalPlantRecipes.RECIPES.get(recipeIndex).displayStack();
            graphics.renderItem(stack, x, y);
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                graphics.renderTooltip(font, ChemicalPlantScreen.recipeTooltip(ChemicalPlantRecipes.RECIPES.get(recipeIndex)), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        if (selectedRecipe >= 0 && selectedRecipe < ChemicalPlantRecipes.RECIPES.size()) {
            ChemicalPlantRecipes.Recipe recipe = ChemicalPlantRecipes.RECIPES.get(selectedRecipe);
            graphics.renderItem(recipe.displayStack(), left + 152, top + 72);
            if (inside(mouseX, mouseY, left + 151, top + 71, 18, 18)) {
                graphics.renderTooltip(font, ChemicalPlantScreen.recipeTooltip(recipe), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (inside(mouseX, mouseY, left + 152, top + 90, 16, 16)) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.close"), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, left + 134, top + 108, 16, 16)) {
            graphics.renderTooltip(font, Component.translatable("gui.hbm_neoforge.clear_search"), mouseX, mouseY);
        }
    }

    private void renderHoverButton(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int u, int v) {
        if (inside(mouseX, mouseY, left + x, top + y, 16, 16)) {
            graphics.blit(TEXTURE, left + x, top + y, u, v, 16, 16, 256, 256);
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
        if (inside(mouseX, mouseY, left + 151, top + 71, 18, 18) && selectedRecipe >= 0) {
            selectedRecipe = -1;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 99);
            return true;
        }
        int first = firstVisibleIndex();
        int last = Math.min(first + VISIBLE_RECIPES, filteredRecipes.size());
        for (int visibleIndex = first; visibleIndex < last; visibleIndex++) {
            int slot = visibleIndex - first;
            int x = left + 8 + slot % COLUMNS * 18;
            int y = top + 18 + slot / COLUMNS * 18;
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                int recipeIndex = filteredRecipes.get(visibleIndex);
                selectedRecipe = selectedRecipe == recipeIndex ? -1 : recipeIndex;
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selectedRecipe < 0 ? 99 : 100 + selectedRecipe);
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

    private int firstVisibleIndex() { return page * PAGE_STEP; }
    private int maxPage() { return Math.max(0, (filteredRecipes.size() - VISIBLE_RECIPES + PAGE_STEP - 1) / PAGE_STEP); }
    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) { return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height; }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
