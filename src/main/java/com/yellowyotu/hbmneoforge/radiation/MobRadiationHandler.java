package com.yellowyotu.hbmneoforge.radiation;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;

public final class MobRadiationHandler {

    private static final float VILLAGER_CONVERSION_LEVEL = 500.0F;
    private static final Set<ResourceLocation> IMMUNE_ENTITY_TYPES = Set.of(
            ResourceLocation.withDefaultNamespace("magma_cube"),
            ResourceLocation.withDefaultNamespace("slime"),
            ResourceLocation.withDefaultNamespace("vex"),
            ResourceLocation.withDefaultNamespace("iron_golem"),
            ResourceLocation.withDefaultNamespace("snow_golem"),
            ResourceLocation.withDefaultNamespace("witch")
    );

    private MobRadiationHandler() {
    }

    public static boolean isRadiationImmune(LivingEntity entity) {
        return IMMUNE_ENTITY_TYPES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static boolean handleTransformation(LivingEntity entity, float radiation) {
        if (radiation < VILLAGER_CONVERSION_LEVEL || !(entity instanceof Villager villager) || !(entity.level() instanceof ServerLevel level)) {
            return false;
        }

        VillagerData villagerData = villager.getVillagerData();
        boolean baby = villager.isBaby();
        ZombieVillager zombie = EntityType.ZOMBIE_VILLAGER.create(level);
        if (zombie == null) {
            return false;
        }

        zombie.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
        zombie.setVillagerData(villagerData);
        zombie.setBaby(baby);
        zombie.setCustomName(villager.getCustomName());
        zombie.setCustomNameVisible(villager.isCustomNameVisible());
        if (!level.addFreshEntity(zombie)) {
            return false;
        }

        villager.discard();
        return true;
    }
}
