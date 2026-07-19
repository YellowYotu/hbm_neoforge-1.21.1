package com.yellowyotu.hbmneoforge.radiation;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModSounds;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID)
public final class RadiationEvents {

    private static final float FIRST_SYMPTOMS = 200.0F;
    private static final float LEVEL_400 = 400.0F;
    private static final float VILLAGER_CONVERSION_LEVEL = 500.0F;
    private static final float LEVEL_600 = 600.0F;
    private static final float LEVEL_800 = 800.0F;
    private static final float SWEAT_LEVEL = 900.0F;
    private static final float LETHAL_LEVEL = 1000.0F;

    private RadiationEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide() || !entity.isAlive()) {
            return;
        }

        if (entity instanceof Player player) {
            RadiationSystem.migrateLegacyDose(player);
        }

        float radiation = RadiationSystem.getRadiation(entity);
        if (!isCreativeOrSpectator(entity) && !MobRadiationHandler.isRadiationImmune(entity)) {
            if (MobRadiationHandler.handleTransformation(entity, radiation)) {
                return;
            }
            applyRadiationEffects(entity, radiation);
        }

        if (!entity.isAlive()) {
            return;
        }

        applyRadiationExposure(entity);
        if (entity.tickCount % 20 == 0) {
            RadiationSystem.rollEnvironmentRadiationBuffer(entity);
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            float updatedRadiation = RadiationSystem.getRadiation(serverPlayer);
            if (updatedRadiation >= FIRST_SYMPTOMS) {
                awardRadiationAdvancement(serverPlayer);
            }
            if (!serverPlayer.getAbilities().instabuild) {
                handleRadiationFx(serverPlayer, updatedRadiation);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        RadiationSystem.clearAll(event.getEntity());
    }

    private static void applyRadiationExposure(LivingEntity entity) {
        if (MobRadiationHandler.isRadiationImmune(entity)) {
            return;
        }

        if (entity instanceof Player player) {
            float inventoryRadiation = RadiationExposure.getInventoryRadiationPerSecond(player);
            if (inventoryRadiation > 0.0F) {
                RadiationSystem.contaminate(player, RadiationContaminationType.CREATIVE, inventoryRadiation / 20.0F);
            }
        }

        float chunkRadiation = ChunkRadiationManager.getRadiation(entity.level(), entity.blockPosition());
        if (chunkRadiation > 0.0F) {
            RadiationSystem.contaminate(entity, RadiationContaminationType.CREATIVE, chunkRadiation / 20.0F);
        }
    }

    private static void awardRadiationAdvancement(ServerPlayer player) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "yay_radiation");
        var advancement = player.serverLevel().getServer().getAdvancements().get(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, "radiation");
        }
    }

    private static void applyRadiationEffects(LivingEntity entity, float radiation) {
        if (radiation < FIRST_SYMPTOMS) {
            return;
        }

        int rng = entity.getRandom().nextInt(21_000);
        if (radiation >= LETHAL_LEVEL) {
            entity.hurt(entity.damageSources().genericKill(), 1000.0F);
            RadiationSystem.clearRadiation(entity);
            if (entity.isAlive()) {
                entity.kill();
            }
            return;
        }

        if (radiation >= LEVEL_800) {
            if (rng % 300 == 0) {
                addEffect(entity, MobEffects.CONFUSION, 5 * 30, 0);
            }
            if (rng % 300 == 50) {
                addEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            }
            if (rng % 300 == 100) {
                addEffect(entity, MobEffects.WEAKNESS, 10 * 20, 2);
            }
            if (rng % 500 == 0) {
                addEffect(entity, MobEffects.POISON, 3 * 20, 2);
            }
            if (rng % 700 == 0) {
                addEffect(entity, MobEffects.WITHER, 3 * 20, 1);
            }
            if (rng % 300 == 150) {
                addEffect(entity, MobEffects.HUNGER, 5 * 20, 3);
            }
            if (rng % 300 == 200) {
                addEffect(entity, MobEffects.DIG_SLOWDOWN, 5 * 20, 3);
            }
        } else if (radiation >= LEVEL_600) {
            if (rng % 300 == 0) {
                addEffect(entity, MobEffects.CONFUSION, 5 * 30, 0);
            }
            if (rng % 300 == 50) {
                addEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            }
            if (rng % 300 == 100) {
                addEffect(entity, MobEffects.WEAKNESS, 10 * 20, 2);
            }
            if (rng % 500 == 0) {
                addEffect(entity, MobEffects.POISON, 3 * 20, 1);
            }
            if (rng % 300 == 150) {
                addEffect(entity, MobEffects.HUNGER, 3 * 20, 3);
            }
            if (rng % 400 == 0) {
                addEffect(entity, MobEffects.DIG_SLOWDOWN, 6 * 20, 2);
            }
        } else if (radiation >= LEVEL_400) {
            if (rng % 300 == 0) {
                addEffect(entity, MobEffects.CONFUSION, 5 * 30, 0);
            }
            if (rng % 500 == 50) {
                addEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0);
            }
            if (rng % 300 == 100) {
                addEffect(entity, MobEffects.WEAKNESS, 5 * 20, 1);
            }
            if (rng % 500 == 150) {
                addEffect(entity, MobEffects.HUNGER, 3 * 20, 2);
            }
            if (rng % 600 == 0) {
                addEffect(entity, MobEffects.DIG_SLOWDOWN, 4 * 20, 1);
            }
        } else {
            if (rng % 300 == 0) {
                addEffect(entity, MobEffects.CONFUSION, 5 * 20, 0);
            }
            if (rng % 500 == 0) {
                addEffect(entity, MobEffects.WEAKNESS, 5 * 20, 0);
            }
            if (rng % 700 == 0) {
                addEffect(entity, MobEffects.HUNGER, 3 * 20, 2);
            }
            if (rng % 800 == 0) {
                addEffect(entity, MobEffects.DIG_SLOWDOWN, 4 * 20, 0);
            }
        }
    }

    private static boolean isCreativeOrSpectator(LivingEntity entity) {
        return entity instanceof Player player && (player.getAbilities().instabuild || player.isSpectator());
    }

    private static void handleRadiationFx(ServerPlayer player, float radiation) {
        Random random = new Random(player.getId());
        int bloodVomitOffset = random.nextInt(600);
        int normalVomitOffset = random.nextInt(1200);
        long gameTime = player.level().getGameTime();

        if (radiation > LEVEL_600) {
            long phase = (gameTime + bloodVomitOffset) % 600L;
            if (phase < 20L) {
                spawnVomitParticles(player, true, 25);
                if (phase == 1L) {
                    playVomit(player);
                }
            }
        } else if (radiation > FIRST_SYMPTOMS) {
            long phase = (gameTime + normalVomitOffset) % 1200L;
            if (phase < 20L) {
                spawnVomitParticles(player, false, 15);
                if (phase == 1L) {
                    playVomit(player);
                }
            }
        }

        if (radiation > SWEAT_LEVEL && (gameTime + random.nextInt(10)) % 10L == 0L) {
            spawnRadiationSweat(player);
        }
    }

    private static void playVomit(ServerPlayer player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PLAYER_VOMIT.get(), player.getSoundSource(), 1.0F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
    }

    private static void spawnVomitParticles(ServerPlayer player, boolean bloody, int count) {
        Vec3 look = player.getLookAngle();
        double x = player.getX();
        double y = player.getEyeY();
        double z = player.getZ();

        for (int i = 0; i < count; i++) {
            BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, bloody ? Blocks.REDSTONE_BLOCK.defaultBlockState() : player.getRandom().nextBoolean() ? Blocks.LIME_TERRACOTTA.defaultBlockState() : Blocks.MAGENTA_TERRACOTTA.defaultBlockState());
            double velocityX = (look.x + player.getRandom().nextGaussian() * 0.2D) * 0.2D;
            double velocityY = (look.y + player.getRandom().nextGaussian() * 0.2D) * 0.2D;
            double velocityZ = (look.z + player.getRandom().nextGaussian() * 0.2D) * 0.2D;
            player.serverLevel().sendParticles(particle, x, y, z, 0, velocityX, velocityY, velocityZ, 1.0D);
        }
    }

    private static void spawnRadiationSweat(ServerPlayer player) {
        AABB bounds = player.getBoundingBox();
        double x = bounds.minX - 0.2D + (bounds.getXsize() + 0.4D) * player.getRandom().nextDouble();
        double y = bounds.minY + (bounds.getYsize() + 0.2D) * player.getRandom().nextDouble();
        double z = bounds.minZ - 0.2D + (bounds.getZsize() + 0.4D) * player.getRandom().nextDouble();
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState());
        player.serverLevel().sendParticles(particle, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void addEffect(LivingEntity entity, Holder<MobEffect> effect, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }
}
