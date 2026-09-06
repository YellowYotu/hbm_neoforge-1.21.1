package com.yellowyotu.hbmneoforge.effect;

import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class RadAwayEffect extends MobEffect {

    public RadAwayEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xBB4B00);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        RadiationSystem.removeRadiation(player, RadiationValues.RADAWAY_REMOVAL_PER_TICK * (amplifier + 1));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
