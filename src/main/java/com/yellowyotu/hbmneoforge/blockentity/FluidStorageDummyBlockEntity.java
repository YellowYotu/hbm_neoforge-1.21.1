package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FluidStorageDummyBlockEntity extends BlockEntity implements FluidNode {
    private BlockPos controller;
    public FluidStorageDummyBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.FLUID_STORAGE_DUMMY.get(), pos, state); }
    public void setController(BlockPos controller) { this.controller = controller.immutable(); setChanged(); }
    public BlockPos getController() { return controller; }
    private FluidStorageBlockEntity core() { return level != null && controller != null && level.getBlockEntity(controller) instanceof FluidStorageBlockEntity storage ? storage : null; }
    public FluidStorageBlockEntity getCoreStorage() { return core(); }
    public void setFluidType(NTMFluidType type) { FluidStorageBlockEntity core = core(); if (core != null) { core.setFluidType(type); } }
    @Override public NTMFluidType getFluidType() { FluidStorageBlockEntity core = core(); return core == null ? null : core.getFluidType(); }
    @Override public int getFluidAmount() { FluidStorageBlockEntity core = core(); return core == null ? 0 : core.getFluidAmount(); }
    @Override public int getFluidCapacity() { FluidStorageBlockEntity core = core(); return core == null ? 0 : core.getFluidCapacity(); }
    @Override public int fill(NTMFluidType type, int amount) { FluidStorageBlockEntity core = core(); return core == null ? 0 : core.fill(type, amount); }
    @Override public int drain(NTMFluidType type, int amount) { FluidStorageBlockEntity core = core(); return core == null ? 0 : core.drain(type, amount); }
    @Override public boolean accepts(NTMFluidType type) { FluidStorageBlockEntity core = core(); return core != null && core.accepts(type); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); if (controller != null) { tag.putLong("controller", controller.asLong()); } }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); controller = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null; }
}
