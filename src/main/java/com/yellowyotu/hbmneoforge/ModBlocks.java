package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.block.AssemblyMachineBlock;
import com.yellowyotu.hbmneoforge.block.AssemblyMachineDummyBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.block.CageLampBlock;
import com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock;
import com.yellowyotu.hbmneoforge.block.ChemicalPlantDummyBlock;
import com.yellowyotu.hbmneoforge.block.FalloutBlock;
import com.yellowyotu.hbmneoforge.block.FoundryOutletBlock;
import com.yellowyotu.hbmneoforge.block.FireboxBlock;
import com.yellowyotu.hbmneoforge.block.HeatingOvenBlock;
import com.yellowyotu.hbmneoforge.block.HeaterDummyBlock;
import com.yellowyotu.hbmneoforge.block.CrucibleBlock;
import com.yellowyotu.hbmneoforge.block.CrucibleDummyBlock;
import com.yellowyotu.hbmneoforge.block.FoundryChannelBlock;
import com.yellowyotu.hbmneoforge.block.FoundryCastingBlock;
import com.yellowyotu.hbmneoforge.block.FoundryTankBlock;
import com.yellowyotu.hbmneoforge.block.FluidPipeBlock;
import com.yellowyotu.hbmneoforge.block.FluidValveBlock;
import com.yellowyotu.hbmneoforge.block.FluidStorageBlock;
import com.yellowyotu.hbmneoforge.block.FluidStorageDummyBlock;
import com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock;
import com.yellowyotu.hbmneoforge.block.FatManBlock;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.block.GeigerBlock;
import com.yellowyotu.hbmneoforge.block.HBMAnvilBlock;
import com.yellowyotu.hbmneoforge.block.MachinePressBlock;
import com.yellowyotu.hbmneoforge.block.BlastFurnaceBlock;
import com.yellowyotu.hbmneoforge.block.MachinePressDummyBlock;
import com.yellowyotu.hbmneoforge.block.LeavesLayerBlock;
import com.yellowyotu.hbmneoforge.block.RadiationAbsorberBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingPaneBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingStairBlock;
import com.yellowyotu.hbmneoforge.block.RadiationShieldingSlabBlock;
import com.yellowyotu.hbmneoforge.block.WasteEarthBlock;
import com.yellowyotu.hbmneoforge.block.WasteLeavesBlock;
import com.yellowyotu.hbmneoforge.block.WoodBurnerBlock;
import com.yellowyotu.hbmneoforge.block.WoodBurnerDummyBlock;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import com.yellowyotu.hbmneoforge.block.RadioactiveBarrelBlock;
import com.yellowyotu.hbmneoforge.block.RedCableBlock;
import com.yellowyotu.hbmneoforge.block.SlidingSealDoorBlock;
import com.yellowyotu.hbmneoforge.block.QeContainmentDoorBlock;
import com.yellowyotu.hbmneoforge.block.StorageCrateBlock;
import com.yellowyotu.hbmneoforge.block.ShredderBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationDummyBlock;
import com.yellowyotu.hbmneoforge.block.SteelScaffoldBlock;
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

    public static final DeferredBlock<FluidPipeBlock> FLUID_DUCT_NEO = fluidPipe("fluid_duct_neo", null, FluidPipeBlock.Style.NEO);
    public static final DeferredBlock<FluidPipeBlock> FLUID_DUCT_BOX = fluidPipe("fluid_duct_box", null, FluidPipeBlock.Style.BOX);
    public static final DeferredBlock<FluidPipeBlock> FLUID_DUCT_PAINTABLE = fluidPipe("fluid_duct_paintable", null, FluidPipeBlock.Style.PAINTABLE);
    public static final DeferredBlock<FluidPipeBlock> FLUID_DUCT_GAUGE = fluidPipe("fluid_duct_gauge", null, FluidPipeBlock.Style.GAUGE);
    public static final DeferredBlock<FluidValveBlock> FLUID_VALVE = BLOCKS.register("fluid_valve", () -> new FluidValveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<FluidPipeBlock> FLUID_PUMP = fluidPipe("fluid_pump", null, FluidPipeBlock.Style.PUMP);
    public static final DeferredBlock<FluidStorageBlock> BARREL_PLASTIC = fluidStorage("barrel_plastic", 12_000, 2.0F, 5.0F, FluidStorageBlock.StorageKind.PLASTIC_BARREL);
    public static final DeferredBlock<FluidStorageBlock> BARREL_CORRODED = fluidStorage("barrel_corroded", 6_000, 2.0F, 5.0F, FluidStorageBlock.StorageKind.CORRODED_BARREL);
    public static final DeferredBlock<FluidStorageBlock> BARREL_STEEL = fluidStorage("barrel_steel", 16_000, 2.0F, 5.0F, FluidStorageBlock.StorageKind.STEEL_BARREL);
    public static final DeferredBlock<FluidStorageBlock> BARREL_ANTIMATTER = fluidStorage("barrel_antimatter", 16_000, 2.0F, 5.0F, FluidStorageBlock.StorageKind.ANTIMATTER_BARREL);
    public static final DeferredBlock<FluidStorageBlock> MACHINE_FLUID_TANK = fluidStorage("machine_fluidtank", 256_000, 5.0F, 20.0F, FluidStorageBlock.StorageKind.TANK);
    public static final DeferredBlock<FluidStorageDummyBlock> FLUID_TANK_DUMMY = BLOCKS.register("fluid_tank_dummy", () -> new FluidStorageDummyBlock(BlockBehaviour.Properties.of().strength(5.0F, 20.0F).noOcclusion().noLootTable()));
    public static final DeferredBlock<SteelScaffoldBlock> STEEL_SCAFFOLD = BLOCKS.register("steel_scaffold", () -> new SteelScaffoldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 15.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<BlastFurnaceBlock> BLAST_FURNACE = BLOCKS.register("machine_blast_furnace", () -> new BlastFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<WoodBurnerBlock> WOOD_BURNER = BLOCKS.register("machine_wood_burner", () -> new WoodBurnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredBlock<WoodBurnerDummyBlock> WOOD_BURNER_DUMMY = BLOCKS.register("machine_wood_burner_dummy", () -> new WoodBurnerDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable().requiresCorrectToolForDrops()));
    public static final DeferredBlock<ShredderBlock> SHREDDER = BLOCKS.register("machine_shredder", () -> new ShredderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<SolderingStationBlock> SOLDERING_STATION = BLOCKS.register("machine_soldering_station", () -> new SolderingStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<SolderingStationDummyBlock> SOLDERING_STATION_DUMMY = BLOCKS.register("machine_soldering_station_dummy", () -> new SolderingStationDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<ObsidianGravelBlock> GRAVEL_OBSIDIAN = BLOCKS.register("gravel_obsidian", () -> new ObsidianGravelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK) .strength(5.0F, 300.0F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops()));

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


    public static final DeferredBlock<SlidingSealDoorBlock> SLIDING_SEAL_DOOR = BLOCKS.register("sliding_seal_door", () -> new SlidingSealDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL) .strength(10.0F, 1000.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<SlidingSealDoorBlock> SLIDING_GATE_DOOR = BLOCKS.register("sliding_gate_door", () -> new SlidingSealDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(100.0F, 1000.0F).sound(SoundType.METAL).noOcclusion(), true));
    public static final DeferredBlock<QeContainmentDoorBlock> QE_CONTAINMENT = BLOCKS.register("qe_containment", () -> new QeContainmentDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(100.0F, 1000.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<StorageCrateBlock> CRATE_IRON = BLOCKS.register("crate_iron", () -> new StorageCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL), 4));
    public static final DeferredBlock<StorageCrateBlock> CRATE_STEEL = BLOCKS.register("crate_steel", () -> new StorageCrateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 20.0F).sound(SoundType.METAL), 6));
    public static final DeferredBlock<FatManBlock> NUKE_MAN = BLOCKS.register("nuke_man", () -> new FatManBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6000.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<AssemblyMachineBlock> ASSEMBLY_MACHINE = BLOCKS.register("assembly_machine", () -> new AssemblyMachineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<ChemicalPlantBlock> CHEMICAL_PLANT = BLOCKS.register("machine_chemical_plant", () -> new ChemicalPlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<ChemicalPlantDummyBlock> CHEMICAL_PLANT_DUMMY = BLOCKS.register("machine_chemical_plant_dummy", () -> new ChemicalPlantDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<AssemblyMachineDummyBlock> ASSEMBLY_MACHINE_DUMMY = BLOCKS.register("assembly_machine_dummy", () -> new AssemblyMachineDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 30.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<Block> BLOCK_RED_COPPER = metalBlock("block_red_copper", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<RedCableBlock> RED_CABLE = BLOCKS.register("red_cable", () -> new RedCableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion()));
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
    public static final DeferredBlock<Block> ORE_SULFUR = oreBlock("ore_sulfur", MapColor.COLOR_YELLOW);
    public static final DeferredBlock<Block> ORE_THORIUM = oreBlock("ore_thorium", MapColor.COLOR_GRAY);
    public static final DeferredBlock<Block> ORE_NITER = oreBlock("ore_niter", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<Block> ORE_FLUORITE = oreBlock("ore_fluorite", MapColor.COLOR_LIGHT_GREEN);
    public static final DeferredBlock<Block> ORE_LIGNITE = oreBlock("ore_lignite", MapColor.COLOR_BROWN);
    public static final DeferredBlock<Block> ORE_ASBESTOS = oreBlock("ore_asbestos", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<Block> ORE_CINNEBAR = oreBlock("ore_cinnebar", MapColor.COLOR_RED);
    public static final DeferredBlock<Block> STONE_LIMESTONE = BLOCKS.register("stone_limestone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> BLOCK_COBALT = metalBlock("block_cobalt", MapColor.COLOR_BLUE);
    public static final DeferredBlock<Block> BLOCK_LEAD = shieldingMetalBlock("block_lead", MapColor.COLOR_PURPLE);

    public static final DeferredBlock<RadioactiveBarrelBlock> YELLOW_BARREL =
            BLOCKS.register(
                    "yellow_barrel",
                    () -> new RadioactiveBarrelBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_YELLOW)
                                    .strength(0.5F, 2.5F)
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
    public static final DeferredBlock<WasteEarthBlock> WASTE_EARTH = BLOCKS.register("waste_earth", () -> new WasteEarthBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS) .strength(0.5F, 1.0F).sound(SoundType.GRASS).randomTicks()));
    public static final DeferredBlock<WasteLeavesBlock> WASTE_LEAVES = BLOCKS.register("waste_leaves", () -> new WasteLeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT) .strength(0.3F, 0.3F).sound(SoundType.GRASS).randomTicks().noOcclusion().ignitedByLava().noLootTable()));
    public static final DeferredBlock<LeavesLayerBlock> LEAVES_LAYER = BLOCKS.register("leaves_layer", () -> new LeavesLayerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.1F).sound(SoundType.GRASS).replaceable().noCollission().noOcclusion().ignitedByLava().noLootTable()));


    public static final DeferredBlock<com.yellowyotu.hbmneoforge.block.HempCropBlock> HEMP = BLOCKS.register("hemp", () -> new com.yellowyotu.hbmneoforge.block.HempCropBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)));

    public static final DeferredBlock<GeigerBlock> GEIGER =
            BLOCKS.register(
                    "geiger",
                    () -> new GeigerBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(15.0F, 0.25F)
                                    .sound(SoundType.METAL)
                                    .noOcclusion()
                    )
            );


    public static final DeferredBlock<HBMAnvilBlock> ANVIL_IRON = BLOCKS.register("anvil_iron", () -> new HBMAnvilBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 100.0F).sound(SoundType.ANVIL).noOcclusion(), 1));
    public static final DeferredBlock<HBMAnvilBlock> ANVIL_STEEL = BLOCKS.register("anvil_steel", () -> new HBMAnvilBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(7.5F, 150.0F).sound(SoundType.ANVIL).noOcclusion(), 2));

    public static final DeferredBlock<Block> FOUNDRY_MOLD = BLOCKS.register("foundry_mold", () -> new FoundryCastingBlock(foundryProperties(), false));
    public static final DeferredBlock<Block> FOUNDRY_BASIN = BLOCKS.register("foundry_basin", () -> new FoundryCastingBlock(foundryProperties(), true));
    public static final DeferredBlock<Block> FOUNDRY_CHANNEL = BLOCKS.register("foundry_channel", () -> new FoundryChannelBlock(foundryProperties()));
    public static final DeferredBlock<Block> FOUNDRY_OUTLET = BLOCKS.register("foundry_outlet", () -> new FoundryOutletBlock(foundryProperties()));
    public static final DeferredBlock<Block> FOUNDRY_SLAGTAP = BLOCKS.register("foundry_slagtap", () -> new FoundryOutletBlock(foundryProperties()));
    public static final DeferredBlock<Block> FOUNDRY_TANK = BLOCKS.register("foundry_tank", () -> new FoundryTankBlock(foundryProperties()));
    public static final DeferredBlock<FireboxBlock> FIREBOX = BLOCKS.register("heater_firebox", () -> new FireboxBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().lightLevel(state -> state.getValue(com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock.LIT) ? 8 : 0)));
    public static final DeferredBlock<HeatingOvenBlock> HEATING_OVEN = BLOCKS.register("heater_oven", () -> new HeatingOvenBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 10.0F).sound(SoundType.STONE).noOcclusion().lightLevel(state -> state.getValue(com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock.LIT) ? 8 : 0)));
    public static final DeferredBlock<HeaterDummyBlock> HEATER_DUMMY = BLOCKS.register("heater_dummy", () -> new HeaterDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion().noLootTable()));
    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register("machine_crucible", () -> new CrucibleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 10.0F).sound(SoundType.STONE).noOcclusion()));
    public static final DeferredBlock<CrucibleDummyBlock> CRUCIBLE_DUMMY = BLOCKS.register("machine_crucible_dummy", () -> new CrucibleDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 10.0F).sound(SoundType.STONE).noOcclusion().noLootTable()));


    public static final DeferredBlock<CageLampBlock> CAGE_LAMP = BLOCKS.register("spotlight_incandescent", () -> new CageLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F, 0.0F).sound(SoundType.METAL).lightLevel(state -> 15).noCollission().noOcclusion()));
    public static final DeferredBlock<RadiationShieldingBlock> REINFORCED_GLASS = BLOCKS.register("reinforced_glass", () -> new RadiationShieldingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3F, 25.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<RadiationShieldingPaneBlock> REINFORCED_GLASS_PANE = BLOCKS.register("reinforced_glass_pane", () -> new RadiationShieldingPaneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(2.0F, 25.0F).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<Block> CONCRETE_COLORED_EXT_HAZARD = concreteBlock("concrete_colored_ext_hazard");

    public static final DeferredBlock<Block> CONCRETE = concreteBlock("concrete");
    public static final DeferredBlock<Block> CONCRETE_SMOOTH = concreteBlock("concrete_smooth");
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
    public static final DeferredBlock<Block> BRICK_CONCRETE = shieldingConcreteBlock("brick_concrete");
    public static final DeferredBlock<Block> BRICK_CONCRETE_CRACKED = concreteBlock("brick_concrete_cracked");
    public static final DeferredBlock<Block> BRICK_CONCRETE_MOSSY = shieldingConcreteBlock("brick_concrete_mossy");
    public static final DeferredBlock<Block> BRICK_CONCRETE_BROKEN = concreteBlock("brick_concrete_broken");
    public static final DeferredBlock<Block> BRICK_CONCRETE_MARKED = concreteBlock("brick_concrete_marked");

    public static final DeferredBlock<Block> ASPHALT = BLOCKS.register("asphalt", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(15.0F, 120.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> ASPHALT_LIGHT = BLOCKS.register("asphalt_light", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(15.0F, 120.0F).sound(SoundType.STONE).lightLevel(state -> 15)));
    public static final DeferredBlock<Block> BASALT_BRICK = BLOCKS.register("basalt_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> BASALT_TILES = BLOCKS.register("basalt_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> BRICK_ASBESTOS = BLOCKS.register("brick_asbestos", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(15.0F, 40.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> BRICK_COMPOUND = BLOCKS.register("brick_compound", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(15.0F, 400.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> BRICK_LIGHT = BLOCKS.register("brick_light", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(15.0F, 20.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> BRICK_OBSIDIAN = BLOCKS.register("brick_obsidian", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(15.0F, 120.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DUCRETE_BRICK = BLOCKS.register("ducrete_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(15.0F, 750.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DEPTH_BRICK = BLOCKS.register("depth_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DEPTH_TILES = BLOCKS.register("depth_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> DEPTH_NETHER_BRICK = BLOCKS.register("depth_nether_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.NETHER_BRICKS)));
    public static final DeferredBlock<Block> DEPTH_NETHER_TILES = BLOCKS.register("depth_nether_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.NETHER_BRICKS)));
    public static final DeferredBlock<Block> GNEISS_BRICK = BLOCKS.register("gneiss_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F, 10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> GNEISS_TILE = BLOCKS.register("gneiss_tile", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5F, 10.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> REINFORCED_BRICK = BLOCKS.register("reinforced_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(15.0F, 300.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> TILE_LAB = BLOCKS.register("tile_lab", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 20.0F).sound(SoundType.GLASS)));
    public static final DeferredBlock<Block> TILE_LAB_BROKEN = BLOCKS.register("tile_lab_broken", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 20.0F).sound(SoundType.GLASS)));
    public static final DeferredBlock<Block> TILE_LAB_CRACKED = BLOCKS.register("tile_lab_cracked", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 20.0F).sound(SoundType.GLASS)));
    public static final DeferredBlock<Block> METEOR_BRICK = BLOCKS.register("meteor_brick", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(15.0F, 360.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK_CHISELED = BLOCKS.register("meteor_brick_chiseled", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(15.0F, 360.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK_CRACKED = BLOCKS.register("meteor_brick_cracked", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(15.0F, 360.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> METEOR_BRICK_MOSSY = BLOCKS.register("meteor_brick_mossy", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(15.0F, 360.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> LIGHTSTONE_TILE = BLOCKS.register("lightstone_tile", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(20.0F, 20.0F).sound(SoundType.STONE).lightLevel(state -> 15)));
    public static final DeferredBlock<Block> LIGHTSTONE_BRICKS = BLOCKS.register("lightstone_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(20.0F, 20.0F).sound(SoundType.STONE).lightLevel(state -> 15)));
    public static final DeferredBlock<Block> LIGHTSTONE_BRICKS_CHISELED = BLOCKS.register("lightstone_bricks_chiseled", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(20.0F, 20.0F).sound(SoundType.STONE).lightLevel(state -> 15)));
    public static final DeferredBlock<Block> VINYL_TILE_SMALL = BLOCKS.register("vinyl_tile_small", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(10.0F, 60.0F).sound(SoundType.GLASS)));
    public static final DeferredBlock<Block> VINYL_TILE_LARGE = BLOCKS.register("vinyl_tile_large", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(10.0F, 60.0F).sound(SoundType.GLASS)));

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


    private static BlockBehaviour.Properties foundryProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion();
    }

    private static DeferredBlock<Block> foundryBlock(String name) {
        return BLOCKS.register(name, () -> new Block(foundryProperties()));
    }

    private static DeferredBlock<StairBlock> concreteStairs(String name, DeferredBlock<? extends Block> base) {
        return BLOCKS.register(name, () -> new RadiationShieldingStairBlock(base.get().defaultBlockState(), concreteProperties(name)));
    }

    private static DeferredBlock<SlabBlock> concreteSlab(String name) {
        return BLOCKS.register(name, () -> new RadiationShieldingSlabBlock(concreteProperties(name)));
    }

    private static DeferredBlock<Block> oreBlock(String name, MapColor color) {
        float resistance = name.equals("ore_beryllium") || name.equals("ore_lignite") || name.equals("ore_asbestos") ? 15.0F : 10.0F;
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(BlockBehaviour.Properties.of().mapColor(color).strength(5.0F, resistance).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> concreteBlock(String name) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(concreteProperties(name)));
    }

    private static DeferredBlock<Block> shieldingConcreteBlock(String name) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(concreteProperties(name)));
    }

    private static BlockBehaviour.Properties concreteProperties(String name) {
        float hardness = 15.0F;
        float resistance = 140.0F;

        if (name.contains("concrete_super")) {
            hardness = 150.0F;
            resistance = 1000.0F;
        } else if (name.contains("concrete_rebar")) {
            hardness = 50.0F;
            resistance = 240.0F;
        } else if (name.contains("concrete_asbestos")) {
            resistance = 1500.0F;
        } else if (name.contains("brick_concrete_cracked")) {
            resistance = 60.0F;
        } else if (name.contains("brick_concrete_broken")) {
            resistance = 45.0F;
        } else if (name.contains("brick_concrete")) {
            resistance = 160.0F;
        }

        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness, resistance).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static DeferredBlock<Block> shieldingMetalBlock(String name, MapColor color) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(metalProperties(name, color)));
    }

    private static DeferredBlock<Block> metalBlock(String name, MapColor color) {
        return BLOCKS.register(name, () -> new RadiationShieldingBlock(metalProperties(name, color)));
    }

    private static BlockBehaviour.Properties metalProperties(String name, MapColor color) {
        float resistance = 10.0F;
        return BlockBehaviour.Properties.of().mapColor(color).strength(5.0F, resistance).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }


    private static DeferredBlock<FluidPipeBlock> fluidPipe(String name, NTMFluidType filter) { return fluidPipe(name, filter, FluidPipeBlock.Style.NEO); }

    private static DeferredBlock<FluidPipeBlock> fluidPipe(String name, NTMFluidType filter, FluidPipeBlock.Style style) {
        return BLOCKS.register(name, () -> new FluidPipeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL).noOcclusion(), filter, style));
    }

    private static DeferredBlock<FluidStorageBlock> fluidStorage(String name, int capacity, float hardness, float resistance) { return fluidStorage(name, capacity, hardness, resistance, FluidStorageBlock.StorageKind.STEEL_BARREL); }

    private static DeferredBlock<FluidStorageBlock> fluidStorage(String name, int capacity, float hardness, float resistance, FluidStorageBlock.StorageKind kind) {
        return BLOCKS.register(name, () -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(hardness, resistance).sound(SoundType.METAL).noOcclusion();
            if (kind == FluidStorageBlock.StorageKind.TANK) { return new FluidTankMultiblockBlock(properties, capacity, kind, 5, 3, 3); }
            return new FluidStorageBlock(properties, capacity, kind);
        });
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
