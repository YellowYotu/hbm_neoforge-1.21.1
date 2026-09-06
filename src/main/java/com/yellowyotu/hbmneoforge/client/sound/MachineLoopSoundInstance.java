package com.yellowyotu.hbmneoforge.client.sound;

import java.util.function.BooleanSupplier;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineLoopSoundInstance extends AbstractTickableSoundInstance {
    private final BlockEntity source;
    private final BooleanSupplier active;
    private boolean stopped;

    public MachineLoopSoundInstance(BlockEntity source, SoundEvent sound, float volume, BooleanSupplier active) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.source = source;
        this.active = active;
        this.volume = volume;
        this.pitch = 1.0F;
        this.looping = true;
        updatePosition();
    }

    @Override
    public void tick() {
        if (source.isRemoved() || source.getLevel() == null || !active.getAsBoolean()) {
            stopped = true;
            stop();
            return;
        }
        updatePosition();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public boolean hasStopped() {
        return stopped;
    }

    private void updatePosition() {
        x = source.getBlockPos().getX() + 0.5D;
        y = source.getBlockPos().getY() + 0.5D;
        z = source.getBlockPos().getZ() + 0.5D;
    }
}
