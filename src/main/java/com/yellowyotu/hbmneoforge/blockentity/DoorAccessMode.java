package com.yellowyotu.hbmneoforge.blockentity;

import net.minecraft.network.chat.Component;

public enum DoorAccessMode {
    HAND_AND_REDSTONE("hand_and_redstone"),
    HAND_ONLY("hand_only"),
    REDSTONE_ONLY("redstone_only"),
    LOCKED("locked");

    private final String key;

    DoorAccessMode(String key) {
        this.key = key;
    }

    public DoorAccessMode next() {
        DoorAccessMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public boolean allowsHand() {
        return this == HAND_AND_REDSTONE || this == HAND_ONLY;
    }

    public boolean allowsRedstone() {
        return this == HAND_AND_REDSTONE || this == REDSTONE_ONLY;
    }

    public Component displayName() {
        return Component.translatable("door_mode.hbm_neoforge." + key);
    }
}
