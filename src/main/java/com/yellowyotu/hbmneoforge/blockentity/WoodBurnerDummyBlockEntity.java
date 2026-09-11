package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WoodBurnerDummyBlockEntity extends BlockEntity {
    private BlockPos controller;
    public WoodBurnerDummyBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WOOD_BURNER_DUMMY.get(), pos, state); }
    public BlockPos getController() { return controller; }
    public void setController(BlockPos controller) { this.controller = controller.immutable(); setChanged(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.saveAdditional(tag, provider); if (controller != null) { tag.putLong("Controller", controller.asLong()); } }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) { super.loadAdditional(tag, provider); controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null; }
}
