package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.menu.HBMAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class HBMAnvilBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_PRIMARY = 0;
    public static final int SLOT_SECONDARY = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot != SLOT_OUTPUT && level != null && !level.isClientSide()) {
                HBMAnvilRecipes.updateOutput(this, selectedRecipe);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    };
    private int selectedRecipe;

    public HBMAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HBM_ANVIL.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(int selectedRecipe) {
        this.selectedRecipe = Math.floorMod(selectedRecipe, HBMAnvilRecipes.size());
        HBMAnvilRecipes.updateOutput(inventory, this.selectedRecipe);
        setChanged();
    }

    public ItemStack craftSelected() {
        ItemStack result = HBMAnvilRecipes.craft(inventory, selectedRecipe);
        if (!result.isEmpty()) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return result;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("SelectedRecipe", selectedRecipe);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        selectedRecipe = Math.floorMod(tag.getInt("SelectedRecipe"), HBMAnvilRecipes.size());
        HBMAnvilRecipes.updateOutput(inventory, selectedRecipe);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.anvil");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new HBMAnvilMenu(containerId, playerInventory, this);
    }
}
