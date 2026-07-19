package com.yellowyotu.hbmneoforge.radiation;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RadiationConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_CONTAMINATION;
    public static final ModConfigSpec.BooleanValue ENABLE_CHUNK_RADIATION;
    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_RADIATION_EFFECTS;
    public static final ModConfigSpec.BooleanValue CLEANUP_DEAD_DIRT;
    public static final ModConfigSpec.BooleanValue ENABLE_POCKET_RADIATION;
    public static final ModConfigSpec.IntValue POCKET_TICK_RATE;
    public static final ModConfigSpec.DoubleValue POCKET_HALF_LIFE_SECONDS;
    public static final ModConfigSpec.DoubleValue POCKET_DIFFUSIVITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("radiation");
        ENABLE_CONTAMINATION = builder.comment("Toggles player contamination and the negative effects of radiation poisoning.").define("RADIATION_00_enableContamination", true);
        ENABLE_CHUNK_RADIATION = builder.comment("Toggles the world radiation system.").define("RADIATION_01_enableChunkRads", true);
        ENABLE_WORLD_RADIATION_EFFECTS = builder.comment("Whether high radiation levels should perform changes in the world.").define("RADWORLD_00_toggle", true);
        CLEANUP_DEAD_DIRT = builder.comment("Whether dead grass should decay into dirt.").define("RADWORLD_03_regrow", false);
        ENABLE_POCKET_RADIATION = builder.comment("Enables the Community Edition pocket-based radiation system instead of the simple chunk system. The key name is retained for compatibility with existing configs.").define("RADIATION_99_enablePRISM", true);
        POCKET_TICK_RATE = builder.comment("How many ticks pass between pocket radiation updates. Community Edition default: 1.").defineInRange("RADIATION_CE_01_radTickRate", 1, 1, 200);
        POCKET_HALF_LIFE_SECONDS = builder.comment("Half-life of pocket radiation in seconds. Community Edition default: 120.").defineInRange("RADIATION_CE_02_radHalfLifeSeconds", 120.0D, 0.001D, Double.MAX_VALUE);
        POCKET_DIFFUSIVITY = builder.comment("Diffusivity of pocket radiation. Community Edition default: 10.").defineInRange("RADIATION_CE_03_radDiffusivity", 10.0D, 0.001D, Double.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private RadiationConfig() {
    }
}
