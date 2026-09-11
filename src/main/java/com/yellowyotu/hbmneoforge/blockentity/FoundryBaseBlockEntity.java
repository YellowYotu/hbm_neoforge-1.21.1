package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FoundryBaseBlockEntity extends BlockEntity {
    protected String material = "";
    protected int amount;
    private String lastMaterial = "";
    private int lastAmount = -1;

    protected FoundryBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int getCapacity();

    public boolean standardCheck(Direction side, String incomingMaterial, int incomingAmount) {
        if (incomingAmount <= 0 || incomingMaterial == null || incomingMaterial.isBlank()) {
            return false;
        }
        if (amount > 0 && !material.equals(incomingMaterial)) {
            return false;
        }
        return amount < getCapacity();
    }

    public int standardAdd(String incomingMaterial, int incomingAmount) {
        if (!standardCheck(Direction.UP, incomingMaterial, incomingAmount)) {
            return 0;
        }
        material = incomingMaterial;
        int accepted = Math.min(incomingAmount, getCapacity() - amount);
        amount += accepted;
        syncIfChanged();
        return accepted;
    }

    public boolean canReceiveFrom(Direction side, String incomingMaterial) {
        return standardCheck(side, incomingMaterial, 1);
    }

    public int insertMolten(String incomingMaterial, int incomingAmount) {
        return standardAdd(incomingMaterial, incomingAmount);
    }

    public int removeMolten(int requested) {
        int removed = Math.min(Math.max(requested, 0), amount);
        amount -= removed;
        if (amount <= 0) {
            amount = 0;
            material = "";
        }
        syncIfChanged();
        return removed;
    }

    public void setMolten(String material, int amount) {
        this.material = amount > 0 ? FoundryMaterialRegistry.normalize(material) : "";
        this.amount = Math.max(0, Math.min(amount, getCapacity()));
        syncIfChanged();
    }

    public String getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public int getMoltenColor() {
        return FoundryMaterialRegistry.color(material);
    }

    protected void syncIfChanged() {
        if (lastAmount == amount && lastMaterial.equals(material)) {
            return;
        }
        lastAmount = amount;
        lastMaterial = material;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Material", material);
        tag.putInt("Amount", amount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        material = tag.getString("Material");
        amount = tag.getInt("Amount");
        lastMaterial = material;
        lastAmount = amount;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), registries);
        }
    }
}
