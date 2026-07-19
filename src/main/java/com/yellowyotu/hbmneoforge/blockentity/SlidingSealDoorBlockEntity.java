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

    public SlidingSealDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_SEAL_DOOR.get(), pos, state);
        progress = state.getValue(SlidingSealDoorBlock.PROGRESS);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlidingSealDoorBlockEntity door) {
        if (level.isClientSide()) {
            return;
        }
        int target = state.getValue(SlidingSealDoorBlock.OPEN) ? SlidingSealDoorBlock.MAX_PROGRESS : 0;
        int next = door.progress;
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
    }
}
