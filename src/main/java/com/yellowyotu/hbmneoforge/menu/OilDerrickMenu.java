package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.OilDerrickBlockEntity;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemEmptyPortableFluidContainer;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.item.ItemPortableFluidContainer;
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

public final class OilDerrickMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = OilDerrickBlockEntity.INVENTORY_SIZE;
    private final OilDerrickBlockEntity derrick;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public OilDerrickMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, getDerrick(inventory, buffer.readBlockPos()), new SimpleContainerData(5));
    }

    public OilDerrickMenu(int id, Inventory inventory, OilDerrickBlockEntity derrick, ContainerData data) {
        super(ModMenus.OIL_DERRICK.get(), id);
        this.derrick = derrick;
        this.access = ContainerLevelAccess.create(inventory.player.level(), derrick.getBlockPos());
        this.data = data;
        ItemStackHandler handler = derrick.getInventory();
        addSlot(new BatterySlot(handler, OilDerrickBlockEntity.SLOT_BATTERY, 8, 58));
        addSlot(new FluidContainerSlot(handler, OilDerrickBlockEntity.SLOT_OIL_INPUT, 94, 22));
        addSlot(new OutputSlot(handler, OilDerrickBlockEntity.SLOT_OIL_OUTPUT, 94, 58));
        addSlot(new FluidContainerSlot(handler, OilDerrickBlockEntity.SLOT_GAS_INPUT, 130, 22));
        addSlot(new OutputSlot(handler, OilDerrickBlockEntity.SLOT_GAS_OUTPUT, 130, 58));
        addSlot(new UpgradeSlot(handler, OilDerrickBlockEntity.SLOT_UPGRADE_1, 156, 36));
        addSlot(new UpgradeSlot(handler, OilDerrickBlockEntity.SLOT_UPGRADE_2, 156, 54));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 12 + col * 18, 108 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 12 + col * 18, 166));
        }
        addDataSlots(data);
    }

    private static OilDerrickBlockEntity getDerrick(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof OilDerrickBlockEntity derrick ? derrick : new OilDerrickBlockEntity(pos, ModBlocks.OIL_DERRICK.get().defaultBlockState());
    }

    public int getEnergy() { return data.get(0); }
    public int getMaxEnergy() { return data.get(1); }
    public int getOilAmount() { return data.get(2); }
    public int getGasAmount() { return data.get(3); }
    public int getIndicator() { return data.get(4); }

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
        } else if (ItemBatteryPack.isBattery(source)) {
            if (!moveItemStackTo(source, OilDerrickBlockEntity.SLOT_BATTERY, OilDerrickBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(source, OilDerrickBlockEntity.SLOT_UPGRADE_1, OilDerrickBlockEntity.SLOT_UPGRADE_2 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemEmptyPortableFluidContainer || source.getItem() instanceof ItemPortableFluidContainer) {
            if (!moveItemStackTo(source, OilDerrickBlockEntity.SLOT_OIL_INPUT, OilDerrickBlockEntity.SLOT_OIL_INPUT + 1, false) && !moveItemStackTo(source, OilDerrickBlockEntity.SLOT_GAS_INPUT, OilDerrickBlockEntity.SLOT_GAS_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.OIL_DERRICK.get());
    }

    private static final class BatterySlot extends SlotItemHandler {
        BatterySlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return ItemBatteryPack.isBattery(stack); }
    }

    private static final class FluidContainerSlot extends SlotItemHandler {
        FluidContainerSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ItemEmptyPortableFluidContainer || stack.getItem() instanceof ItemPortableFluidContainer; }
    }

    private static final class UpgradeSlot extends SlotItemHandler {
        UpgradeSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ItemMachineUpgrade; }
    }

    private static final class OutputSlot extends SlotItemHandler {
        OutputSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
