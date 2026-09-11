package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemFluidIdentifierBlank extends Item {
    public ItemFluidIdentifierBlank(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidStorageBlockEntity storage) || storage.getFluidType() == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            ItemStack replacement = new ItemStack(ModItems.identifierFor(storage.getFluidType()));
            InteractionHand hand = context.getHand();
            context.getPlayer().setItemInHand(hand, replacement);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
