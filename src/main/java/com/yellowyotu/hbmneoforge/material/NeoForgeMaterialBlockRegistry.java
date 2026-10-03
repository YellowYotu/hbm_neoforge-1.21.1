package com.yellowyotu.hbmneoforge.material;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class NeoForgeMaterialBlockRegistry {
    private NeoForgeMaterialBlockRegistry() {}

    public static Map<String, DeferredBlock<Block>> registerMissing(DeferredRegister.Blocks blocks) {
        Set<String> existing = new HashSet<>();
        blocks.getEntries().forEach(entry -> existing.add(entry.getId().getPath()));
        LinkedHashMap<String, DeferredBlock<Block>> result = new LinkedHashMap<>();
        HBMMaterialCatalog.forms().stream()
                .filter(form -> form.shape() == MaterialShape.BLOCK)
                .filter(form -> !existing.contains(form.registryName()))
                .forEach(form -> result.put(form.registryName(), blocks.register(form.registryName(), () ->
                        new Block(BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .strength(5.0F, 10.0F)
                                .sound(SoundType.METAL)
                                .requiresCorrectToolForDrops()))));
        return Collections.unmodifiableMap(result);
    }
}
