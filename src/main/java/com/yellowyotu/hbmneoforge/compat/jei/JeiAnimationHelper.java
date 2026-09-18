package com.yellowyotu.hbmneoforge.compat.jei;

import com.yellowyotu.hbmneoforge.ModItems;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

final class JeiAnimationHelper {
    private static final List<ItemStack> BATTERIES = List.of(
            new ItemStack(ModItems.BATTERY_PACK.get()),
            new ItemStack(ModItems.BATTERY_LEAD.get()),
            new ItemStack(ModItems.BATTERY_LITHIUM.get()),
            new ItemStack(ModItems.BATTERY_SC_WASTE.get()),
            new ItemStack(ModItems.BATTERY_SC_PU238.get()),
            new ItemStack(ModItems.BATTERY_SC_AM241.get()),
            new ItemStack(ModItems.BATTERY_INFINITE.get())
    );

    private JeiAnimationHelper() {
    }

    static List<ItemStack> batteryStacks() {
        return BATTERIES;
    }

    static void drawHorizontalProgress(GuiGraphics graphics, int x, int y, int width, int height, int periodTicks) {
        int filled = Math.max(1, Math.round(width * phase(periodTicks)));
        graphics.fill(x, y + height - 2, x + filled, y + height, 0xB0FFFFFF);
    }

    static void drawVerticalPower(GuiGraphics graphics, int x, int y, int width, int height, int periodTicks) {
        int filled = Math.max(1, Math.round(height * phase(periodTicks)));
        graphics.fill(x, y + height - filled, x + width, y + height, 0x60FFFFFF);
    }

    static void drawFire(GuiGraphics graphics, int x, int y, int width, int height) {
        long frame = (System.currentTimeMillis() / 100L) & 3L;
        int inset = frame == 1L || frame == 3L ? 2 : 1;
        graphics.fill(x + inset, y + 2, x + width - inset, y + height, 0xA0FF8A00);
        graphics.fill(x + inset + 2, y + 5, x + width - inset - 2, y + height, 0xA0FFE56A);
    }

    static void drawProgressArrow(GuiGraphics graphics, int x, int y, int periodTicks) {
        int filled = filledWidth(32, periodTicks);
        int shaftWidth = Math.min(filled, 28);
        if (shaftWidth > 0) {
            graphics.fill(x, y + 3, x + shaftWidth, y + 5, 0xFFFFFFFF);
        }
        int headWidth = Math.max(0, filled - 28);
        for (int i = 0; i < headWidth; i++) {
            int halfHeight = 4 - i;
            graphics.fill(x + 28 + i, y + 4 - halfHeight, x + 29 + i, y + 4 + halfHeight, 0xFFFFFFFF);
        }
    }

    static int filledWidth(int width, int periodTicks) {
        return net.minecraft.util.Mth.clamp(Math.round(width * phase(periodTicks)), 0, width);
    }

    static int filledHeight(int height, int periodTicks) {
        return net.minecraft.util.Mth.clamp(Math.round(height * phase(periodTicks)), 0, height);
    }

    private static float phase(int periodTicks) {
        long periodMs = Math.max(1, periodTicks) * 50L;
        return (System.currentTimeMillis() % periodMs) / (float) periodMs;
    }
}
