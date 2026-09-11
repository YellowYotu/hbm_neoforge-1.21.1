package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.QeContainmentDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class QeContainmentDoorBlockEntity extends BlockEntity {
    public static final int OPEN_TIME = 160;
    private int progress;
    private DoorAccessMode accessMode = DoorAccessMode.HAND_AND_REDSTONE;
    private boolean powered;

    public QeContainmentDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QE_CONTAINMENT.get(), pos, state);
        progress = state.getValue(QeContainmentDoorBlock.FRAME);
    }

    public DoorAccessMode getAccessMode() {
        return accessMode;
    }

    public DoorAccessMode cycleAccessMode() {
        accessMode = accessMode.next();
        setChanged();
        return accessMode;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isMoving() {
        return progress > 0 && progress < OPEN_TIME;
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QeContainmentDoorBlockEntity door) {
        if (level.isClientSide()) {
            return;
        }
        int target = state.getValue(QeContainmentDoorBlock.OPEN) ? OPEN_TIME : 0;
        if (door.progress == target) {
            return;
        }
        boolean starting = door.progress == 0 || door.progress == OPEN_TIME;
        if (starting) {
            level.playSound(null, pos, ModSounds.QE_CONTAINMENT_MOVE.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        door.progress += door.progress < target ? 1 : -1;
        door.setChanged();
        QeContainmentDoorBlock.updateFrame(level, pos, state, Math.max(0, Math.min(OPEN_TIME, door.progress)));
        if (door.progress == target) {
            QeContainmentDoorBlock.stopMoveSound(level, pos);
            level.playSound(null, pos, ModSounds.QE_CONTAINMENT_STOP.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
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
