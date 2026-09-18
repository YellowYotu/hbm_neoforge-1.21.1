package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MixerDummyBlockEntity extends BlockEntity {
    private BlockPos controller;

    public MixerDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXER_DUMMY.get(), pos, state);
    }

    public void setController(BlockPos controller) {
        this.controller = controller == null ? null : controller.immutable();
        setChanged();
    }

    public BlockPos getController() {
        return controller;
    }

    public MixerBlockEntity getCore() {
        return level != null && controller != null && level.getBlockEntity(controller) instanceof MixerBlockEntity mixer ? mixer : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controller != null) {
            tag.putLong("controller", controller.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = tag.contains("controller") ? BlockPos.of(tag.getLong("controller")) : null;
    }
}
