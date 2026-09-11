package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.SlidingSealDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SlidingSealDoorBlockEntity extends BlockEntity {
    private int progress;
    private DoorAccessMode accessMode = DoorAccessMode.HAND_AND_REDSTONE;
    private boolean powered;

    public SlidingSealDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_SEAL_DOOR.get(), pos, state);
        progress = state.getValue(SlidingSealDoorBlock.PROGRESS);
    }

    public DoorAccessMode getAccessMode() {
        return accessMode;
    }

    public DoorAccessMode cycleAccessMode() {
        accessMode = accessMode.next();
        setChanged();
        return accessMode;
    }

    public boolean isMoving() {
        return progress > 0 && progress < SlidingSealDoorBlock.MAX_PROGRESS;
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlidingSealDoorBlockEntity door) {
        if (level.isClientSide()) {
            return;
        }
        int target = state.getValue(SlidingSealDoorBlock.OPEN) ? SlidingSealDoorBlock.MAX_PROGRESS : 0;
        int next = door.progress;
        boolean starting = next != target && (next == 0 || next == SlidingSealDoorBlock.MAX_PROGRESS);
        if (starting) {
            level.playSound(null, pos, ModSounds.SLIDING_SEAL_OPEN.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        if (next < target) {
            next++;
        } else if (next > target) {
            next--;
        }
        if (next == door.progress) {
            return;
        }
        door.progress = next;
        door.setChanged();
        SlidingSealDoorBlock.updateProgress(level, pos, state, next);
        if (next == target) {
            level.playSound(null, pos, ModSounds.SLIDING_SEAL_STOP.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.putString("DoorAccessMode", accessMode.name());
        tag.putBoolean("Powered", powered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        powered = tag.getBoolean("Powered");
        if (tag.contains("DoorAccessMode")) {
            try {
                accessMode = DoorAccessMode.valueOf(tag.getString("DoorAccessMode"));
            } catch (IllegalArgumentException ignored) {
                accessMode = DoorAccessMode.HAND_AND_REDSTONE;
            }
        }
    }
}
