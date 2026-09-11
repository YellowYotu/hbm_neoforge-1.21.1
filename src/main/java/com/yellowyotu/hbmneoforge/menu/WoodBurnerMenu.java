package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.WoodBurnerBlockEntity;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
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

public final class WoodBurnerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 3;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final WoodBurnerBlockEntity burner;
    public WoodBurnerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { this(id, inventory, getBurner(inventory, buffer.readBlockPos()), new SimpleContainerData(4)); }
    public WoodBurnerMenu(int id, Inventory inventory, WoodBurnerBlockEntity burner, ContainerData data) {
        super(ModMenus.WOOD_BURNER.get(), id); this.burner = burner; this.access = ContainerLevelAccess.create(inventory.player.level(), burner.getBlockPos()); this.data = data;
        addSlot(new FuelSlot(burner.getInventory(), WoodBurnerBlockEntity.SLOT_FUEL, 26, 18));
        addSlot(new OutputSlot(burner.getInventory(), WoodBurnerBlockEntity.SLOT_OUTPUT, 26, 54));
        addSlot(new BatterySlot(burner.getInventory(), WoodBurnerBlockEntity.SLOT_BATTERY, 143, 54));
        for (int row = 0; row < 3; row++) { for (int column = 0; column < 9; column++) { addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 104 + row * 18)); } }
        for (int column = 0; column < 9; column++) { addSlot(new Slot(inventory, column, 8 + column * 18, 162)); }
        addDataSlots(data);
    }
    private static WoodBurnerBlockEntity getBurner(Inventory inventory, BlockPos pos) { return inventory.player.level().getBlockEntity(pos) instanceof WoodBurnerBlockEntity burner ? burner : new WoodBurnerBlockEntity(pos, ModBlocks.WOOD_BURNER.get().defaultBlockState()); }
    public int getBurnTime() { return data.get(0); } public int getMaxBurnTime() { return data.get(1); } public int getPower() { return data.get(2); } public boolean isEnabled() { return data.get(3) != 0; }
    @Override public boolean clickMenuButton(Player player, int id) { if (id != 0) { return false; } if (!player.level().isClientSide()) { burner.toggleEnabled(); } return true; }
    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.WOOD_BURNER.get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { Slot slot = slots.get(index); if (!slot.hasItem()) { return ItemStack.EMPTY; } ItemStack source = slot.getItem(); ItemStack original = source.copy(); if (index < MACHINE_SLOTS) { if (!moveItemStackTo(source, MACHINE_SLOTS, slots.size(), true)) { return ItemStack.EMPTY; } } else if (source.getBurnTime(null) > 0) { if (!moveItemStackTo(source, WoodBurnerBlockEntity.SLOT_FUEL, WoodBurnerBlockEntity.SLOT_FUEL + 1, false)) { return ItemStack.EMPTY; } } else if (ItemBatteryPack.isBattery(source)) { if (!moveItemStackTo(source, WoodBurnerBlockEntity.SLOT_BATTERY, WoodBurnerBlockEntity.SLOT_BATTERY + 1, false)) { return ItemStack.EMPTY; } } else { return ItemStack.EMPTY; } if (source.isEmpty()) { slot.set(ItemStack.EMPTY); } else { slot.setChanged(); } return original; }
    private static final class FuelSlot extends SlotItemHandler { FuelSlot(net.neoforged.neoforge.items.ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); } @Override public boolean mayPlace(ItemStack stack) { return stack.getBurnTime(null) > 0; } }
    private static final class BatterySlot extends SlotItemHandler { BatterySlot(net.neoforged.neoforge.items.ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); } @Override public boolean mayPlace(ItemStack stack) { return ItemBatteryPack.isBattery(stack); } }
    private static final class OutputSlot extends SlotItemHandler { OutputSlot(net.neoforged.neoforge.items.ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); } @Override public boolean mayPlace(ItemStack stack) { return false; } }
}
