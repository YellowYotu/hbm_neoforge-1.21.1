package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.BlastFurnaceBlock;
import com.yellowyotu.hbmneoforge.menu.BlastFurnaceMenu;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BlastFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT_UPPER = 0;
    public static final int SLOT_INPUT_LOWER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int MAX_FUEL = 12_800;
    public static final int PROCESSING_TIME = 400;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (slot == SLOT_FUEL) {
                return getItemPower(stack) > 0;
            }
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };

    private int progress;
    private int fuel;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> fuel;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
            if (index == 1) {
                fuel = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity furnace) {
        furnace.consumeFuelItem();
        BlastFurnaceRecipes.Recipe recipe = BlastFurnaceRecipes.find(furnace.inventory.getStackInSlot(SLOT_INPUT_UPPER), furnace.inventory.getStackInSlot(SLOT_INPUT_LOWER));
        boolean canProcess = recipe != null && furnace.fuel > 0 && furnace.canOutput(recipe.output());

        if (canProcess) {
            furnace.fuel--;
            furnace.progress++;
            if (furnace.progress >= PROCESSING_TIME) {
                furnace.progress -= PROCESSING_TIME;
                furnace.process(recipe);
            }
        } else {
            furnace.progress = 0;
        }

        boolean lit = furnace.progress > 0;
        if (state.getValue(BlastFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(BlastFurnaceBlock.LIT, lit), Block.UPDATE_ALL);
        }
        furnace.setChangedAndSync();
    }

    private void consumeFuelItem() {
        ItemStack stack = inventory.getStackInSlot(SLOT_FUEL);
        if (stack.isEmpty()) {
            return;
        }
        int power = getItemPower(stack);
        if (power <= 0 || fuel > MAX_FUEL - power) {
            return;
        }
        ItemStack remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (stack.isEmpty() && !remainder.isEmpty()) {
            inventory.setStackInSlot(SLOT_FUEL, remainder.copy());
        } else {
            inventory.setStackInSlot(SLOT_FUEL, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
        fuel += power;
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void process(BlastFurnaceRecipes.Recipe recipe) {
        ItemStack upper = inventory.getStackInSlot(SLOT_INPUT_UPPER);
        ItemStack lower = inventory.getStackInSlot(SLOT_INPUT_LOWER);
        upper.shrink(1);
        lower.shrink(1);
        inventory.setStackInSlot(SLOT_INPUT_UPPER, upper.isEmpty() ? ItemStack.EMPTY : upper);
        inventory.setStackInSlot(SLOT_INPUT_LOWER, lower.isEmpty() ? ItemStack.EMPTY : lower);

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, recipe.output().copy());
        } else {
            output.grow(recipe.output().getCount());
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
    }

    public static int getItemPower(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 200;
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return 2_000;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return 12_800;
        }
        if (stack.is(Items.BLAZE_ROD)) {
            return 1_000;
        }
        if (stack.is(Items.BLAZE_POWDER)) {
            return 300;
        }
        if (stack.is(ModItems.LIGNITE.get()) || stack.is(ModItems.POWDER_LIGNITE.get())) {
            return 150;
        }
        if (stack.is(ModItems.POWDER_COAL.get())) {
            return 200;
        }
        if (stack.is(ModItems.BRIQUETTE_COAL.get()) || stack.is(ModItems.BRIQUETTE_LIGNITE.get()) || stack.is(ModItems.BRIQUETTE_WOOD.get())) {
            return 200;
        }
        if (stack.is(ModItems.COKE_COAL.get()) || stack.is(ModItems.COKE_LIGNITE.get()) || stack.is(ModItems.COKE_PETROLEUM.get()) || stack.is(ModItems.SOLID_FUEL.get())) {
            return 400;
        }
        return 0;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack);
            }
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Fuel", fuel);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        fuel = tag.getInt("Fuel");
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.blast_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BlastFurnaceMenu(containerId, playerInventory, this, data);
    }
}
