package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.BatterySocketBlockEntity;
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

public final class BatterySocketMenu extends AbstractContainerMenu {
    private final BatterySocketBlockEntity socket;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public BatterySocketMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof BatterySocketBlockEntity socket ? socket : null);
    }

    public BatterySocketMenu(int containerId, Inventory inventory, BatterySocketBlockEntity socket) {
        super(ModMenus.BATTERY_SOCKET.get(), containerId);
        this.socket = socket;
        this.access = socket == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(inventory.player.level(), socket.getBlockPos());
        this.data = socket == null ? new SimpleContainerData(5) : socket.getData();
        if (socket != null) {
            addSlot(new SlotItemHandler(socket.getInventory(), 0, 35, 35));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 99 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 157));
        }
        addDataSlots(data);
    }

    public int getModeWithoutSignal() {
        return data.get(0);
    }

    public int getModeWithSignal() {
        return data.get(1);
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getCapacity() {
        return data.get(3);
    }

    public int getDelta() {
        return data.get(4);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (socket == null || id < 0 || id > 1) {
            return false;
        }
        socket.cycleMode(id == 1);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MACHINE_BATTERY_SOCKET.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
