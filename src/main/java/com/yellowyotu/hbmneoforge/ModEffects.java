package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.effect.RadAwayEffect;
import com.yellowyotu.hbmneoforge.effect.RadXEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HBMsNuclearTechModUnofficialNeoForgeEdition.MODID);
    public static final DeferredHolder<MobEffect, RadAwayEffect> RADAWAY = MOB_EFFECTS.register("radaway", RadAwayEffect::new);
    public static final DeferredHolder<MobEffect, RadXEffect> RAD_X = MOB_EFFECTS.register("radx", RadXEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
