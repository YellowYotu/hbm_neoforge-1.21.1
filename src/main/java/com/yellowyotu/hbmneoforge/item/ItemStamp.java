package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ItemStamp extends Item {
    public enum StampType { FLAT, PLATE, WIRE, CIRCUIT }
    private final StampType type;

    public ItemStamp(Properties properties, StampType type) {
        super(properties);
        this.type = type;
    }

    public StampType getStampType() { return type; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Stamp Type: " + switch (type) {
            case FLAT -> "Flat";
            case PLATE -> "Plate";
            case WIRE -> "Wire";
            case CIRCUIT -> "Circuit";
        }).withStyle(ChatFormatting.GRAY));
    }
}
