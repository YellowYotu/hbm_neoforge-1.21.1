package com.yellowyotu.hbmneoforge.radiation;

public final class RadiationValues {

    public static final float URANIUM_INGOT = 0.35F;
    public static final float URANIUM_233_INGOT = 5.0F;
    public static final float URANIUM_235_INGOT = 1.0F;
    public static final float URANIUM_238_INGOT = 0.25F;
    public static final float PLUTONIUM_INGOT = 7.5F;
    public static final float PLUTONIUM_239_INGOT = 5.0F;
    public static final float PLUTONIUM_240_INGOT = 7.5F;
    public static final float PLUTONIUM_241_INGOT = 25.0F;
    public static final float GADGET_CORE = 5.0F;
    public static final float FALLOUT_ITEM = 30.0F;
    public static final float FALLOUT_BLOCK_ITEM = 10.5F;
    public static final float FALLOUT_BLOCK_SOURCE = FALLOUT_BLOCK_ITEM * 0.1F;
    public static final float YELLOW_BARREL_ITEM = 150.0F;
    public static final float YELLOW_BARREL_SOURCE = 5.0F;
    public static final float PELLET_RTG = 15.0F;
    public static final int RADAWAY_DURATION_TICKS = 140;
    public static final float RADAWAY_REMOVAL_PER_TICK = 1.0F;
    public static final int RAD_X_DURATION_TICKS = 3 * 60 * 20;
    public static final float RAD_X_RESISTANCE = 0.2F;
    public static final int BASIC_ABSORBER_TICK_INTERVAL = 10;
    public static final float BASIC_ABSORBER_AMOUNT_PER_TICK = 2.5F;

    private RadiationValues() {
    }
}
