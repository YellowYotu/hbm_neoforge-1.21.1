package com.yellowyotu.hbmneoforge;

import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.BatterySocketBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.SlidingSealDoorBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.ShredderBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID
            );

    public static final Supplier<BlockEntityType<ShredderBlockEntity>> SHREDDER = BLOCK_ENTITIES.register("machine_shredder", () -> BlockEntityType.Builder.of(ShredderBlockEntity::new, ModBlocks.SHREDDER.get()).build(null));
    public static final Supplier<BlockEntityType<SolderingStationBlockEntity>> SOLDERING_STATION = BLOCK_ENTITIES.register("machine_soldering_station", () -> BlockEntityType.Builder.of(SolderingStationBlockEntity::new, ModBlocks.SOLDERING_STATION.get()).build(null));

    public static final Supplier<BlockEntityType<MachinePressBlockEntity>> MACHINE_PRESS =
            BLOCK_ENTITIES.register(
                    "machine_press",
                    () -> BlockEntityType.Builder.of(
                            MachinePressBlockEntity::new,
                            ModBlocks.MACHINE_PRESS.get()
                    ).build(null)
            );

    public static final Supplier<BlockEntityType<AssemblyMachineBlockEntity>> ASSEMBLY_MACHINE = BLOCK_ENTITIES.register("assembly_machine", () -> BlockEntityType.Builder.of(AssemblyMachineBlockEntity::new, ModBlocks.ASSEMBLY_MACHINE.get()).build(null));
    public static final Supplier<BlockEntityType<BatterySocketBlockEntity>> BATTERY_SOCKET = BLOCK_ENTITIES.register("battery_socket", () -> BlockEntityType.Builder.of(BatterySocketBlockEntity::new, ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null));
    public static final Supplier<BlockEntityType<SlidingSealDoorBlockEntity>> SLIDING_SEAL_DOOR = BLOCK_ENTITIES.register("sliding_seal_door", () -> BlockEntityType.Builder.of(SlidingSealDoorBlockEntity::new, ModBlocks.SLIDING_SEAL_DOOR.get()).build(null));

    public static final Supplier<BlockEntityType<HBMAnvilBlockEntity>> HBM_ANVIL =
            BLOCK_ENTITIES.register(
                    "hbm_anvil",
                    () -> BlockEntityType.Builder.of(
                            HBMAnvilBlockEntity::new,
                            ModBlocks.ANVIL_IRON.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}