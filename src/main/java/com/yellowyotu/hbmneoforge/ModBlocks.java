package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.block.AssemblyMachineBlock;
import com.yellowyotu.hbmneoforge.block.AssemblyMachineDummyBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.block.CageLampBlock;
import com.yellowyotu.hbmneoforge.block.FalloutBlock;
import com.yellowyotu.hbmneoforge.block.GeigerBlock;
import com.yellowyotu.hbmneoforge.block.HBMAnvilBlock;
import com.yellowyotu.hbmneoforge.block.MachinePressBlock;
import com.yellowyotu.hbmneoforge.block.MachinePressDummyBlock;
import com.yellowyotu.hbmneoforge.block.LeavesLayerBlock;
import com.yellowyotu.hbmneoforge.block.RadiationAbsorberBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingPaneBlock;
import com.yellowyotu.hbmneoforge.block.WasteEarthBlock;
import com.yellowyotu.hbmneoforge.block.WasteLeavesBlock;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import com.yellowyotu.hbmneoforge.block.RadioactiveBarrelBlock;
import com.yellowyotu.hbmneoforge.block.RedCableBlock;
import com.yellowyotu.hbmneoforge.block.SlidingSealDoorBlock;
import com.yellowyotu.hbmneoforge.block.ShredderBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationDummyBlock;
import com.yellowyotu.hbmneoforge.block.ObsidianGravelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID
            );

    public static final DeferredBlock<ShredderBlock> SHREDDER = BLOCKS.register("machine_shredder", () -> new ShredderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<SolderingStationBlock> SOLDERING_STATION = BLOCKS.register("machine_soldering_station", () -> new SolderingStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<SolderingStationDummyBlock> SOLDERING_STATION_DUMMY = BLOCKS.register("machine_soldering_station_dummy", () -> new SolderingStationDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<ObsidianGravelBlock> GRAVEL_OBSIDIAN = BLOCKS.register("gravel_obsidian", () -> new ObsidianGravelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 300.0F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops()));

    public static final DeferredBlock<MachinePressBlock> MACHINE_PRESS =
            BLOCKS.register(
                    "machine_press",
                    () -> new MachinePressBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 10.0F)
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );

    public static final DeferredBlock<MachinePressDummyBlock> MACHINE_PRESS_DUMMY =
            BLOCKS.register(
                    "machine_press_dummy",
                    () -> new MachinePressDummyBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(5.0F, 10.0F)
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );

    public static final DeferredBlock<Block> MACHINE_PRESS_HEAD_RENDER =
            BLOCKS.register(
                    "machine_press_head_render",
                    () -> new Block(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(-1.0F, 3_600_000.0F)
                                    .sound(SoundType.METAL)
                                    .noCollission()
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );


    public static final DeferredBlock<SlidingSealDoorBlock> SLIDING_SEAL_DOOR = BLOCKS.register("sliding_seal_door", () -> new SlidingSealDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(10.0F, 1000.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<AssemblyMachineBlock> ASSEMBLY_MACHINE = BLOCKS.register("assembly_machine", () -> new AssemblyMachineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<AssemblyMachineDummyBlock> ASSEMBLY_MACHINE_DUMMY = BLOCKS.register("assembly_machine_dummy", () -> new AssemblyMachineDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> BLOCK_RED_COPPER = metalBlock("block_red_copper", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<RedCableBlock> RED_CABLE = BLOCKS.register("red_cable", () -> new RedCableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F, 2.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<BatterySocketBlock> MACHINE_BATTERY_SOCKET = BLOCKS.register("machine_battery_socket", () -> new BatterySocketBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<BatterySocketDummyBlock> MACHINE_BATTERY_SOCKET_DUMMY = BLOCKS.register("machine_battery_socket_dummy", () -> new BatterySocketDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));

    public static final DeferredBlock<Block> BLOCK_BORON =
            shieldingMetalBlock("block_boron", MapColor.COLOR_LIGHT_GREEN);

    public static final DeferredBlock<Block> PWR_CONTROLLER =
            metalBlock("pwr_controller", MapColor.METAL);

    public static final DeferredBlock<Block> ORE_URANIUM = oreBlock("ore_uranium", MapColor.COLOR_GREEN);
    public static final DeferredBlock<Block> ORE_TITANIUM = oreBlock("ore_titanium", MapColor.METAL);
    public static final DeferredBlock<Block> ORE_TUNGSTEN = oreBlock("ore_tungsten", MapColor.COLOR_GRAY);
    public static final DeferredBlock<Block> ORE_ALUMINIUM = oreBlock("ore_aluminium", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<Block> ORE_BERYLLIUM = oreBlock("ore_beryllium", MapColor.COLOR_LIGHT_GREEN);
    public static final DeferredBlock<Block> ORE_LEAD = oreBlock("ore_lead", MapColor.COLOR_PURPLE);
    public static final DeferredBlock<Block> ORE_COBALT = oreBlock("ore_cobalt", MapColor.COLOR_BLUE);
    public static final DeferredBlock<Block> ORE_RARE_EARTH = oreBlock("ore_rare_earth", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> BLOCK_COBALT = metalBlock("block_cobalt", MapColor.COLOR_BLUE);
    public static final DeferredBlock<Block> BLOCK_LEAD = shieldingMetalBlock("block_lead", MapColor.COLOR_PURPLE);

    public static final DeferredBlock<RadioactiveBarrelBlock> YELLOW_BARREL =
            BLOCKS.register(
                    "yellow_barrel",
                    () -> new RadioactiveBarrelBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_YELLOW)
                                    .strength(4.0F, 10.0F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion(),
                            RadiationValues.YELLOW_BARREL_SOURCE
                    )
            );

    public static final DeferredBlock<FalloutBlock> BLOCK_FALLOUT =
            BLOCKS.register(
                    "block_fallout",
                    () -> new FalloutBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .strength(0.2F)
                                    .sound(SoundType.GRAVEL)
                    )
            );

    public static final DeferredBlock<RadiationAbsorberBlock> RAD_ABSORBER = BLOCKS.register("rad_absorber", () -> new RadiationAbsorberBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().randomTicks()));
    public static final DeferredBlock<WasteEarthBlock> WASTE_EARTH = BLOCKS.register("waste_earth", () -> new WasteEarthBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<WasteLeavesBlock> WASTE_LEAVES = BLOCKS.register("waste_leaves", () -> new WasteLeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.1F).sound(SoundType.GRASS).randomTicks().noOcclusion().ignitedByLava().noLootTable()));
    public static final DeferredBlock<LeavesLayerBlock> LEAVES_LAYER = BLOCKS.register("leaves_layer", () -> new LeavesLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.1F).sound(SoundType.GRASS).replaceable().noCollission().noOcclusion().ignitedByLava().noLootTable()));

    public static final DeferredBlock<GeigerBlock> GEIGER =
            BLOCKS.register(
                    "geiger",
                    () -> new GeigerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(2.5F, 6.0F)
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );


    public static final DeferredBlock<HBMAnvilBlock> ANVIL_IRON = BLOCKS.register("anvil_iron", () -> new HBMAnvilBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 1200.0F).sound(SoundType.ANVIL).noOcclusion()));


    public static final DeferredBlock<CageLampBlock> CAGE_LAMP = BLOCKS.register("spotlight_incandescent", () -> new CageLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).lightLevel(state -> 15).noOcclusion()));
    public static final DeferredBlock<RadiationShieldingBlock> REINFORCED_GLASS = BLOCKS.register("reinforced_glass", () -> new RadiationShieldingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(2.0F, 25.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<RadiationShieldingPaneBlock> REINFORCED_GLASS_PANE = BLOCKS.register("reinforced_glass_pane", () -> new RadiationShieldingPaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(2.0F, 25.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<Block> CONCRETE_COLORED_EXT_HAZARD = concreteBlock("concrete_colored_ext_hazard");

    public static final DeferredBlock<Block> CONCRETE = concreteBlock("concrete");
    public static final DeferredBlock<Block> CONCRETE_WHITE = concreteBlock("concrete_white");
    public static final DeferredBlock<Block> CONCRETE_ORANGE = concreteBlock("concrete_orange");
    public static final DeferredBlock<Block> CONCRETE_MAGENTA = concreteBlock("concrete_magenta");
    public static final DeferredBlock<Block> CONCRETE_LIGHT_BLUE = concreteBlock("concrete_light_blue");
    public static final DeferredBlock<Block> CONCRETE_YELLOW = concreteBlock("concrete_yellow");
    public static final DeferredBlock<Block> CONCRETE_LIME = concreteBlock("concrete_lime");
    public static final DeferredBlock<Block> CONCRETE_PINK = concreteBlock("concrete_pink");
    public static final DeferredBlock<Block> CONCRETE_GRAY = concreteBlock("concrete_gray");
    public static final DeferredBlock<Block> CONCRETE_SILVER = concreteBlock("concrete_silver");
    public static final DeferredBlock<Block> CONCRETE_CYAN = concreteBlock("concrete_cyan");
    public static final DeferredBlock<Block> CONCRETE_PURPLE = concreteBlock("concrete_purple");
    public static final DeferredBlock<Block> CONCRETE_BLUE = concreteBlock("concrete_blue");
    public static final DeferredBlock<Block> CONCRETE_BROWN = concreteBlock("concrete_brown");
    public static final DeferredBlock<Block> CONCRETE_GREEN = concreteBlock("concrete_green");
    public static final DeferredBlock<Block> CONCRETE_RED = concreteBlock("concrete_red");
    public static final DeferredBlock<Block> CONCRETE_BLACK = concreteBlock("concrete_black");
    public static final DeferredBlock<Block> CONCRETE_REBAR = concreteBlock("concrete_rebar");
    public static final DeferredBlock<Block> CONCRETE_ASBESTOS = concreteBlock("concrete_asbestos");
    public static final DeferredBlock<Block> CONCRETE_SUPER = concreteBlock("concrete_super");
    public static final DeferredBlock<Block> CONCRETE_TILE = concreteBlock("concrete_tile");
    public static final DeferredBlock<Block> BRICK_CONCRETE = shieldingConcreteBlock("brick_concrete");
    public static final DeferredBlock<Block> BRICK_CONCRETE_CRACKED = concreteBlock("brick_concrete_cracked");
    public static final DeferredBlock<Block> BRICK_CONCRETE_MOSSY = shieldingConcreteBlock("brick_concrete_mossy");
    public static final DeferredBlock<Block> BRICK_CONCRETE_BROKEN = concreteBlock("brick_concrete_broken");
    public static final DeferredBlock<Block> BRICK_CONCRETE_MARKED = concreteBlock("brick_concrete_marked");

    public static final DeferredBlock<StairBlock> CONCRETE_STAIRS = concreteStairs("concrete_stairs", CONCRETE);
    public static final DeferredBlock<SlabBlock> CONCRETE_SLAB = concreteSlab("concrete_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_WHITE_STAIRS = concreteStairs("concrete_white_stairs", CONCRETE_WHITE);
    public static final DeferredBlock<SlabBlock> CONCRETE_WHITE_SLAB = concreteSlab("concrete_white_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_ORANGE_STAIRS = concreteStairs("concrete_orange_stairs", CONCRETE_ORANGE);
    public static final DeferredBlock<SlabBlock> CONCRETE_ORANGE_SLAB = concreteSlab("concrete_orange_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_MAGENTA_STAIRS = concreteStairs("concrete_magenta_stairs", CONCRETE_MAGENTA);
    public static final DeferredBlock<SlabBlock> CONCRETE_MAGENTA_SLAB = concreteSlab("concrete_magenta_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_LIGHT_BLUE_STAIRS = concreteStairs("concrete_light_blue_stairs", CONCRETE_LIGHT_BLUE);
    public static final DeferredBlock<SlabBlock> CONCRETE_LIGHT_BLUE_SLAB = concreteSlab("concrete_light_blue_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_YELLOW_STAIRS = concreteStairs("concrete_yellow_stairs", CONCRETE_YELLOW);
    public static final DeferredBlock<SlabBlock> CONCRETE_YELLOW_SLAB = concreteSlab("concrete_yellow_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_LIME_STAIRS = concreteStairs("concrete_lime_stairs", CONCRETE_LIME);
    public static final DeferredBlock<SlabBlock> CONCRETE_LIME_SLAB = concreteSlab("concrete_lime_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_PINK_STAIRS = concreteStairs("concrete_pink_stairs", CONCRETE_PINK);
    public static final DeferredBlock<SlabBlock> CONCRETE_PINK_SLAB = concreteSlab("concrete_pink_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_GRAY_STAIRS = concreteStairs("concrete_gray_stairs", CONCRETE_GRAY);
    public static final DeferredBlock<SlabBlock> CONCRETE_GRAY_SLAB = concreteSlab("concrete_gray_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_SILVER_STAIRS = concreteStairs("concrete_silver_stairs", CONCRETE_SILVER);
    public static final DeferredBlock<SlabBlock> CONCRETE_SILVER_SLAB = concreteSlab("concrete_silver_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_CYAN_STAIRS = concreteStairs("concrete_cyan_stairs", CONCRETE_CYAN);
    public static final DeferredBlock<SlabBlock> CONCRETE_CYAN_SLAB = concreteSlab("concrete_cyan_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_PURPLE_STAIRS = concreteStairs("concrete_purple_stairs", CONCRETE_PURPLE);
    public static final DeferredBlock<SlabBlock> CONCRETE_PURPLE_SLAB = concreteSlab("concrete_purple_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_BLUE_STAIRS = concreteStairs("concrete_blue_stairs", CONCRETE_BLUE);
    public static final DeferredBlock<SlabBlock> CONCRETE_BLUE_SLAB = concreteSlab("concrete_blue_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_BROWN_STAIRS = concreteStairs("concrete_brown_stairs", CONCRETE_BROWN);
    public static final DeferredBlock<SlabBlock> CONCRETE_BROWN_SLAB = concreteSlab("concrete_brown_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_GREEN_STAIRS = concreteStairs("concrete_green_stairs", CONCRETE_GREEN);
    public static final DeferredBlock<SlabBlock> CONCRETE_GREEN_SLAB = concreteSlab("concrete_green_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_RED_STAIRS = concreteStairs("concrete_red_stairs", CONCRETE_RED);
    public static final DeferredBlock<SlabBlock> CONCRETE_RED_SLAB = concreteSlab("concrete_red_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_BLACK_STAIRS = concreteStairs("concrete_black_stairs", CONCRETE_BLACK);
    public static final DeferredBlock<SlabBlock> CONCRETE_BLACK_SLAB = concreteSlab("concrete_black_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_REBAR_STAIRS = concreteStairs("concrete_rebar_stairs", CONCRETE_REBAR);
    public static final DeferredBlock<SlabBlock> CONCRETE_REBAR_SLAB = concreteSlab("concrete_rebar_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_ASBESTOS_STAIRS = concreteStairs("concrete_asbestos_stairs", CONCRETE_ASBESTOS);
    public static final DeferredBlock<SlabBlock> CONCRETE_ASBESTOS_SLAB = concreteSlab("concrete_asbestos_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_SUPER_STAIRS = concreteStairs("concrete_super_stairs", CONCRETE_SUPER);
    public static final DeferredBlock<SlabBlock> CONCRETE_SUPER_SLAB = concreteSlab("concrete_super_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_TILE_STAIRS = concreteStairs("concrete_tile_stairs", CONCRETE_TILE);
    public static final DeferredBlock<SlabBlock> CONCRETE_TILE_SLAB = concreteSlab("concrete_tile_slab");
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_STAIRS = concreteStairs("brick_concrete_stairs", BRICK_CONCRETE);
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_SLAB = concreteSlab("brick_concrete_slab");
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_CRACKED_STAIRS = concreteStairs("brick_concrete_cracked_stairs", BRICK_CONCRETE_CRACKED);
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_CRACKED_SLAB = concreteSlab("brick_concrete_cracked_slab");
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_MOSSY_STAIRS = concreteStairs("brick_concrete_mossy_stairs", BRICK_CONCRETE_MOSSY);
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_MOSSY_SLAB = concreteSlab("brick_concrete_mossy_slab");
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_BROKEN_STAIRS = concreteStairs("brick_concrete_broken_stairs", BRICK_CONCRETE_BROKEN);
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_BROKEN_SLAB = concreteSlab("brick_concrete_broken_slab");
    public static final DeferredBlock<StairBlock> BRICK_CONCRETE_MARKED_STAIRS = concreteStairs("brick_concrete_marked_stairs", BRICK_CONCRETE_MARKED);
    public static final DeferredBlock<SlabBlock> BRICK_CONCRETE_MARKED_SLAB = concreteSlab("brick_concrete_marked_slab");
    public static final DeferredBlock<StairBlock> CONCRETE_COLORED_EXT_HAZARD_STAIRS = concreteStairs("concrete_colored_ext_hazard_stairs", CONCRETE_COLORED_EXT_HAZARD);
    public static final DeferredBlock<SlabBlock> CONCRETE_COLORED_EXT_HAZARD_SLAB = concreteSlab("concrete_colored_ext_hazard_slab");


    private static DeferredBlock<StairBlock> concreteStairs(String name, DeferredBlock<? extends Block> base) {
        return BLOCKS.register(name, () -> new StairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 12.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<SlabBlock> concreteSlab(String name) {
        return BLOCKS.register(name, () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 12.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> oreBlock(String name, MapColor color) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of().mapColor(color).strength(3.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> concreteBlock(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(4.0F, 12.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> shieldingConcreteBlock(String name) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 12.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> shieldingMetalBlock(String name, MapColor color) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(BlockBehaviour.Properties.of().mapColor(color).strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> metalBlock(
            String name,
            MapColor color
    ) {
        return BLOCKS.register(
                name,
                () -> new Block(
                        BlockBehaviour.Properties.of()
                                .mapColor(color)
                                .strength(5.0F, 10.0F)
                                .sound(SoundType.METAL)
                                .requiresCorrectToolForDrops()
                )
        );
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
