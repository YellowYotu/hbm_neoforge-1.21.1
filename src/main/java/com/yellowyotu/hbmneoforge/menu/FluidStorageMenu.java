package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class FluidStorageMenu extends AbstractContainerMenu {
    private final FluidStorageBlockEntity storage;
    private final ContainerData data;

    public FluidStorageMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof FluidStorageBlockEntity storage ? storage : null);
    }

    public FluidStorageMenu(int id, Inventory inventory, FluidStorageBlockEntity storage) {
        super(ModMenus.FLUID_STORAGE.get(), id);
        this.storage = storage;
        this.data = storage == null ? new SimpleContainerData(6) : storage.getData();

        if (storage != null) {
            addSlot(new SlotItemHandler(storage.getInventory(), 0, 8, 17));
            addSlot(new OutputSlot(storage.getInventory(), 1, 8, 53));
            addSlot(new SlotItemHandler(storage.getInventory(), 2, 35, 17));
            addSlot(new OutputSlot(storage.getInventory(), 3, 35, 53));
            addSlot(new SlotItemHandler(storage.getInventory(), 4, 125, 17));
            addSlot(new OutputSlot(storage.getInventory(), 5, 125, 53));
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public int amount() { return data.get(0); }
    public int capacity() { return data.get(1); }
    public int fluidOrdinal() { return data.get(2); }
    public int modeOrdinal() { return data.get(3); }
    public int transferRate() { return data.get(4); }
    public int kindOrdinal() { return data.get(5); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (storage == null) {
            return false;
        }
        if (id == 0) {
            storage.cycleMode();
            return true;
        }
        int current = storage.getFluidType() == null ? -1 : storage.getFluidType().ordinal();
        if (id == 1) {
            storage.setFluidType(NTMFluidType.byOrdinalSafe(Math.floorMod(current + 1, NTMFluidType.values().length)));
            return true;
        }
        if (id == 2) {
            storage.setFluidType(NTMFluidType.byOrdinalSafe(Math.floorMod(current - 1, NTMFluidType.values().length)));
            return true;
        }
        if (id == 3) {
            storage.setFluidType(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return storage != null && !storage.isRemoved() && player.distanceToSqr(storage.getBlockPos().getX() + .5, storage.getBlockPos().getY() + .5, storage.getBlockPos().getZ() + .5) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (storage == null || index < 0 || index >= slots.size()) {
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
            boolean moved = moveItemStackTo(original, 0, 1, false)
                    || moveItemStackTo(original, 2, 3, false)
                    || moveItemStackTo(original, 4, 5, false);
            if (!moved) {
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

    private static final class OutputSlot extends SlotItemHandler {
        private OutputSlot(net.neoforged.neoforge.items.IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
