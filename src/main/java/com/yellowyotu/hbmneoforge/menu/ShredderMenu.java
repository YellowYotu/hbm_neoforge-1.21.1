package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.ShredderBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.ShredderRecipes;
import com.yellowyotu.hbmneoforge.item.ItemShredderBlade;
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

public final class ShredderMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOT_COUNT = ShredderBlockEntity.INVENTORY_SIZE;
    private final ContainerLevelAccess access;
    private final ShredderBlockEntity shredder;
    private final ContainerData data;

    public ShredderMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getShredder(inventory, buffer.readBlockPos()), new SimpleContainerData(5));
    }

    public ShredderMenu(int containerId, Inventory inventory, ShredderBlockEntity shredder) {
        this(containerId, inventory, shredder, shredder.getData());
    }

    private ShredderMenu(int containerId, Inventory inventory, ShredderBlockEntity shredder, ContainerData data) {
        super(ModMenus.SHREDDER.get(), containerId);
        this.shredder = shredder;
        this.data = data;
        this.access = ContainerLevelAccess.create(inventory.player.level(), shredder.getBlockPos());
        ItemStackHandler handler = shredder.getInventory();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new InputSlot(handler, ShredderBlockEntity.INPUT_START + column + row * 3, 44 + column * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new OutputSlot(handler, ShredderBlockEntity.OUTPUT_START + column + row * 3, 116 + column * 18, 18 + row * 18));
            }
        }

        addSlot(new BladeSlot(handler, ShredderBlockEntity.SLOT_BLADE_LEFT, 44, 108));
        addSlot(new BladeSlot(handler, ShredderBlockEntity.SLOT_BLADE_RIGHT, 80, 108));
        addSlot(new BatterySlot(handler, ShredderBlockEntity.SLOT_BATTERY, 8, 108));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 151 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 209));
        }
        addDataSlots(data);
    }

    private static ShredderBlockEntity getShredder(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ShredderBlockEntity shredder) {
            return shredder;
        }
        return new ShredderBlockEntity(pos, ModBlocks.SHREDDER.get().defaultBlockState());
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

    public boolean isProcessing() {
        return data.get(4) != 0;
    }

    public int getBladeState(int machineSlot) {
        return shredder.getBladeState(machineSlot);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SHREDDER.get());
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
        } else if (source.getItem() instanceof ItemShredderBlade) {
            if (!moveItemStackTo(source, ShredderBlockEntity.SLOT_BLADE_LEFT, ShredderBlockEntity.SLOT_BLADE_RIGHT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(ModItems.BATTERY_PACK.get())) {
            if (!moveItemStackTo(source, ShredderBlockEntity.SLOT_BATTERY, ShredderBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ShredderRecipes.canShred(source)) {
            if (!moveItemStackTo(source, ShredderBlockEntity.INPUT_START, ShredderBlockEntity.INPUT_START + ShredderBlockEntity.INPUT_COUNT, false)) {
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

    private static final class InputSlot extends SlotItemHandler {
        private InputSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ShredderRecipes.canShred(stack);
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

    private static final class BladeSlot extends SlotItemHandler {
        private BladeSlot(ItemStackHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ItemShredderBlade;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
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
}
