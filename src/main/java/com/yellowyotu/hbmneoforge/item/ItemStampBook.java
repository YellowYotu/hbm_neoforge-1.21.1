package com.yellowyotu.hbmneoforge.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ItemStampBook extends ItemStamp {
    private static final StampType[] PRINTING_TYPES = { StampType.PRINTING1, StampType.PRINTING2, StampType.PRINTING3, StampType.PRINTING4, StampType.PRINTING5, StampType.PRINTING6, StampType.PRINTING7, StampType.PRINTING8 };

    public ItemStampBook(Properties properties) {
        super(properties, StampType.PRINTING1);
    }

    @Override
    public StampType getStampType(ItemStack stack) {
        return PRINTING_TYPES[Math.min(stack.getDamageValue(), PRINTING_TYPES.length - 1)];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            stack.setDamageValue((stack.getDamageValue() + 1) % PRINTING_TYPES.length);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Printing Press Stamp (Part " + (stack.getDamageValue() + 1) + ")").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click to select the next part").withStyle(ChatFormatting.DARK_GRAY));
    }
}
