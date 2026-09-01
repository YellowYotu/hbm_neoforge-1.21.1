package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.radiation.RadiationConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID)
public final class HBMsNuclearTechModUnofficialNeoForgeEdition {
    public static final String MODID = "hbm_neoforge";

    public HBMsNuclearTechModUnofficialNeoForgeEdition(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RadiationConfig.SPEC, "hbm_neoforge-radiation.toml");

        ModBlocks.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCapabilities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);
    }
}
