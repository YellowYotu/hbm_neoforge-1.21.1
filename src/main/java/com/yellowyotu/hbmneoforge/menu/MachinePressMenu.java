package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import com.yellowyotu.hbmneoforge.item.ItemStamp;
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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MachinePressMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOT_COUNT = 13;

    private static final int PLAYER_INVENTORY_START = 13;
    private static final int PLAYER_INVENTORY_END = 40;
    private static final int HOTBAR_START = 40;
    private static final int HOTBAR_END = 49;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    /*
     * Клиентский конструктор.
     */
    public MachinePressMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(MACHINE_SLOT_COUNT),
                new SimpleContainerData(4),
                buffer.readBlockPos()
        );
    }

    /*
     * Серверный конструктор.
     */
    public MachinePressMenu(
            int containerId,
            Inventory playerInventory,
            ItemStackHandler machineInventory,
            ContainerData data,
            BlockPos pos
    ) {
        super(ModMenus.MACHINE_PRESS.get(), containerId);

        this.access = ContainerLevelAccess.create(
                playerInventory.player.level(),
                pos
        );

        this.data = data;

        checkContainerDataCount(data, 4);

        addMachineSlots(machineInventory);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(data);
    }

    private void addMachineSlots(ItemStackHandler inventory) {
        // Топливо
        addSlot(new FuelSlot(
                inventory,
                MachinePressBlockEntity.SLOT_FUEL,
                26,
                53
        ));

        // Any iron or steel press stamp
        addSlot(new StampSlot(
                inventory,
                MachinePressBlockEntity.SLOT_STAMP,
                80,
                17
        ));

        // Входной материал
        addSlot(new SlotItemHandler(
                inventory,
                MachinePressBlockEntity.SLOT_INPUT,
                80,
                53
        ));

        // Результат
        addSlot(new OutputSlot(
                inventory,
                MachinePressBlockEntity.SLOT_OUTPUT,
                140,
                35
        ));

        // Девять дополнительных слотов
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(
                    inventory,
                    MachinePressBlockEntity.STORAGE_START + i,
                    8 + i * 18,
                    84
            ));
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        132 + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    inventory,
                    column,
                    8 + column * 18,
                    190
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                access,
                player,
                ModBlocks.MACHINE_PRESS.get()
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();

        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(
                    source,
                    PLAYER_INVENTORY_START,
                    HOTBAR_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemStamp) {
            if (!moveItemStackTo(source, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(Items.COAL)
                || source.is(Items.CHARCOAL)) {

            if (!moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(source, 2, 3, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, source);

        return original;
    }

    public int getSpeed() {
        return data.get(0);
    }

    public int getBurnTime() {
        return data.get(1);
    }

    public int getPressProgress() {
        return data.get(2);
    }

    public boolean isRetracting() {
        return data.get(3) != 0;
    }

    private static final class FuelSlot extends SlotItemHandler {

        private FuelSlot(
                ItemStackHandler inventory,
                int index,
                int x,
                int y
        ) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.COAL)
                    || stack.is(Items.CHARCOAL);
        }
    }

    private static final class StampSlot extends SlotItemHandler {

        private StampSlot(
                ItemStackHandler inventory,
                int index,
                int x,
                int y
        ) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ItemStamp;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class OutputSlot extends SlotItemHandler {

        private OutputSlot(
                ItemStackHandler inventory,
                int index,
                int x,
                int y
        ) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}