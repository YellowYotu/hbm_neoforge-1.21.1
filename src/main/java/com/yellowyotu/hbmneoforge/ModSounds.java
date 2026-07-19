package com.yellowyotu.hbmneoforge;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID
            );

    public static final Supplier<SoundEvent> PLAYER_COUGH =
            SOUNDS.register(
                    "player.cough",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                                    "player.cough"
                            )
                    )
            );

    public static final Supplier<SoundEvent> PLAYER_VOMIT =
            SOUNDS.register(
                    "player.vomit",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                                    "player.vomit"
                            )
                    )
            );


    public static final Supplier<SoundEvent> GEIGER_1 = geiger("item.geiger1");
    public static final Supplier<SoundEvent> GEIGER_2 = geiger("item.geiger2");
    public static final Supplier<SoundEvent> GEIGER_3 = geiger("item.geiger3");
    public static final Supplier<SoundEvent> GEIGER_4 = geiger("item.geiger4");
    public static final Supplier<SoundEvent> GEIGER_5 = geiger("item.geiger5");
    public static final Supplier<SoundEvent> GEIGER_6 = geiger("item.geiger6");


    public static final Supplier<SoundEvent> RADAWAY = sound("item.radaway");

    public static final Supplier<SoundEvent> UPGRADE_PLUG = sound("item.upgrade_plug");
    public static final Supplier<SoundEvent> SOLDERING_OPERATE = sound("block.soldering_operate");
    public static final Supplier<SoundEvent> ASSEMBLER_OPERATE = sound("block.assembler_operate");
    public static final Supplier<SoundEvent> ASSEMBLER_START = sound("block.assembler_start");
    public static final Supplier<SoundEvent> ASSEMBLER_STOP = sound("block.assembler_stop");
    public static final Supplier<SoundEvent> ASSEMBLER_STRIKE = sound("block.assembler_strike");
    public static final Supplier<SoundEvent> SLIDING_SEAL_MOVE = sound("door.sliding_seal_move");
    public static final Supplier<SoundEvent> SLIDING_SEAL_STOP = sound("door.sliding_seal_stop");

    private static Supplier<SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, id)));
    }

    private static Supplier<SoundEvent> geiger(String id) {
        return SOUNDS.register(
                id,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(
                                HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                                id
                        )
                )
        );
    }

    private ModSounds() {
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}