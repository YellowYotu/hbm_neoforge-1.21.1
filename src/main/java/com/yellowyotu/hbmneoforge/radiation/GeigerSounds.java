package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;

public final class GeigerSounds {

    private GeigerSounds() {
    }

    public static SoundEvent select(
            float radiationPerSecond,
            RandomSource random
    ) {
        List<SoundEvent> candidates = new ArrayList<>();

        if (radiationPerSecond < 1.0F) candidates.add(null);
        if (radiationPerSecond < 5.0F) candidates.add(null);
        if (radiationPerSecond < 10.0F) candidates.add(ModSounds.GEIGER_1.get());
        if (radiationPerSecond > 5.0F && radiationPerSecond < 15.0F) candidates.add(ModSounds.GEIGER_2.get());
        if (radiationPerSecond > 10.0F && radiationPerSecond < 20.0F) candidates.add(ModSounds.GEIGER_3.get());
        if (radiationPerSecond > 15.0F && radiationPerSecond < 25.0F) candidates.add(ModSounds.GEIGER_4.get());
        if (radiationPerSecond > 20.0F && radiationPerSecond < 30.0F) candidates.add(ModSounds.GEIGER_5.get());
        if (radiationPerSecond > 25.0F) candidates.add(ModSounds.GEIGER_6.get());

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(
                random.nextInt(candidates.size())
        );
    }
}
