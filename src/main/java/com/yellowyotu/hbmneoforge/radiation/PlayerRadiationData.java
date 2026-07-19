package com.yellowyotu.hbmneoforge.radiation;

import com.mojang.serialization.Codec;

public record PlayerRadiationData(float radiation, float environmentRadiation, float environmentRadiationBuffer) {

    public static final Codec<PlayerRadiationData> CODEC = Codec.FLOAT.optionalFieldOf("radiation", 0.0F).xmap(radiation -> new PlayerRadiationData(radiation, 0.0F, 0.0F), PlayerRadiationData::radiation).codec();

    public static PlayerRadiationData empty() {
        return new PlayerRadiationData(0.0F, 0.0F, 0.0F);
    }
}
