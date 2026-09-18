package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemEmptyPortableFluidContainer extends Item {
    private final int capacity;
    private final Supplier<Item> filledItem;

    public ItemEmptyPortableFluidContainer(Properties properties, int capacity, Supplier<Item> filledItem) {
        super(properties);
        this.capacity = capacity;
        this.filledItem = filledItem;
    }

    public int getCapacity() {
        return capacity;
    }

    public Item getFilledItem() {
        return filledItem.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("0 / " + capacity + " mB"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        NTMFluidType type = node.getFluidType();
        if (type == null || node.getFluidAmount() < capacity) {
            return InteractionResult.PASS;
        }
        int moved = node.drain(type, capacity);
        if (moved != capacity) {
            if (moved > 0) {
                node.fill(type, moved);
            }
            return InteractionResult.PASS;
        }
        ItemStack filled = new ItemStack(filledItem.get());
        ItemPortableFluidContainer.setFluid(filled, type, capacity);
        ItemStack held = context.getItemInHand();
        held.shrink(1);
        if (held.isEmpty()) {
            context.getPlayer().setItemInHand(context.getHand(), filled);
        } else if (!context.getPlayer().getInventory().add(filled)) {
            context.getPlayer().drop(filled, false);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
