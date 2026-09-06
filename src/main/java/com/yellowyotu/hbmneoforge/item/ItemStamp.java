package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemStamp extends Item {
    public enum StampType { FLAT, PLATE, WIRE, CIRCUIT, PRINTING1, PRINTING2, PRINTING3, PRINTING4, PRINTING5, PRINTING6, PRINTING7, PRINTING8 }
    private final StampType type;

    public ItemStamp(Properties properties, StampType type) {
        super(properties);
        this.type = type;
    }

    public StampType getStampType() { return type; }
    public StampType getStampType(ItemStack stack) { return type; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Stamp Type: " + switch (type) {
            case FLAT -> "Flat";
            case PLATE -> "Plate";
            case WIRE -> "Wire";
            case CIRCUIT -> "Circuit";
            case PRINTING1 -> "Printing Part 1"; case PRINTING2 -> "Printing Part 2"; case PRINTING3 -> "Printing Part 3"; case PRINTING4 -> "Printing Part 4";
            case PRINTING5 -> "Printing Part 5"; case PRINTING6 -> "Printing Part 6"; case PRINTING7 -> "Printing Part 7"; case PRINTING8 -> "Printing Part 8";
        }).withStyle(ChatFormatting.GRAY));
    }
}
