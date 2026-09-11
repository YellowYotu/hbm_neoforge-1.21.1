package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.menu.StorageCrateMenu;

import com.yellowyotu.hbmneoforge.menu.AssemblyMachineMenu;
import com.yellowyotu.hbmneoforge.menu.BatterySocketMenu;
import com.yellowyotu.hbmneoforge.menu.BlastFurnaceMenu;
import com.yellowyotu.hbmneoforge.menu.ChemicalPlantMenu;
import com.yellowyotu.hbmneoforge.menu.HBMAnvilMenu;
import com.yellowyotu.hbmneoforge.menu.HeaterMenu;
import com.yellowyotu.hbmneoforge.menu.CrucibleMenu;
import com.yellowyotu.hbmneoforge.menu.FluidStorageMenu;
import com.yellowyotu.hbmneoforge.menu.FatManMenu;
import com.yellowyotu.hbmneoforge.menu.FluidPipeMenu;
import com.yellowyotu.hbmneoforge.menu.FluidIdentifierMenu;
import com.yellowyotu.hbmneoforge.menu.MachinePressMenu;
import com.yellowyotu.hbmneoforge.menu.ShredderMenu;
import com.yellowyotu.hbmneoforge.menu.SolderingStationMenu;
import com.yellowyotu.hbmneoforge.menu.WoodBurnerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(
                    Registries.MENU,
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID
            );

    public static final Supplier<MenuType<FluidIdentifierMenu>> FLUID_IDENTIFIER = MENUS.register("fluid_identifier", () -> IMenuTypeExtension.create(FluidIdentifierMenu::new));
    public static final Supplier<MenuType<FluidPipeMenu>> FLUID_PIPE = MENUS.register("fluid_pipe", () -> IMenuTypeExtension.create(FluidPipeMenu::new));
    public static final Supplier<MenuType<FluidStorageMenu>> FLUID_STORAGE = MENUS.register("fluid_storage", () -> IMenuTypeExtension.create(FluidStorageMenu::new));
    public static final Supplier<MenuType<BlastFurnaceMenu>> BLAST_FURNACE = MENUS.register("machine_blast_furnace", () -> IMenuTypeExtension.create(BlastFurnaceMenu::new));
    public static final Supplier<MenuType<WoodBurnerMenu>> WOOD_BURNER = MENUS.register("machine_wood_burner", () -> IMenuTypeExtension.create(WoodBurnerMenu::new));
    public static final Supplier<MenuType<ShredderMenu>> SHREDDER = MENUS.register("machine_shredder", () -> IMenuTypeExtension.create(ShredderMenu::new));
    public static final Supplier<MenuType<SolderingStationMenu>> SOLDERING_STATION = MENUS.register("machine_soldering_station", () -> IMenuTypeExtension.create(SolderingStationMenu::new));
    public static final Supplier<MenuType<HeaterMenu>> HEATER = MENUS.register("heater", () -> IMenuTypeExtension.create(HeaterMenu::new));
    public static final Supplier<MenuType<CrucibleMenu>> CRUCIBLE = MENUS.register("machine_crucible", () -> IMenuTypeExtension.create(CrucibleMenu::new));

    public static final Supplier<MenuType<MachinePressMenu>> MACHINE_PRESS =
            MENUS.register(
                    "machine_press",
                    () -> IMenuTypeExtension.create(
                            MachinePressMenu::new
                    )
            );

    public static final Supplier<MenuType<AssemblyMachineMenu>> ASSEMBLY_MACHINE = MENUS.register("assembly_machine", () -> IMenuTypeExtension.create(AssemblyMachineMenu::new));
    public static final Supplier<MenuType<ChemicalPlantMenu>> CHEMICAL_PLANT = MENUS.register("machine_chemical_plant", () -> IMenuTypeExtension.create(ChemicalPlantMenu::new));
    public static final Supplier<MenuType<BatterySocketMenu>> BATTERY_SOCKET = MENUS.register("battery_socket", () -> IMenuTypeExtension.create(BatterySocketMenu::new));
    public static final Supplier<MenuType<FatManMenu>> NUKE_MAN = MENUS.register("nuke_man", () -> IMenuTypeExtension.create(FatManMenu::new));
    public static final Supplier<MenuType<StorageCrateMenu>> STORAGE_CRATE = MENUS.register("storage_crate", () -> IMenuTypeExtension.create(StorageCrateMenu::new));

    public static final Supplier<MenuType<HBMAnvilMenu>> HBM_ANVIL =
            MENUS.register(
                    "hbm_anvil",
                    () -> IMenuTypeExtension.create(HBMAnvilMenu::new)
            );

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}