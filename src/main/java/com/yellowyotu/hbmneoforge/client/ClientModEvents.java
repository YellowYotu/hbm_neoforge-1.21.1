package com.yellowyotu.hbmneoforge.client;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.ModParticles;
import com.yellowyotu.hbmneoforge.client.particle.DeadLeafParticle;
import com.yellowyotu.hbmneoforge.client.particle.SolderTauParticle;
import com.yellowyotu.hbmneoforge.client.model.DuctOverlayBakedModel;
import com.yellowyotu.hbmneoforge.client.model.PaintableDuctBakedModel;
import com.yellowyotu.hbmneoforge.client.renderer.AssemblyMachineBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.BatterySocketBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.MachinePressBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.renderer.SolderingStationBlockEntityRenderer;
import com.yellowyotu.hbmneoforge.client.screen.AssemblyMachineScreen;
import com.yellowyotu.hbmneoforge.client.screen.BatterySocketScreen;
import com.yellowyotu.hbmneoforge.client.screen.BlastFurnaceScreen;
import com.yellowyotu.hbmneoforge.client.screen.HBMAnvilScreen;
import com.yellowyotu.hbmneoforge.client.screen.FluidStorageScreen;
import com.yellowyotu.hbmneoforge.client.screen.FluidPipeScreen;
import com.yellowyotu.hbmneoforge.client.screen.FluidIdentifierScreen;
import com.yellowyotu.hbmneoforge.client.screen.MachinePressScreen;
import com.yellowyotu.hbmneoforge.client.screen.ShredderScreen;
import com.yellowyotu.hbmneoforge.client.screen.SolderingStationScreen;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    private static final ResourceLocation DUCT_OVERLAY_STITCH_MODEL = ResourceLocation.fromNamespaceAndPath(
        HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "block/fluid_duct_neo_overlay_stitch");
    private static final Material DUCT_OVERLAY = new Material(
        TextureAtlas.LOCATION_BLOCKS,
        ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "block/fluid_duct_mk2_overlay"));
    private static final ResourceLocation PAINTABLE_OVERLAY_STITCH_MODEL = ResourceLocation.fromNamespaceAndPath(
        HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "block/fluid_duct_paintable_overlay_stitch");


    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FLUID_IDENTIFIER.get(), FluidIdentifierScreen::new);
        event.register(ModMenus.FLUID_PIPE.get(), FluidPipeScreen::new);
        event.register(ModMenus.FLUID_STORAGE.get(), FluidStorageScreen::new);
        event.register(ModMenus.MACHINE_PRESS.get(), MachinePressScreen::new);
        event.register(ModMenus.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
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
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(DUCT_OVERLAY_STITCH_MODEL));
        event.register(ModelResourceLocation.standalone(PAINTABLE_OVERLAY_STITCH_MODEL));
    }

    @SubscribeEvent
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var overlaySprite = event.getTextureGetter().apply(DUCT_OVERLAY);
        BakedModel paintableOverlay = event.getModels().get(ModelResourceLocation.standalone(PAINTABLE_OVERLAY_STITCH_MODEL));
        event.getModels().replaceAll((location, model) -> {
            String id = location.toString();
            if (id.startsWith(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID + ":fluid_duct_neo#")) {
                return new DuctOverlayBakedModel(model, overlaySprite);
            }
            if (id.startsWith(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID + ":fluid_duct_paintable#") && paintableOverlay != null) {
                return new PaintableDuctBakedModel(model, paintableOverlay);
            }
            return model;
        });
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return 0xFFFFFFFF;
            }
            com.yellowyotu.hbmneoforge.fluid.NTMFluidType type = ItemFluidIdentifierMulti.getType(stack, true);
            return type == null ? 0xFFFFFFFF : 0xFF000000 | type.color();
        }, com.yellowyotu.hbmneoforge.ModItems.FLUID_IDENTIFIER.get());

        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return 0xFFFFFFFF;
            }
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            if (!tag.contains("filter")) {
                return 0xFFFFFFFF;
            }
            com.yellowyotu.hbmneoforge.fluid.NTMFluidType type = com.yellowyotu.hbmneoforge.fluid.NTMFluidType.byId(tag.getString("filter"));
            return type == null ? 0xFFFFFFFF : 0xFF000000 | type.color();
        }, com.yellowyotu.hbmneoforge.ModItems.FLUID_DUCT_NEO.get(), com.yellowyotu.hbmneoforge.ModItems.FLUID_DUCT_PAINTABLE.get());
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> {
            if (tintIndex != 1 || world == null || pos == null) {
                return 0xFFFFFFFF;
            }
            if (world.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe && pipe.getFilter() != null) {
                return 0xFF000000 | pipe.getFilter().color();
            }
            return 0xFFFFFFFF;
        },
            com.yellowyotu.hbmneoforge.ModBlocks.FLUID_DUCT_NEO.get(),
            com.yellowyotu.hbmneoforge.ModBlocks.FLUID_DUCT_PAINTABLE.get());
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DEAD_LEAF.get(), DeadLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOLDER_TAU.get(), SolderTauParticle.Provider::new);
    }
}
