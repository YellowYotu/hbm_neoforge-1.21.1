package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifier;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
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

public final class ArcWelderMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ArcWelderBlockEntity.INVENTORY_SIZE;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ArcWelderMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(8));
    }

    public ArcWelderMenu(int containerId, Inventory inventory, ArcWelderBlockEntity machine) {
        this(containerId, inventory, machine, machine.getData());
    }

    public ArcWelderMenu(int containerId, Inventory inventory, ArcWelderBlockEntity machine, ContainerData data) {
        super(ModMenus.ARC_WELDER.get(), containerId);
        this.data = data;
        this.access = ContainerLevelAccess.create(inventory.player.level(), machine.getBlockPos());
        ItemStackHandler handler = machine.getInventory();

        addSlot(new SlotItemHandler(handler, ArcWelderBlockEntity.SLOT_INPUT_1, 17, 36));
        addSlot(new SlotItemHandler(handler, ArcWelderBlockEntity.SLOT_INPUT_2, 35, 36));
        addSlot(new SlotItemHandler(handler, ArcWelderBlockEntity.SLOT_INPUT_3, 53, 36));
        addSlot(new OutputSlot(handler, ArcWelderBlockEntity.SLOT_OUTPUT, 107, 36));
        addSlot(new BatterySlot(handler, ArcWelderBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(new IdentifierSlot(handler, ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER, 17, 63));
        addSlot(new UpgradeSlot(handler, ArcWelderBlockEntity.SLOT_UPGRADE_1, 89, 63));
        addSlot(new UpgradeSlot(handler, ArcWelderBlockEntity.SLOT_UPGRADE_2, 107, 63));

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

    private static ArcWelderBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ArcWelderBlockEntity machine) {
            return machine;
        }
        return new ArcWelderBlockEntity(pos, ModBlocks.ARC_WELDER.get().defaultBlockState());
    }

    public int getEnergy() { return data.get(0); }
    public int getMaxEnergy() { return data.get(1); }
    public int getConsumption() { return data.get(2); }
    public int getProgress() { return data.get(3); }
    public int getProcessTime() { return data.get(4); }
    public boolean isProcessing() { return data.get(5) != 0; }
    public int getFluidAmount() { return data.get(6); }
    public NTMFluidType getFluidType() { return NTMFluidType.byOrdinalSafe(data.get(7)); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ARC_WELDER.get());
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
        } else if (ItemBatteryPack.isBattery(source)) {
            if (!moveItemStackTo(source, ArcWelderBlockEntity.SLOT_BATTERY, ArcWelderBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(source, ArcWelderBlockEntity.SLOT_UPGRADE_1, ArcWelderBlockEntity.SLOT_UPGRADE_2 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemFluidIdentifier || source.getItem() instanceof ItemFluidIdentifierMulti) {
            if (!moveItemStackTo(source, ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER, ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, ArcWelderBlockEntity.SLOT_INPUT_1, ArcWelderBlockEntity.SLOT_INPUT_3 + 1, false)) {
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

    private static final class OutputSlot extends SlotItemHandler {
        private OutputSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }

    private static final class BatterySlot extends SlotItemHandler {
        private BatterySlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return ItemBatteryPack.isBattery(stack); }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class IdentifierSlot extends SlotItemHandler {
        private IdentifierSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ItemFluidIdentifier || stack.getItem() instanceof ItemFluidIdentifierMulti; }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class UpgradeSlot extends SlotItemHandler {
        private UpgradeSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ItemMachineUpgrade; }
        @Override public int getMaxStackSize() { return 1; }
    }
}
