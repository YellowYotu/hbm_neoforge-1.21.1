package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantRecipes;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
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
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ChemicalPlantMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ChemicalPlantBlockEntity.INVENTORY_SIZE;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final ChemicalPlantBlockEntity machine;

    public ChemicalPlantMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, getMachine(inventory, buffer.readBlockPos()), new SimpleContainerData(18));
    }

    public ChemicalPlantMenu(int containerId, Inventory inventory, ChemicalPlantBlockEntity machine) {
        this(containerId, inventory, machine, machine.getData());
    }

    private ChemicalPlantMenu(int containerId, Inventory inventory, ChemicalPlantBlockEntity machine, ContainerData data) {
        super(ModMenus.CHEMICAL_PLANT.get(), containerId);
        this.machine = machine;
        this.data = data;
        access = ContainerLevelAccess.create(inventory.player.level(), machine.getBlockPos());
        ItemStackHandler handler = machine.getInventory();
        addSlot(new BatterySlot(handler, ChemicalPlantBlockEntity.SLOT_BATTERY, 152, 81));
        for (int i = 0; i < ChemicalPlantBlockEntity.INPUT_COUNT; i++) {
            addSlot(new SlotItemHandler(handler, ChemicalPlantBlockEntity.INPUT_START + i, 8 + i * 18, 99));
        }
        addSlot(new OutputSlot(handler, ChemicalPlantBlockEntity.SLOT_OUTPUT, 80, 99));
        for (int i = 0; i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            addSlot(new FluidInputContainerSlot(handler, ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_CONTAINER_START + i, 8 + i * 18, 54));
        }
        for (int i = 0; i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            addSlot(new OutputSlot(handler, ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_OUTPUT_START + i, 8 + i * 18, 72));
        }
        addSlot(new UpgradeSlot(handler, ChemicalPlantBlockEntity.SLOT_UPGRADE_1, 152, 108));
        addSlot(new UpgradeSlot(handler, ChemicalPlantBlockEntity.SLOT_UPGRADE_2, 152, 126));
        for (int i = 0; i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            addSlot(new FluidOutputContainerSlot(handler, ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_CONTAINER_START + i, 80 + i * 18, 54));
        }
        for (int i = 0; i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            addSlot(new OutputSlot(handler, ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_START + i, 80 + i * 18, 72));
        }
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

    private static ChemicalPlantBlockEntity getMachine(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof ChemicalPlantBlockEntity machine) {
            return machine;
        }
        return new ChemicalPlantBlockEntity(pos, ModBlocks.CHEMICAL_PLANT.get().defaultBlockState());
    }

    public int getEnergy() { return data.get(0); }
    public int getMaxEnergy() { return data.get(1); }
    public int getProgress() { return data.get(2); }
    public int getMaxProgress() { return data.get(3); }
    public int getActiveRecipe() { return data.get(4); }
    public boolean isProcessing() { return data.get(5) != 0; }
    public int getFluidAmount(boolean input, int slot) { return data.get((input ? 6 : 12) + slot * 2); }
    public NTMFluidType getFluidType(boolean input, int slot) { return NTMFluidType.byOrdinalSafe(data.get((input ? 7 : 13) + slot * 2)); }
    public ChemicalPlantRecipes.Recipe getRecipe() { int index = getActiveRecipe(); return index >= 0 && index < ChemicalPlantRecipes.RECIPES.size() ? ChemicalPlantRecipes.RECIPES.get(index) : null; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 99) {
            machine.selectRecipe(-1);
            return true;
        }
        if (id >= 100 && id < 100 + ChemicalPlantRecipes.RECIPES.size()) {
            machine.selectRecipe(id - 100);
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.CHEMICAL_PLANT.get()); }

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
            if (!moveItemStackTo(source, ChemicalPlantBlockEntity.SLOT_BATTERY, ChemicalPlantBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(source, ChemicalPlantBlockEntity.SLOT_UPGRADE_1, ChemicalPlantBlockEntity.SLOT_UPGRADE_2 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isFilledFluidContainer(source)) {
            if (!moveFilledFluidContainer(source)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(Items.BUCKET) || source.getItem() instanceof ItemEmptyPortableFluidContainer) {
            if (!moveEmptyFluidContainer(source)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveToMatchingRecipeSlot(source) && !moveItemStackTo(source, ChemicalPlantBlockEntity.INPUT_START, ChemicalPlantBlockEntity.INPUT_START + ChemicalPlantBlockEntity.INPUT_COUNT, false)) {
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

    private boolean moveToMatchingRecipeSlot(ItemStack source) {
        ChemicalPlantRecipes.Recipe recipe = getRecipe();
        if (recipe == null) {
            return false;
        }
        for (int i = 0; i < recipe.ingredients().size() && i < ChemicalPlantBlockEntity.INPUT_COUNT; i++) {
            if (source.is(recipe.ingredients().get(i).item()) && moveItemStackTo(source, ChemicalPlantBlockEntity.INPUT_START + i, ChemicalPlantBlockEntity.INPUT_START + i + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean moveFilledFluidContainer(ItemStack source) {
        ChemicalPlantRecipes.Recipe recipe = getRecipe();
        if (recipe == null) {
            return false;
        }
        NTMFluidType type = source.is(Items.WATER_BUCKET) ? NTMFluidType.WATER : source.is(Items.LAVA_BUCKET) ? NTMFluidType.LAVA : ItemPortableFluidContainer.getFluidType(source);
        if (type == null) {
            return false;
        }
        for (int i = 0; i < recipe.inputFluids().size() && i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            if (recipe.inputFluids().get(i).type() == type && moveItemStackTo(source, ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_CONTAINER_START + i, ChemicalPlantBlockEntity.SLOT_FLUID_INPUT_CONTAINER_START + i + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean moveEmptyFluidContainer(ItemStack source) {
        ChemicalPlantRecipes.Recipe recipe = getRecipe();
        if (recipe == null) {
            return false;
        }
        for (int i = 0; i < recipe.outputFluids().size() && i < ChemicalPlantBlockEntity.FLUID_SLOT_COUNT; i++) {
            NTMFluidType type = recipe.outputFluids().get(i).type();
            if (source.is(Items.BUCKET) && type != NTMFluidType.WATER && type != NTMFluidType.LAVA) {
                continue;
            }
            if (moveItemStackTo(source, ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_CONTAINER_START + i, ChemicalPlantBlockEntity.SLOT_FLUID_OUTPUT_CONTAINER_START + i + 1, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFilledFluidContainer(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET) || stack.getItem() instanceof ItemPortableFluidContainer;
    }

    private static final class BatterySlot extends SlotItemHandler {
        private BatterySlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return ItemBatteryPack.isBattery(stack); }
    }
    private static final class FluidInputContainerSlot extends SlotItemHandler {
        private FluidInputContainerSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET) || stack.getItem() instanceof ItemPortableFluidContainer; }
    }
    private static final class FluidOutputContainerSlot extends SlotItemHandler {
        private FluidOutputContainerSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.BUCKET) || stack.getItem() instanceof ItemEmptyPortableFluidContainer; }
    }
    private static final class UpgradeSlot extends SlotItemHandler {
        private UpgradeSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ItemMachineUpgrade; }
        @Override public int getMaxStackSize() { return 1; }
    }
    private static final class OutputSlot extends SlotItemHandler {
        private OutputSlot(ItemStackHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
