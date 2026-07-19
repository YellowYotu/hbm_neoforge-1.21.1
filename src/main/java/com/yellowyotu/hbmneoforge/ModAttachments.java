package com.yellowyotu.hbmneoforge;

import com.mojang.serialization.Codec;
import com.yellowyotu.hbmneoforge.radiation.PlayerRadiationData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HBMsNuclearTechModUnofficialNeoForgeEdition.MODID);

    public static final Supplier<AttachmentType<Float>> LEGACY_RADIATION = ATTACHMENTS.register("radiation", () -> AttachmentType.builder(() -> 0.0F).serialize(Codec.FLOAT).build());
    public static final Supplier<AttachmentType<PlayerRadiationData>> RADIATION_DATA = ATTACHMENTS.register("radiation_data", () -> AttachmentType.builder(PlayerRadiationData::empty).serialize(PlayerRadiationData.CODEC).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
