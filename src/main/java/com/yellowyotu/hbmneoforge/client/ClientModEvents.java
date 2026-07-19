package com.yellowyotu.hbmneoforge.client;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.ModParticles;
import com.yellowyotu.hbmneoforge.client.particle.DeadLeafParticle;
import com.yellowyotu.hbmneoforge.client.particle.SolderTauParticle;
import com.yellowyotu.hbmneoforge.client.renderer.AssemblyMachineBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.BatterySocketBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.MachinePressBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.SolderingStationBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.screen.AssemblyMachineScreen;
import com.yellowyotu.hbmneoforge.client.screen.BatterySocketScreen;
import com.yellowyotu.hbmneoforge.client.screen.HBMAnvilScreen;
import com.yellowyotu.hbmneoforge.client.screen.MachinePressScreen;
import com.yellowyotu.hbmneoforge.client.screen.ShredderScreen;
import com.yellowyotu.hbmneoforge.client.screen.SolderingStationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MACHINE_PRESS.get(), MachinePressScreen::new);
        event.register(ModMenus.SHREDDER.get(), ShredderScreen::new);
        event.register(ModMenus.SOLDERING_STATION.get(), SolderingStationScreen::new);
        event.register(ModMenus.HBM_ANVIL.get(), HBMAnvilScreen::new);
        event.register(ModMenus.ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new);
        event.register(ModMenus.BATTERY_SOCKET.get(), BatterySocketScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_PRESS.get(), MachinePressBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLDERING_STATION.get(), SolderingStationBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), AssemblyMachineBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BATTERY_SOCKET.get(), BatterySocketBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DEAD_LEAF.get(), DeadLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOLDER_TAU.get(), SolderTauParticle.Provider::new);
    }
}
