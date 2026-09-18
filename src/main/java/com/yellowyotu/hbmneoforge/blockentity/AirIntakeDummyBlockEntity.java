package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AirIntakeDummyBlockEntity extends BlockEntity implements FluidNode {
    private BlockPos controller;

    public AirIntakeDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_INTAKE_DUMMY.get(), pos, state);
    }

    public void setController(BlockPos controller) {
        this.controller = controller == null ? null : controller.immutable();
        setChanged();
    }

    public BlockPos getController() {
        return controller;
    }

    public AirIntakeBlockEntity getCore() {
        return level != null && controller != null && level.getBlockEntity(controller) instanceof AirIntakeBlockEntity intake ? intake : null;
    }

    @Override
    public NTMFluidType getFluidType() {
        AirIntakeBlockEntity core = getCore();
        return core == null ? NTMFluidType.AIR : core.getFluidType();
    }

    @Override
    public int getFluidAmount() {
        AirIntakeBlockEntity core = getCore();
        return core == null ? 0 : core.getFluidAmount();
    }

    @Override
    public int getFluidCapacity() {
        AirIntakeBlockEntity core = getCore();
        return core == null ? AirIntakeBlockEntity.AIR_CAPACITY : core.getFluidCapacity();
    }

    @Override
    public int fill(NTMFluidType type, int amount) {
        return 0;
    }

    @Override
    public int drain(NTMFluidType type, int amount) {
        AirIntakeBlockEntity core = getCore();
        return core == null ? 0 : core.drain(type, amount);
    }

    @Override
    public boolean accepts(NTMFluidType type) {
        return type == NTMFluidType.AIR;
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
