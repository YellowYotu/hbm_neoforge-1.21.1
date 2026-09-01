package com.yellowyotu.hbmneoforge.radiation;

public enum RadiationContaminationType {
    CREATIVE(true, false),
    RAD_BYPASS(true, true),
    NONE(false, false);

    private final boolean preventedByCreativeMode;
    private final boolean bypassesResistance;

    RadiationContaminationType(boolean preventedByCreativeMode, boolean bypassesResistance) {
        this.preventedByCreativeMode = preventedByCreativeMode;
        this.bypassesResistance = bypassesResistance;
    }

    public boolean isPreventedByCreativeMode() {
        return preventedByCreativeMode;
    }

    public boolean bypassesResistance() {
        return bypassesResistance;
    }
}
