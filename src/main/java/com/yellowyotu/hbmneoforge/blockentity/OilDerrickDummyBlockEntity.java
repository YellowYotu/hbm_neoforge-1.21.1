package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class OilDerrickDummyBlockEntity extends BlockEntity {
    private BlockPos controller;

    public OilDerrickDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_DERRICK_DUMMY.get(), pos, state);
    }

    public void setController(BlockPos controller) {
        this.controller = controller.immutable();
        setChanged();
    }

    @Nullable
    public BlockPos getController() {
        return controller;
    }

    public boolean isFluidPort() {
        return getFluidPortDirection() != null;
    }

    @Nullable
    public Direction getFluidPortDirection() {
        if (controller == null) {
            return null;
        }

        int dx = worldPosition.getX() - controller.getX();
        int dy = worldPosition.getY() - controller.getY();
        int dz = worldPosition.getZ() - controller.getZ();

        if (dy != 0) {
            return null;
        }

        if (dx == 1 && dz == 0) {
            return Direction.EAST;
        }

        if (dx == -1 && dz == 0) {
            return Direction.WEST;
        }

        if (dx == 0 && dz == 1) {
            return Direction.SOUTH;
        }

        if (dx == 0 && dz == -1) {
            return Direction.NORTH;
        }

        return null;
    }

    public boolean canConnectFluidFrom(BlockPos pipePos, NTMFluidType type) {
        Direction portDirection = getFluidPortDirection();

        if (portDirection == null) {
            return false;
        }

        if (type != NTMFluidType.OIL && type != NTMFluidType.GAS) {
            return false;
        }

        return worldPosition.relative(portDirection).equals(pipePos);
    }

    @Nullable
    public OilDerrickBlockEntity getControllerBlockEntity() {
        if (controller == null || level == null) {
            return null;
        }

        if (level.getBlockEntity(controller) instanceof OilDerrickBlockEntity derrick) {
            return derrick;
        }

        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (controller != null) {
            tag.putLong("Controller", controller.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null;
    }
}
