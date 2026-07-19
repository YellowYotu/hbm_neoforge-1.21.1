package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationRecipes;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
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

public final class SolderingStationMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOT_COUNT = SolderingStationBlockEntity.INVENTORY_SIZE;
    private final ContainerLevelAccess access;
    private final SolderingStationBlockEntity station;
    private final ContainerData data;

    public SolderingStationMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getStation(inventory, buffer.readBlockPos()), new SimpleContainerData(9));
    }

    public SolderingStationMenu(int containerId, Inventory inventory, SolderingStationBlockEntity station) {
        this(containerId, inventory, station, station.getData());
    }

    private SolderingStationMenu(int containerId, Inventory inventory, SolderingStationBlockEntity station, ContainerData data) {
        super(ModMenus.SOLDERING_STATION.get(), containerId);
        this.station = station;
        this.data = data;
        this.access = ContainerLevelAccess.create(inventory.player.level(), station.getBlockPos());
        ItemStackHandler handler = station.getInventory();

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = column + row * 3;
                addSlot(new IngredientSlot(handler, slot, 17 + column * 18, 18 + row * 18));
            }
        }

        addSlot(new OutputSlot(handler, SolderingStationBlockEntity.SLOT_OUTPUT, 107, 27));
        addSlot(new BatterySlot(handler, SolderingStationBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(new FluidCellSlot(handler, SolderingStationBlockEntity.SLOT_FLUID_CELL, 17, 63));
        addSlot(new UpgradeSlot(handler, SolderingStationBlockEntity.SLOT_UPGRADE_1, 89, 63));
        addSlot(new UpgradeSlot(handler, SolderingStationBlockEntity.SLOT_UPGRADE_2, 107, 63));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 122 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 180));
        }
        addDataSlots(data);
    }

    private static SolderingStationBlockEntity getStation(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof SolderingStationBlockEntity station) {
            return station;
        }
        return new SolderingStationBlockEntity(pos, ModBlocks.SOLDERING_STATION.get().defaultBlockState());
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getConsumption() {
        return data.get(2);
    }

    public int getProgress() {
        return data.get(3);
    }

    public int getProcessTime() {
        return data.get(4);
    }

    public boolean isCollisionPreventionEnabled() {
        return data.get(5) != 0;
    }

    public boolean isProcessing() {
        return data.get(6) != 0;
    }

    public int getFluidAmount() {
        return data.get(7);
    }

    public int getFluidTypeId() {
        return data.get(8);
    }

    public int getFluidColor() {
        int type = getFluidTypeId();
        return type < 0 || type >= ItemSolderingFluidCell.FluidType.values().length ? 0 : ItemSolderingFluidCell.FluidType.values()[type].getColor();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0) {
            return false;
        }
        if (!player.level().isClientSide()) {
            station.toggleCollisionPrevention();
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SOLDERING_STATION.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(source, MACHINE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(ModItems.BATTERY_PACK.get())) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_BATTERY, SolderingStationBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(ModItems.CELL_EMPTY.get()) || source.getItem() instanceof ItemSolderingFluidCell) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_FLUID_CELL, SolderingStationBlockEntity.SLOT_FLUID_CELL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_UPGRADE_1, SolderingStationBlockEntity.SLOT_UPGRADE_2 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingStationRecipes.isValidTopping(source)) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_TOPPING_START, SolderingStationBlockEntity.SLOT_TOPPING_START + SolderingStationBlockEntity.SLOT_TOPPING_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingStationRecipes.isValidPcb(source)) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_PCB_START, SolderingStationBlockEntity.SLOT_PCB_START + SolderingStationBlockEntity.SLOT_PCB_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SolderingStationRecipes.isValidSolder(source)) {
            if (!moveItemStackTo(source, SolderingStationBlockEntity.SLOT_SOLDER, SolderingStationBlockEntity.SLOT_SOLDER + 1, false)) {
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
        slot.onTake(player, source);
        return original;
    }

    private static final class IngredientSlot extends SlotItemHandler {
        private IngredientSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
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

    private static final class BatterySlot extends SlotItemHandler {
        private BatterySlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.BATTERY_PACK.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class FluidCellSlot extends SlotItemHandler {
        private FluidCellSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.CELL_EMPTY.get()) || stack.getItem() instanceof ItemSolderingFluidCell;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class UpgradeSlot extends SlotItemHandler {
        private UpgradeSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ItemMachineUpgrade;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
