package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.function.Supplier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        NTMFluidType type = node.getFluidType();
        if (type == null || node.getFluidAmount() <= 0) {
            return InteractionResult.PASS;
        }
        int moved = node.drain(type, Math.min(capacity, node.getFluidAmount()));
        if (moved <= 0) {
            return InteractionResult.PASS;
        }
        ItemStack filled = new ItemStack(filledItem.get());
        ItemPortableFluidContainer.setFluid(filled, type, moved);
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
