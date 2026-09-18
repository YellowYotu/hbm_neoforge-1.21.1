package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.MixerBlockEntity;
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

public final class MixerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = MixerBlockEntity.INVENTORY_SIZE;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final MixerBlockEntity machine;

    public MixerMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(12));
    }

    public MixerMenu(int containerId, Inventory inventory, MixerBlockEntity machine) {
        this(containerId, inventory, machine, machine.getData());
    }

    private MixerMenu(int containerId, Inventory inventory, MixerBlockEntity machine, ContainerData data) {
        super(ModMenus.MIXER.get(), containerId);
        this.machine = machine;
        this.data = data;
        this.access = ContainerLevelAccess.create(inventory.player.level(), machine.getBlockPos());
        ItemStackHandler handler = machine.getInventory();
        addSlot(new BatterySlot(handler, MixerBlockEntity.SLOT_BATTERY, 23, 77));
        addSlot(new SlotItemHandler(handler, MixerBlockEntity.SLOT_SOLID_INPUT, 43, 77));
        addSlot(new IdentifierSlot(handler, MixerBlockEntity.SLOT_FLUID_IDENTIFIER, 117, 77));
        addSlot(new UpgradeSlot(handler, MixerBlockEntity.SLOT_UPGRADE_1, 137, 24));
        addSlot(new UpgradeSlot(handler, MixerBlockEntity.SLOT_UPGRADE_2, 137, 42));
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

    private static MixerBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof MixerBlockEntity machine) {
            return machine;
        }
        return new MixerBlockEntity(pos, ModBlocks.MIXER.get().defaultBlockState());
    }

    public int getEnergy() { return data.get(0); }
    public int getMaxEnergy() { return data.get(1); }
    public int getProgress() { return data.get(2); }
    public int getMaxProgress() { return data.get(3); }
    public int getRecipeIndex() { return data.get(4); }
    public boolean isProcessing() { return data.get(5) != 0; }
    public int getInput1Amount() { return data.get(6); }
    public NTMFluidType getInput1Type() { return NTMFluidType.byOrdinalSafe(data.get(7)); }
    public int getInput2Amount() { return data.get(8); }
    public NTMFluidType getInput2Type() { return NTMFluidType.byOrdinalSafe(data.get(9)); }
    public int getOutputAmount() { return data.get(10); }
    public NTMFluidType getOutputType() { return NTMFluidType.byOrdinalSafe(data.get(11)); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            machine.cycleRecipe();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MIXER.get());
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
            if (!moveItemStackTo(source, MixerBlockEntity.SLOT_BATTERY, MixerBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(source, MixerBlockEntity.SLOT_UPGRADE_1, MixerBlockEntity.SLOT_UPGRADE_2 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemFluidIdentifier || source.getItem() instanceof ItemFluidIdentifierMulti) {
            if (!moveItemStackTo(source, MixerBlockEntity.SLOT_FLUID_IDENTIFIER, MixerBlockEntity.SLOT_FLUID_IDENTIFIER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, MixerBlockEntity.SLOT_SOLID_INPUT, MixerBlockEntity.SLOT_SOLID_INPUT + 1, false)) {
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
        private BatterySlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return ItemBatteryPack.isBattery(stack); }
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
