package com.yellowyotu.hbmneoforge.client.screen;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.FluidIdentifierMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FluidIdentifierScreen extends AbstractContainerScreen<FluidIdentifierMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/machine/gui_fluid.png");
    private static final int PAGE_SIZE = 9;
    private EditBox search;
    private final List<NTMFluidType> matches = new ArrayList<>();
    private final List<NTMFluidType> visible = new ArrayList<>();
    private int page;

    public FluidIdentifierScreen(FluidIdentifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 54;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 46, topPos + 9, 86, 14, Component.empty());
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setResponder(value -> {
            page = 0;
            updateSearch();
        });
        addRenderableWidget(search);
        setInitialFocus(search);
        updateSearch();
    }

    private void updateSearch() {
        matches.clear();
        visible.clear();
        String query = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        for (NTMFluidType type : NTMFluidType.values()) {
            if (type.displayName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(type);
            }
        }
        int pageCount = Math.max(1, (matches.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(page, pageCount - 1);
        int start = page * PAGE_SIZE;
        int end = Math.min(matches.size(), start + PAGE_SIZE);
        visible.addAll(matches.subList(start, end));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (search != null && search.isFocused()) {
            g.blit(TEXTURE, leftPos + 43, topPos + 7, 166, 54, 90, 18, 256, 256);
        }

        NTMFluidType primary = NTMFluidType.byOrdinalSafe(menu.primaryOrdinal());
        NTMFluidType secondary = NTMFluidType.byOrdinalSafe(menu.secondaryOrdinal());
        for (int i = 0; i < visible.size(); i++) {
            NTMFluidType type = visible.get(i);
            int x = leftPos + 7 + i * 18;
            int y = topPos + 29;
            g.blit(type.iconTexture(), x + 1, y + 1, 0, 0, 16, 16, 16, 16);
            if (type == primary && type == secondary) {
                g.blit(TEXTURE, x, y, 176, 36, 18, 18, 256, 256);
            } else if (type == primary) {
                g.blit(TEXTURE, x, y, 176, 0, 18, 18, 256, 256);
            } else if (type == secondary) {
                g.blit(TEXTURE, x, y, 176, 18, 18, 18, 256, 256);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= topPos + 29 && mouseY < topPos + 47 && mouseX >= leftPos + 7 && mouseX < leftPos + 169) {
            int slot = (int) ((mouseX - (leftPos + 7)) / 18);
            if (slot >= 0 && slot < visible.size()) {
                NTMFluidType type = visible.get(slot);
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, (button == 1 ? 2000 : 1000) + type.ordinal());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (matches.size() <= PAGE_SIZE) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int pageCount = (matches.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (scrollY < 0) {
            page = Math.min(pageCount - 1, page + 1);
        } else if (scrollY > 0) {
            page = Math.max(0, page - 1);
        }
        updateSearch();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        for (int i = 0; i < visible.size(); i++) {
            int x = leftPos + 7 + i * 18;
            int y = topPos + 29;
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                g.renderTooltip(font, visible.get(i).displayName(), mouseX, mouseY);
                break;
            }
        }
    }
}
