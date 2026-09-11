package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.heat.HeatSource;
import com.yellowyotu.hbmneoforge.menu.HeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class AbstractHeaterBlockEntity extends BlockEntity implements MenuProvider, HeatSource {
    public static final int SLOT_FUEL_LEFT = 0;
    public static final int SLOT_FUEL_RIGHT = 1;
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getBurnTime(null) > 0; }
        @Override protected void onContentsChanged(int slot) { setChangedAndSync(); }
    };
    private int maxBurnTime;
    private int burnTime;
    private int burnHeat;
    private int heatEnergy;
    private boolean burning;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) { case 0 -> maxBurnTime; case 1 -> burnTime; case 2 -> burnHeat; case 3 -> heatEnergy; case 4 -> getMaxHeat(); case 5 -> burning ? 1 : 0; default -> 0; }; }
        @Override public void set(int index, int value) { if (index == 0) { maxBurnTime = value; } else if (index == 1) { burnTime = value; } else if (index == 2) { burnHeat = value; } else if (index == 3) { heatEnergy = value; } else if (index == 5) { burning = value != 0; } }
        @Override public int getCount() { return 6; }
    };

    protected AbstractHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractHeaterBlockEntity heater) {
        heater.tickHeatInput(level);
        heater.burning = false;
        if (heater.burnTime <= 0 && heater.heatEnergy < heater.getMaxHeat()) {
            heater.consumeFuel();
        }
        if (heater.burnTime > 0) {
            if (heater.heatEnergy < heater.getMaxHeat()) {
                heater.burnTime--;
            }
            heater.heatEnergy = Math.min(heater.getMaxHeat(), heater.heatEnergy + heater.burnHeat);
            heater.burning = true;
        } else {
            heater.burnHeat = 0;
            heater.heatEnergy = Math.max(0, heater.heatEnergy - Math.max(heater.heatEnergy / 1000, 1));
        }
        boolean lit = state.hasProperty(com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock.LIT) && state.getValue(com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock.LIT);
        if (lit != heater.burning) {
            level.setBlock(pos, state.setValue(com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock.LIT, heater.burning), Block.UPDATE_ALL);
        }
        heater.setChangedAndSync();
    }

    protected void tickHeatInput(Level level) {
    }

    private void consumeFuel() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            int baseTime = stack.getBurnTime(null);
            if (baseTime <= 0) {
                continue;
            }
            double timeMultiplier = getTimeMultiplier() * getFuelTimeMultiplier(stack);
            maxBurnTime = burnTime = Math.max(1, (int) Math.round(baseTime * timeMultiplier));
            burnHeat = Math.max(1, (int) Math.round(getBaseHeat() * getFuelHeatMultiplier(stack)));
            ItemStack remainder = stack.getCraftingRemainingItem();
            stack.shrink(1);
            inventory.setStackInSlot(i, stack.isEmpty() ? (remainder.isEmpty() ? ItemStack.EMPTY : remainder.copy()) : stack);
            return;
        }
    }

    private static double getFuelTimeMultiplier(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.equals("solid_fuel")) {
            return 1.5D;
        }
        if (id.contains("coke") || id.contains("lignite") || id.equals("coal") || id.equals("charcoal")) {
            return 1.25D;
        }
        return 1.0D;
    }

    private static double getFuelHeatMultiplier(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.equals("solid_fuel")) {
            return 3.0D;
        }
        if (id.contains("coke") || id.contains("lignite") || id.equals("coal") || id.equals("charcoal")) {
            return 2.0D;
        }
        return 1.0D;
    }

    protected abstract int getBaseHeat();
    protected abstract double getTimeMultiplier();
    public abstract int getMaxHeat();
    protected abstract Component getMachineName();

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }
    public int getBurnHeat() { return burnHeat; }
    public int getBurnTime() { return burnTime; }
    public int getMaxBurnTime() { return maxBurnTime; }
    public boolean isBurning() { return burning; }
    @Override public int getHeatStored() { return heatEnergy; }
    @Override public int extractHeat(int amount) { if (amount <= 0 || heatEnergy <= 0) { return 0; } int extracted = Math.min(amount, heatEnergy); heatEnergy -= extracted; setChangedAndSync(); return extracted; }
    public void addHeat(int amount) { if (amount <= 0) { return; } heatEnergy = Math.min(getMaxHeat(), heatEnergy + amount); setChangedAndSync(); }
    public void dropContents() { if (level == null) { return; } for (int i = 0; i < inventory.getSlots(); i++) { ItemStack stack = inventory.extractItem(i, inventory.getStackInSlot(i).getCount(), false); if (!stack.isEmpty()) { Block.popResource(level, worldPosition, stack); } } }
    private void setChangedAndSync() { setChanged(); if (level != null && !level.isClientSide()) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS); } }
    @Override public Component getDisplayName() { return getMachineName(); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new HeaterMenu(id, inventory, this, data); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); tag.put("Inventory", inventory.serializeNBT(provider)); tag.putInt("MaxBurnTime", maxBurnTime); tag.putInt("BurnTime", burnTime); tag.putInt("BurnHeat", burnHeat); tag.putInt("HeatEnergy", heatEnergy); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); if (tag.contains("Inventory")) { inventory.deserializeNBT(provider, tag.getCompound("Inventory")); } maxBurnTime = tag.getInt("MaxBurnTime"); burnTime = tag.getInt("BurnTime"); burnHeat = tag.getInt("BurnHeat"); heatEnergy = tag.getInt("HeatEnergy"); burning = burnTime > 0; }
}
