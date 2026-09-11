package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemPortableFluidContainer extends Item {
    private final int capacity;
    private final Supplier<Item> emptyItem;

    public ItemPortableFluidContainer(Properties properties, int capacity, Supplier<Item> emptyItem) {
        super(properties);
        this.capacity = capacity;
        this.emptyItem = emptyItem;
    }

    public int getCapacity() {
        return capacity;
    }

    public Item getEmptyItem() {
        return emptyItem.get();
    }

    public static NTMFluidType getFluidType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return NTMFluidType.byId(tag.getString("Fluid"));
    }

    public static int getAmount(ItemStack stack) {
        return Math.max(0, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Amount"));
    }

    public static void setFluid(ItemStack stack, NTMFluidType type, int amount) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (type == null || amount <= 0) {
            tag.remove("Fluid");
            tag.remove("Amount");
        } else {
            tag.putString("Fluid", type.id());
            tag.putInt("Amount", amount);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static ItemStack configured(Item item, NTMFluidType type, int amount) {
        ItemStack stack = new ItemStack(item);
        setFluid(stack, type, amount);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMFluidType type = getFluidType(stack);
        if (type == null) {
            return super.getName(stack);
        }
        return super.getName(stack).copy().append(" - ").append(type.displayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NTMFluidType type = getFluidType(stack);
        int amount = getAmount(stack);
        if (type != null && amount > 0) {
            tooltip.add(Component.literal(amount + " / " + capacity + " mB").append(" ").append(type.displayName()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        NTMFluidType type = getFluidType(stack);
        int stored = getAmount(stack);
        if (type != null && stored > 0 && node.accepts(type)) {
            int room = Math.max(0, node.getFluidCapacity() - node.getFluidAmount());
            if (stack.getCount() == 1 || room >= stored) {
                int moved = node.fill(type, Math.min(stored, room));
                if (moved > 0) {
                    if (moved == stored) {
                        ItemStack empty = new ItemStack(emptyItem.get());
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            context.getPlayer().setItemInHand(context.getHand(), empty);
                        } else if (!context.getPlayer().getInventory().add(empty)) {
                            context.getPlayer().drop(empty, false);
                        }
                    } else if (stack.getCount() == 1) {
                        setFluid(stack, type, stored - moved);
                    }
                    return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
                }
            }
        }

        NTMFluidType nodeType = node.getFluidType();
        if (nodeType != null && node.getFluidAmount() > 0 && (type == null || type == nodeType)) {
            int room = capacity - stored;
            if (room > 0) {
                int moved = node.drain(nodeType, room);
                if (moved > 0) {
                    if (stack.getCount() == 1) {
                        setFluid(stack, nodeType, stored + moved);
                    } else {
                        ItemStack filled = stack.copyWithCount(1);
                        setFluid(filled, nodeType, stored + moved);
                        stack.shrink(1);
                        if (!context.getPlayer().getInventory().add(filled)) {
                            context.getPlayer().drop(filled, false);
                        }
                    }
                    return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
                }
            }
        }
        return InteractionResult.PASS;
    }
}
