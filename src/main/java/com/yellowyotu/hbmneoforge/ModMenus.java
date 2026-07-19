package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.menu.AssemblyMachineMenu;
import com.yellowyotu.hbmneoforge.menu.BatterySocketMenu;
import com.yellowyotu.hbmneoforge.menu.HBMAnvilMenu;
import com.yellowyotu.hbmneoforge.menu.MachinePressMenu;
import com.yellowyotu.hbmneoforge.menu.ShredderMenu;
import com.yellowyotu.hbmneoforge.menu.SolderingStationMenu;
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

    public static final Supplier<MenuType<ShredderMenu>> SHREDDER = MENUS.register("machine_shredder", () -> IMenuTypeExtension.create(ShredderMenu::new));
    public static final Supplier<MenuType<SolderingStationMenu>> SOLDERING_STATION = MENUS.register("machine_soldering_station", () -> IMenuTypeExtension.create(SolderingStationMenu::new));

    public static final Supplier<MenuType<MachinePressMenu>> MACHINE_PRESS =
            MENUS.register(
                    "machine_press",
                    () -> IMenuTypeExtension.create(
                            MachinePressMenu::new
                    )
            );

    public static final Supplier<MenuType<AssemblyMachineMenu>> ASSEMBLY_MACHINE = MENUS.register("assembly_machine", () -> IMenuTypeExtension.create(AssemblyMachineMenu::new));
    public static final Supplier<MenuType<BatterySocketMenu>> BATTERY_SOCKET = MENUS.register("battery_socket", () -> IMenuTypeExtension.create(BatterySocketMenu::new));

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