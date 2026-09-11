package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public final class ItemFluidIcon extends Item {
    public ItemFluidIcon(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack make(NTMFluidType type, int amount) {
        ItemStack stack = new ItemStack(ModItems.FLUID_ICON.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("Fluid", type.id());
        tag.putInt("Amount", Math.max(0, amount));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static NTMFluidType getFluidType(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return NTMFluidType.byId(tag.getString("Fluid"));
    }

    public static int getAmount(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return Math.max(0, tag.getInt("Amount"));
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMFluidType type = getFluidType(stack);
        return type == null ? Component.translatable("item.hbm_neoforge.fluid_icon") : type.displayName();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int amount = getAmount(stack);
        if (amount > 0) {
            tooltip.add(Component.literal(amount + " mB"));
        }
    }
}
