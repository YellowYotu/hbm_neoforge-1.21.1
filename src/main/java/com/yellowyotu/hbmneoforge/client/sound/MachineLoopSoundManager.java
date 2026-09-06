package com.yellowyotu.hbmneoforge.client.sound;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineLoopSoundManager {
    private static final Map<BlockEntity, MachineLoopSoundInstance> SOUNDS = new WeakHashMap<>();

    public static void update(BlockEntity source, boolean active, SoundEvent sound, float volume, BooleanSupplier activeSupplier) {
        MachineLoopSoundInstance current = SOUNDS.get(source);
        if (!active) {
            if (current != null && !current.hasStopped()) {
                Minecraft.getInstance().getSoundManager().stop(current);
                SOUNDS.remove(source);
            }
            return;
        }
        if (current == null || current.hasStopped()) {
            MachineLoopSoundInstance created = new MachineLoopSoundInstance(source, sound, volume, activeSupplier);
            SOUNDS.put(source, created);
            Minecraft.getInstance().getSoundManager().play(created);
        }
    }

    private MachineLoopSoundManager() {
    }
}
