package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.blockentity.StorageCrateBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class StorageCrateMenu extends AbstractContainerMenu {
    private final Container container;
    private final int rows;
    private final boolean held;

    public StorageCrateMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(id, playerInventory, readContainer(playerInventory, buffer));
    }

    private StorageCrateMenu(int id, Inventory playerInventory, ClientData data) {
        this(id, playerInventory, data.container(), data.rows(), data.held());
    }

    public StorageCrateMenu(int id, Inventory playerInventory, Container container, int rows) {
        this(id, playerInventory, container, rows, container instanceof com.yellowyotu.hbmneoforge.item.StorageCrateBlockItem.BoundCrateContainer);
    }

    private StorageCrateMenu(int id, Inventory playerInventory, Container container, int rows, boolean held) {
        super(ModMenus.STORAGE_CRATE.get(), id);
        this.container = container;
        this.rows = rows;
        this.held = held;
        container.startOpen(playerInventory.player);

        int crateTop = 18;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(container, column + row * 9, 8 + column * 18, crateTop + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return container.canPlaceItem(getSlotIndex(), stack);
                    }
                });
            }
        }

        int playerTop = crateTop + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, playerTop + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, playerTop + 58));
        }
    }

    private static ClientData readContainer(Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        int rows = buffer.readVarInt();
        boolean held = buffer.readBoolean();
        if (held) {
            return new ClientData(new SimpleContainer(rows * 9), rows, true);
        }
        var pos = buffer.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof StorageCrateBlockEntity crate) {
            return new ClientData(crate, rows, false);
        }
        return new ClientData(new SimpleContainer(rows * 9), rows, false);
    }

    public int getRows() {
        return rows;
    }

    @Override
    public boolean stillValid(Player player) {
        return held || container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        int crateSlots = rows * 9;
        if (index < crateSlots) {
            if (!moveItemStackTo(original, crateSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, 0, crateSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player.blockPosition(), ModSounds.CRATE_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private record ClientData(Container container, int rows, boolean held) {
    }
}
