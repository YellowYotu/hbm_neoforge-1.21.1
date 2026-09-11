package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.AbstractHeaterBlockEntity;
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
import net.neoforged.neoforge.items.SlotItemHandler;

public final class HeaterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 2;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final AbstractHeaterBlockEntity heater;
    public HeaterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { this(id, inventory, getHeater(inventory, buffer.readBlockPos()), new SimpleContainerData(6)); }
    public HeaterMenu(int id, Inventory inventory, AbstractHeaterBlockEntity heater, ContainerData data) {
        super(ModMenus.HEATER.get(), id); this.heater = heater; this.access = ContainerLevelAccess.create(inventory.player.level(), heater.getBlockPos()); this.data = data;
        addSlot(new FuelSlot(heater.getInventory(), 0, 44, 27));
        addSlot(new FuelSlot(heater.getInventory(), 1, 62, 27));
        for (int row = 0; row < 3; row++) { for (int column = 0; column < 9; column++) { addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 86 + row * 18)); } }
        for (int column = 0; column < 9; column++) { addSlot(new Slot(inventory, column, 8 + column * 18, 144)); }
        addDataSlots(data);
    }
    private static AbstractHeaterBlockEntity getHeater(Inventory inventory, BlockPos pos) { if (inventory.player.level().getBlockEntity(pos) instanceof AbstractHeaterBlockEntity heater) { return heater; } return new com.yellowyotu.hbmneoforge.blockentity.FireboxBlockEntity(pos, ModBlocks.FIREBOX.get().defaultBlockState()); }
    public int getMaxBurnTime() { return data.get(0); } public int getBurnTime() { return data.get(1); } public int getBurnHeat() { return data.get(2); } public int getHeatStored() { return data.get(3); } public int getMaxHeat() { return data.get(4); } public boolean isBurning() { return data.get(5) != 0; }
    public boolean isHeatingOven() { return heater instanceof com.yellowyotu.hbmneoforge.blockentity.HeatingOvenBlockEntity; }
    @Override public boolean stillValid(Player player) { return access.evaluate((level, pos) -> player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true); }
    @Override public ItemStack quickMoveStack(Player player, int index) { Slot slot = slots.get(index); if (!slot.hasItem()) { return ItemStack.EMPTY; } ItemStack source = slot.getItem(); ItemStack original = source.copy(); if (index < MACHINE_SLOTS) { if (!moveItemStackTo(source, MACHINE_SLOTS, slots.size(), true)) { return ItemStack.EMPTY; } } else if (source.getBurnTime(null) > 0) { if (!moveItemStackTo(source, 0, MACHINE_SLOTS, false)) { return ItemStack.EMPTY; } } else { return ItemStack.EMPTY; } if (source.isEmpty()) { slot.set(ItemStack.EMPTY); } else { slot.setChanged(); } return original; }
    private static final class FuelSlot extends SlotItemHandler { FuelSlot(net.neoforged.neoforge.items.ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); } @Override public boolean mayPlace(ItemStack stack) { return stack.getBurnTime(null) > 0; } }
}
