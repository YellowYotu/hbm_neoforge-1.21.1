package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.BlastFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class BlastFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 4;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public BlastFurnaceMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getFurnace(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public BlastFurnaceMenu(int containerId, Inventory inventory, BlastFurnaceBlockEntity furnace, ContainerData data) {
        super(ModMenus.BLAST_FURNACE.get(), containerId);
        this.access = ContainerLevelAccess.create(inventory.player.level(), furnace.getBlockPos());
        this.data = data;
        ItemStackHandler handler = furnace.getInventory();
        addSlot(new SlotItemHandler(handler, BlastFurnaceBlockEntity.SLOT_INPUT_UPPER, 80, 18));
        addSlot(new SlotItemHandler(handler, BlastFurnaceBlockEntity.SLOT_INPUT_LOWER, 80, 54));
        addSlot(new FuelSlot(handler, BlastFurnaceBlockEntity.SLOT_FUEL, 8, 36));
        addSlot(new OutputSlot(handler, BlastFurnaceBlockEntity.SLOT_OUTPUT, 134, 36));
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

    private static BlastFurnaceBlockEntity getFurnace(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof BlastFurnaceBlockEntity furnace) {
            return furnace;
        }
        return new BlastFurnaceBlockEntity(pos, ModBlocks.BLAST_FURNACE.get().defaultBlockState());
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getFuel() {
        return data.get(1);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BLAST_FURNACE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(source, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (BlastFurnaceBlockEntity.getItemPower(source) > 0) {
            if (!moveItemStackTo(source, BlastFurnaceBlockEntity.SLOT_FUEL, BlastFurnaceBlockEntity.SLOT_FUEL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, BlastFurnaceBlockEntity.SLOT_INPUT_UPPER, BlastFurnaceBlockEntity.SLOT_INPUT_LOWER + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private static final class FuelSlot extends SlotItemHandler {
        private FuelSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return BlastFurnaceBlockEntity.getItemPower(stack) > 0;
        }
    }

    private static final class OutputSlot extends SlotItemHandler {
        private OutputSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
