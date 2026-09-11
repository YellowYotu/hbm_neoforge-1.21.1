package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class FoundryCastingBlockEntity extends FoundryBaseBlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && FoundryMaterialRegistry.isMold(stack, basin);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : 64;
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncInventory();
        }
    };
    private boolean basin;
    private int cooloff = 200;

    public FoundryCastingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_CASTING.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryCastingBlockEntity casting) {
        if (casting.amount > casting.getCapacity()) {
            casting.amount = casting.getCapacity();
        }
        if (casting.amount == 0) {
            casting.material = "";
        }

        FoundryMaterialRegistry.MoldResult mold = casting.getMoldResult();
        if (mold != null && casting.amount == casting.getCapacity() && casting.inventory.getStackInSlot(1).isEmpty()) {
            casting.cooloff--;
            if (casting.cooloff <= 0) {
                casting.amount = 0;
                casting.material = "";
                casting.inventory.setStackInSlot(1, mold.output().copy());
                casting.cooloff = 200;
            }
        } else {
            casting.cooloff = 200;
        }
        casting.syncIfChanged();
    }


    private void syncInventory() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public void setBasin(boolean basin) {
        this.basin = basin;
    }

    public boolean isBasin() {
        return basin;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public FoundryMaterialRegistry.MoldResult getMoldResult() {
        return FoundryMaterialRegistry.getMoldResult(inventory.getStackInSlot(0), material);
    }

    @Override
    public int getCapacity() {
        return FoundryMaterialRegistry.getMoldCost(inventory.getStackInSlot(0), basin);
    }

    @Override
    public boolean canReceiveFrom(Direction side, String incomingMaterial) {
        // Casting blocks are filled by a downward stream through an outlet, not directly from a channel.
        return false;
    }

    public boolean canReceivePour(Direction side, String incomingMaterial) {
        if (side != Direction.UP || !inventory.getStackInSlot(1).isEmpty()) {
            return false;
        }
        int capacity = getCapacity();
        if (capacity <= 0 || amount >= capacity) {
            return false;
        }
        FoundryMaterialRegistry.MoldResult mold =
                FoundryMaterialRegistry.getMoldResult(inventory.getStackInSlot(0), incomingMaterial);
        if (mold == null || mold.cost() != capacity) {
            return false;
        }
        return standardCheck(side, incomingMaterial, 1);
    }

    public int insertPour(String incomingMaterial, int incomingAmount) {
        return canReceivePour(Direction.UP, incomingMaterial) ? standardAdd(incomingMaterial, incomingAmount) : 0;
    }

    public int getCooloff() {
        return cooloff;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putBoolean("Basin", basin);
        tag.putInt("Cooloff", cooloff);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        basin = tag.getBoolean("Basin");
        cooloff = tag.contains("Cooloff") ? tag.getInt("Cooloff") : 200;
    }
}
