package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineRecipes;
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

public final class AssemblyMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 17;
    private final ContainerLevelAccess access;
    private final AssemblyMachineBlockEntity machine;
    private final ContainerData data;

    public AssemblyMachineMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(6));
    }

    public AssemblyMachineMenu(int containerId, Inventory inventory, AssemblyMachineBlockEntity machine) {
        this(containerId, inventory, machine, machine.getData());
    }

    private AssemblyMachineMenu(int containerId, Inventory inventory, AssemblyMachineBlockEntity machine, ContainerData data) {
        super(ModMenus.ASSEMBLY_MACHINE.get(), containerId);
        this.machine = machine;
        this.data = data;
        access = ContainerLevelAccess.create(inventory.player.level(), machine.getBlockPos());
        ItemStackHandler handler = machine.getInventory();
        addSlot(new BatterySlot(handler, AssemblyMachineBlockEntity.SLOT_BATTERY, 152, 81));
        addSlot(new UpgradeSlot(handler, AssemblyMachineBlockEntity.SLOT_FILTER, 35, 126));
        addSlot(new UpgradeSlot(handler, AssemblyMachineBlockEntity.SLOT_UPGRADE_1, 152, 108));
        addSlot(new UpgradeSlot(handler, AssemblyMachineBlockEntity.SLOT_UPGRADE_2, 170, 108));
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(handler, AssemblyMachineBlockEntity.INPUT_START + column + row * 3, 8 + column * 18, 18 + row * 18));
            }
        }
        addSlot(new OutputSlot(handler, AssemblyMachineBlockEntity.SLOT_OUTPUT, 98, 45));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 174 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 232));
        }
        addDataSlots(data);
    }

    private static AssemblyMachineBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof AssemblyMachineBlockEntity machine) {
            return machine;
        }
        return new AssemblyMachineBlockEntity(pos, ModBlocks.ASSEMBLY_MACHINE.get().defaultBlockState());
    }

    public AssemblyMachineBlockEntity getMachine() {
        return machine;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getMaxProgress() {
        return data.get(3);
    }

    public int getSelectedRecipe() {
        return data.get(4);
    }

    public boolean isProcessing() {
        return data.get(5) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 100 && id < 100 + AssemblyMachineRecipes.RECIPES.size()) {
            if (!player.level().isClientSide()) {
                machine.selectRecipe(id - 100);
            }
            return true;
        }
        if (id == 99) {
            if (!player.level().isClientSide()) {
                machine.selectRecipe(-1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ASSEMBLY_MACHINE.get());
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
            if (!moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade) {
            if (!moveItemStackTo(source, 1, 4, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 4, 16, false)) {
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

    private static final class BatterySlot extends SlotItemHandler {
        private BatterySlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.BATTERY_PACK.get());
        }
    }

    private static final class UpgradeSlot extends SlotItemHandler {
        private UpgradeSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
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
