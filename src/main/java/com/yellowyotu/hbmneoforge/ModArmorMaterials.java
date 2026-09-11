package com.yellowyotu.hbmneoforge;

import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, HBMsNuclearTechModUnofficialNeoForgeEdition.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register("hazmat", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), defense -> {
                defense.put(ArmorItem.Type.BOOTS, 1);
                defense.put(ArmorItem.Type.LEGGINGS, 4);
                defense.put(ArmorItem.Type.CHESTPLATE, 5);
                defense.put(ArmorItem.Type.HELMET, 2);
                defense.put(ArmorItem.Type.BODY, 5);
            }),
            5,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> Ingredient.of(ModItems.HAZMAT_CLOTH.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "hazmat"))),
            0.0F,
            0.0F
    ));


    private static ArmorMaterial gasMaskMaterial(String texture) {
        return new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), defense -> {
                    defense.put(ArmorItem.Type.BOOTS, 0);
                    defense.put(ArmorItem.Type.LEGGINGS, 0);
                    defense.put(ArmorItem.Type.CHESTPLATE, 0);
                    defense.put(ArmorItem.Type.HELMET, 2);
                    defense.put(ArmorItem.Type.BODY, 0);
                }),
                5,
                SoundEvents.ARMOR_EQUIP_GENERIC,
                () -> Ingredient.of(ModItems.HAZMAT_CLOTH.get()),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, texture))),
                0.0F,
                0.0F
        );
    }

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK = ARMOR_MATERIALS.register("gas_mask", () -> gasMaskMaterial("gas_mask"));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK_M65 = ARMOR_MATERIALS.register("gas_mask_m65", () -> gasMaskMaterial("gas_mask_m65"));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK_MONO = ARMOR_MATERIALS.register("gas_mask_mono", () -> gasMaskMaterial("gas_mask_mono"));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAS_MASK_OLDE = ARMOR_MATERIALS.register("gas_mask_olde", () -> gasMaskMaterial("gas_mask_olde"));

    private ModArmorMaterials() {
    }

    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }
}
