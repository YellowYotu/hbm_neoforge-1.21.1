package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ItemMaterialTooltip extends Item {

    public enum TooltipType {
        URANIUM,
        TUNGSTEN
    }

    private final TooltipType tooltipType;

    public ItemMaterialTooltip(Properties properties, TooltipType tooltipType) {
        super(properties);
        this.tooltipType = tooltipType;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        if (tooltipType == TooltipType.URANIUM) {
            tooltip.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal(" - Alpha rays: 0.35 RAD/s").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(" - Radon Gas: 0.5 RAD/s").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Adds multiplier 1.05 to the custom nuke stage: Nuclear")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("[ Press F1 for help ]").withStyle(ChatFormatting.GREEN));
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Adds 1.0 to the custom nuke stage: Salted")
                .withStyle(ChatFormatting.GOLD));
    }
}
