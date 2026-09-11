package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.FatManBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class FatManMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    public FatManMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof FatManBlockEntity fatMan ? fatMan : null);
    }

    public FatManMenu(int id, Inventory playerInventory, FatManBlockEntity fatMan) {
        super(ModMenus.NUKE_MAN.get(), id);
        access = fatMan == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(playerInventory.player.level(), fatMan.getBlockPos());
        if (fatMan != null) {
            addSlot(new SlotItemHandler(fatMan.getInventory(), 0, 26, 35));
            addSlot(new SlotItemHandler(fatMan.getInventory(), 1, 8, 17));
            addSlot(new SlotItemHandler(fatMan.getInventory(), 2, 44, 17));
            addSlot(new SlotItemHandler(fatMan.getInventory(), 3, 8, 53));
            addSlot(new SlotItemHandler(fatMan.getInventory(), 4, 44, 53));
            addSlot(new SlotItemHandler(fatMan.getInventory(), 5, 98, 35));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.NUKE_MAN.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index < 6) {
            if (!moveItemStackTo(original, 6, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(original, 0, 6, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
