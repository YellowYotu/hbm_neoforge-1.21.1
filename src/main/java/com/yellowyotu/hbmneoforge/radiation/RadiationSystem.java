package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.ModAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class RadiationSystem {

    public static final float MAX_RADIATION = 2_500_000.0F;

    private RadiationSystem() {
    }

    public static float getRadiation(LivingEntity entity) {
        if (!RadiationConfig.ENABLE_CONTAMINATION.get()) {
            return 0.0F;
        }
        return getData(entity).radiation();
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        if (!RadiationConfig.ENABLE_CONTAMINATION.get()) {
            return;
        }
        PlayerRadiationData data = getData(entity);
        setData(entity, new PlayerRadiationData(sanitizeRadiation(radiation), data.environmentRadiation(), data.environmentRadiationBuffer()));
    }

    public static void addRadiation(LivingEntity entity, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return;
        }
        setRadiation(entity, getRadiation(entity) + amount);
    }

    public static void removeRadiation(LivingEntity entity, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return;
        }
        setRadiation(entity, getRadiation(entity) - amount);
    }

    public static void clearRadiation(LivingEntity entity) {
        PlayerRadiationData data = getData(entity);
        setData(entity, new PlayerRadiationData(0.0F, data.environmentRadiation(), data.environmentRadiationBuffer()));
    }

    public static float getEnvironmentRadiation(LivingEntity entity) {
        return getData(entity).environmentRadiation();
    }

    public static float getEnvironmentRadiationBuffer(LivingEntity entity) {
        return getData(entity).environmentRadiationBuffer();
    }

    public static boolean contaminate(LivingEntity entity, RadiationContaminationType contaminationType, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0F) {
            return false;
        }

        addEnvironmentRadiation(entity, amount);

        if (entity instanceof Player player) {
            if (player.isSpectator()) {
                return false;
            }
            if (player.getAbilities().instabuild && contaminationType.isPreventedByCreativeMode()) {
                return false;
            }
            if (player.tickCount < 200) {
                return false;
            }
        }
        if (!RadiationConfig.ENABLE_CONTAMINATION.get() || MobRadiationHandler.isRadiationImmune(entity)) {
            return false;
        }

        float modifier = contaminationType.bypassesResistance() ? 1.0F : entity instanceof Player player ? RadiationResistance.calculateRadiationModifier(player) : 1.0F;
        addRadiation(entity, amount * modifier);
        return true;
    }

    public static void rollEnvironmentRadiationBuffer(LivingEntity entity) {
        PlayerRadiationData data = getData(entity);
        setData(entity, new PlayerRadiationData(data.radiation(), 0.0F, data.environmentRadiation()));
    }

    public static void migrateLegacyDose(Player player) {
        float legacyRadiation = player.getData(ModAttachments.LEGACY_RADIATION);
        if (!Float.isFinite(legacyRadiation) || legacyRadiation <= 0.0F) {
            return;
        }

        PlayerRadiationData data = getData(player);
        float migratedRadiation = Math.max(data.radiation(), sanitizeRadiation(legacyRadiation));
        setData(player, new PlayerRadiationData(migratedRadiation, data.environmentRadiation(), data.environmentRadiationBuffer()));
        player.setData(ModAttachments.LEGACY_RADIATION, 0.0F);
    }

    public static void clearAll(LivingEntity entity) {
        setData(entity, PlayerRadiationData.empty());
        if (entity instanceof Player player) {
            player.setData(ModAttachments.LEGACY_RADIATION, 0.0F);
        }
    }

    private static void addEnvironmentRadiation(LivingEntity entity, float amount) {
        PlayerRadiationData data = getData(entity);
        float environmentRadiation = Math.max(data.environmentRadiation() + amount, 0.0F);
        setData(entity, new PlayerRadiationData(data.radiation(), environmentRadiation, data.environmentRadiationBuffer()));
    }

    private static PlayerRadiationData getData(LivingEntity entity) {
        return entity.getData(ModAttachments.RADIATION_DATA);
    }

    private static void setData(LivingEntity entity, PlayerRadiationData data) {
        entity.setData(ModAttachments.RADIATION_DATA, data);
    }

    private static float sanitizeRadiation(float radiation) {
        if (!Float.isFinite(radiation)) {
            return 0.0F;
        }
        return Mth.clamp(radiation, 0.0F, MAX_RADIATION);
    }
}
