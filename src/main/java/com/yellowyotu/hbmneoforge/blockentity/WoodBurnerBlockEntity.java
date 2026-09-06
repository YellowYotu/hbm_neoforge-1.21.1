package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.menu.WoodBurnerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class WoodBurnerBlockEntity extends BlockEntity implements MenuProvider, MachineEnergySource {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int MAX_POWER = 100_000;
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == SLOT_FUEL ? stack.getBurnTime(null) > 0 : slot == SLOT_BATTERY && ItemBatteryPack.isBattery(stack); }
        @Override protected void onContentsChanged(int slot) { setChangedAndSync(); }
    };
    private int burnTime;
    private int maxBurnTime;
    private int power;
    private boolean enabled;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) { case 0 -> burnTime; case 1 -> maxBurnTime; case 2 -> power; case 3 -> enabled ? 1 : 0; default -> 0; }; }
        @Override public void set(int index, int value) { if (index == 0) { burnTime = value; } else if (index == 1) { maxBurnTime = value; } else if (index == 2) { power = value; } else if (index == 3) { enabled = value != 0; } }
        @Override public int getCount() { return 4; }
    };

    public WoodBurnerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WOOD_BURNER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WoodBurnerBlockEntity burner) {
        if (burner.enabled && burner.burnTime <= 0 && burner.power <= MAX_POWER - 100) { burner.consumeFuel(); }
        if (burner.enabled && burner.burnTime > 0 && burner.power < MAX_POWER) { burner.burnTime--; burner.power = Math.min(MAX_POWER, burner.power + 100); }
        ItemStack battery = burner.inventory.getStackInSlot(SLOT_BATTERY);
        if (burner.power > 0 && ItemBatteryPack.isBattery(battery) && ItemBatteryPack.canReceiveEnergy(battery)) {
            int accepted = ItemBatteryPack.insertEnergy(battery, Math.min(100, burner.power));
            burner.power -= accepted;
            if (accepted > 0) { burner.inventory.setStackInSlot(SLOT_BATTERY, battery); }
        }
        burner.setChangedAndSync();
    }

    private void consumeFuel() {
        ItemStack stack = inventory.getStackInSlot(SLOT_FUEL);
        if (stack.isEmpty()) { return; }
        int time = stack.getBurnTime(null);
        if (time <= 0) { return; }
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        inventory.setStackInSlot(SLOT_FUEL, stack.isEmpty() ? (remainder.isEmpty() ? ItemStack.EMPTY : remainder.copy()) : stack);
        burnTime = time;
        maxBurnTime = time;
    }

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }
    public int getPower() { return power; }
    public boolean isEnabled() { return enabled; }
    public void toggleEnabled() { enabled = !enabled; setChangedAndSync(); }
    @Override public int getStoredEnergy() { return power; }
    @Override public int extractEnergyForMachine(int amount) { if (amount <= 0 || power <= 0) { return 0; } int extracted = Math.min(amount, power); power -= extracted; setChangedAndSync(); return extracted; }
    public void dropContents() { if (level == null) { return; } for (int i = 0; i < inventory.getSlots(); i++) { ItemStack stack = inventory.extractItem(i, inventory.getStackInSlot(i).getCount(), false); if (!stack.isEmpty()) { Block.popResource(level, worldPosition, stack); } } }
    private void setChangedAndSync() { setChanged(); if (level != null && !level.isClientSide()) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS); } }
    @Override public Component getDisplayName() { return Component.translatable("container.hbm_neoforge.machine_wood_burner"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new WoodBurnerMenu(id, inventory, this, data); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); tag.put("Inventory", inventory.serializeNBT(provider)); tag.putInt("BurnTime", burnTime); tag.putInt("MaxBurnTime", maxBurnTime); tag.putInt("Power", power); tag.putBoolean("Enabled", enabled); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); if (tag.contains("Inventory")) { inventory.deserializeNBT(provider, tag.getCompound("Inventory")); } burnTime = tag.getInt("BurnTime"); maxBurnTime = tag.getInt("MaxBurnTime"); power = tag.getInt("Power"); enabled = tag.contains("Enabled") && tag.getBoolean("Enabled"); }
}
