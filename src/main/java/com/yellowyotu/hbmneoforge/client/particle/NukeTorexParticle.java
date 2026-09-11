package com.yellowyotu.hbmneoforge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.client.NukeClientEffects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Direct 1.21.1 port of CE EntityNukeTorex + the cloudlet part of RenderTorex.
 * The cloud is one controller particle containing CE's own Cloudlet list; cloudlets are not vanilla particles.
 */
public final class NukeTorexParticle extends TextureSheetParticle {
    public static final int FIRST_CONDENSE_HEIGHT = 130;
    public static final int SECOND_CONDENSE_HEIGHT = 170;
    public static final int BLAST_WAVE_HEADSTART = 5;
    public static final int MAX_CLOUDLETS = 20_000;

    private static final double NR1 = 2.5D;
    private static final double NG1 = 1.3D;
    private static final double NB1 = 0.4D;
    private static final double NR2 = 0.1D;
    private static final double NG2 = 0.075D;
    private static final double NB2 = 0.05D;
    private static final float SCALE = 1.75F;
    private static final float CS = 1.5F;

    public double coreHeight = 3.0D;
    public double convectionHeight = 3.0D;
    public double torusWidth = 3.0D;
    public double rollerSize = 1.0D;
    public double heat = 1.0D;
    public double lastSpawnY = -1.0D;
    public final ArrayList<Cloudlet> cloudlets = new ArrayList<>();
    public int maxAge = 1000;
    public float humidity = -1.0F;
    public boolean didPlaySound;
    public boolean didShake;
    private final SpriteSet sprites;

    private NukeTorexParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.sprites = sprites;
        this.pickSprite(sprites);
        setScale(SCALE);
    }

    private void setScale(float scale) {
        coreHeight *= scale;
        convectionHeight *= scale;
        torusWidth *= scale;
        rollerSize *= scale;
        maxAge = (int) (45 * 20 * scale);
        lifetime = maxAge;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        age++;
        if (age > maxAge) {
            for (Cloudlet cloud : cloudlets) {
                cloud.removeVisual();
            }
            cloudlets.clear();
            remove();
            return;
        }

        if (age == 1) {
            NukeClientEffects.startFlash();
        }

        if (humidity == -1.0F) {
            humidity = level.getBiome(BlockPos.containing(x, y, z)).value().getModifiedClimateSettings().downfall();
        }
        if (lastSpawnY == -1.0D) {
            lastSpawnY = y - 3.0D;
        }

        int spawnTarget = Math.max(level.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(x), Mth.floor(z)) - 3, 1);
        double moveSpeed = 0.5D;
        if (Math.abs(spawnTarget - lastSpawnY) < moveSpeed) {
            lastSpawnY = spawnTarget;
        } else {
            lastSpawnY += moveSpeed * Math.signum(spawnTarget - lastSpawnY);
        }

        double range = (torusWidth - rollerSize) * 0.5D;
        double simSpeed = getSimulationSpeed();
        int cloudLife = Math.min(age * age + 200, maxAge - age + 200);
        int toSpawn = (int) (0.6D * Math.min(
                Math.max(0, MAX_CLOUDLETS - cloudlets.size()),
                Math.ceil(10.0D * simSpeed * simSpeed * Math.min(1.0D, 1200.0D / cloudLife))));

        for (int i = 0; i < toSpawn; i++) {
            double px = x + random.nextGaussian() * range;
            double pz = z + random.nextGaussian() * range;
            Cloudlet cloud = new Cloudlet(px, lastSpawnY, pz,
                    (float) (random.nextDouble() * Math.PI * 2.0D), 0, cloudLife);
            cloud.setScale(
                    (float) (Math.sqrt(SCALE) * 3.0D + age * 0.0025D * SCALE),
                    (float) (Math.sqrt(SCALE) * 3.0D + age * 0.0025D * 6.0D * CS * SCALE));
            cloudlets.add(cloud);
        }

        if (age < 150) {
            int cloudCount = Math.min(age * 2, 100);
            int shockLife = Math.max(400 - age * 20, 50);
            for (int i = 0; i < cloudCount && cloudlets.size() < MAX_CLOUDLETS; i++) {
                double radius = (age + random.nextDouble() * 2.0D) * 1.5D;
                float rot = (float) (Math.PI * 2.0D * random.nextDouble());
                double px = x + Math.cos(rot) * radius;
                double pz = z + Math.sin(rot) * radius;
                double py = level.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(px) + 1, Mth.floor(pz));
                cloudlets.add(new Cloudlet(px, py, pz, rot, 0, shockLife, TorexType.SHOCK)
                        .setScale(SCALE * 5.0F, SCALE * 2.0F)
                        .setMotion(Mth.clamp(0.25D * age - 5.0D, 0.0D, 1.0D)));
            }

            if (!didPlaySound) {
                Player player = Minecraft.getInstance().player;
                if (player != null && player.distanceToSqr(x, y, z) < Math.pow((age * 1.5D + 1.0D) * 1.5D, 2.0D)) {
                    level.playLocalSound(x, y, z, ModSounds.NUCLEAR_EXPLOSION.get(), SoundSource.HOSTILE, 10_000.0F, 1.0F, false);
                    didPlaySound = true;
                    if (!didShake) {
                        NukeClientEffects.startShake();
                        didShake = true;
                    }
                }
            }
        }

        if (age < 200) {
            int ringLife = (int) (cloudLife * SCALE);
            for (int i = 0; i < 2 && cloudlets.size() < MAX_CLOUDLETS; i++) {
                Cloudlet cloud = new Cloudlet(x, y + coreHeight, z,
                        (float) (random.nextDouble() * Math.PI * 2.0D), 0, ringLife, TorexType.RING);
                cloud.setScale(
                        (float) (Math.sqrt(SCALE) * CS + age * 0.0015D * SCALE),
                        (float) (Math.sqrt(SCALE) * CS + age * 0.0015D * 6.0D * CS * SCALE));
                cloudlets.add(cloud);
            }
        }

        if (humidity > 0.0F && age < 220) {
            spawnCondensationClouds(age, humidity, FIRST_CONDENSE_HEIGHT, 80, 4);
            spawnCondensationClouds(age, humidity, SECOND_CONDENSE_HEIGHT, 80, 2);
        }

        for (int i = cloudlets.size() - 1; i >= 0; i--) {
            Cloudlet cloud = cloudlets.get(i);
            if (cloud.isDead) {
                cloudlets.remove(i);
            } else {
                cloud.update();
            }
        }

        coreHeight += 0.15D;
        torusWidth += 0.05D;
        rollerSize = torusWidth * 0.35D;
        convectionHeight = coreHeight + rollerSize;

        int maxHeat = (int) (50.0D * SCALE * SCALE);
        heat = maxHeat - Math.pow((maxHeat * age) / (double) maxAge, 0.6D);
    }

    private void spawnCondensationClouds(int age, float humidity, int height, int count, int spreadAngle) {
        if (y + age <= height) {
            return;
        }
        for (int i = 0; i < (int) (5.0D * humidity * count / (double) spreadAngle); i++) {
            for (int j = 1; j < spreadAngle && cloudlets.size() < MAX_CLOUDLETS; j++) {
                float angle = (float) (Math.PI * 2.0D * random.nextDouble());
                double pitch = Math.acos((height - y) / age)
                        + Math.toRadians(humidity * humidity * 90.0D * j * (0.1D * random.nextDouble() - 0.05D));
                double horizontal = age * Math.sin(pitch);
                double px = x + Math.cos(angle) * horizontal;
                double py = y + age * Math.cos(pitch);
                double pz = z + Math.sin(angle) * horizontal;
                Cloudlet cloud = new Cloudlet(px, py, pz, angle, 0,
                        (int) ((20 + age / 10.0D) * (1.0D + random.nextDouble() * 0.1D)), TorexType.CONDENSATION);
                cloud.setScale(3.0F * CS * SCALE, 4.0F * CS * SCALE);
                cloudlets.add(cloud);
            }
        }
    }

    public double getSimulationSpeed() {
        int simSlow = maxAge / 4;
        if (age > maxAge) {
            return 0.0D;
        }
        if (age > simSlow) {
            return 1.0D - (double) (age - simSlow) / (double) (maxAge - simSlow);
        }
        return 1.0D;
    }

    public float getCloudAlpha() {
        int fadeOut = maxAge * 3 / 4;
        if (age > fadeOut) {
            return 1.0F - (float) (age - fadeOut) / (float) (maxAge - fadeOut);
        }
        return 1.0F;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        // Cloudlets are rendered as their own TextureSheetParticle instances.
        // The controller remains responsible for the exact CE simulation only.
        renderFlares(camera, partialTick);
    }

    private void renderFlares(Camera camera, float partialTick) {
        float flareDuration = SCALE * 100.0F;
        if (age >= flareDuration + 1.0F) {
            return;
        }
        double flareAge = Math.min(age + partialTick, flareDuration);
        float flareAlpha = (float) Math.min(1.0D, (flareDuration - flareAge) / flareDuration);
        Random seeded = new Random(432L);
        for (int i = 0; i < 3; i++) {
            double px = x + seeded.nextGaussian() * 0.5D * rollerSize;
            double py = y + coreHeight + seeded.nextGaussian() * 0.5D * rollerSize;
            double pz = z + seeded.nextGaussian() * 0.5D * rollerSize;
            NukeFlareParticle.spawn(level, px, py, pz, (float) (10.0D * rollerSize), Math.max(1, (int) (flareAlpha * 3.0F)));
        }
    }

    @Override
    public void remove() {
        for (Cloudlet cloud : cloudlets) {
            cloud.removeVisual();
        }
        cloudlets.clear();
        super.remove();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public enum TorexType {
        STANDARD,
        RING,
        CONDENSATION,
        SHOCK
    }

    public final class Cloudlet {
        public double posX;
        public double posY;
        public double posZ;
        public double prevPosX;
        public double prevPosY;
        public double prevPosZ;
        public double motionX;
        public double motionY;
        public double motionZ;
        public int age;
        public int cloudletLife;
        public float angle;
        public boolean isDead;
        public float rangeMod = 1.0F;
        public float colorMod = 1.0F;
        public double colorR;
        public double colorG;
        public double colorB;
        public double prevColorR;
        public double prevColorG;
        public double prevColorB;
        public double renderSortDistanceSq;
        public TorexType type;
        public float startingScale = 3.0F;
        public float growingScale = 5.0F;
        private final NukeCloudParticle visual;
        private double computedMotionX;
        private double computedMotionY;
        private double computedMotionZ;
        private double motionMult = 1.0D;
        private final double motionConvectionMult = 0.5D;
        private final double motionLiftMult = 0.625D;
        private final double motionRingMult = 0.5D;
        private final double motionCondensationMult = 1.0D;
        private final double motionShockwaveMult = 1.0D;

        Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge) {
            this(posX, posY, posZ, angle, age, maxAge, TorexType.STANDARD);
        }

        Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge, TorexType type) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.prevPosX = posX;
            this.prevPosY = posY;
            this.prevPosZ = posZ;
            this.age = age;
            this.cloudletLife = maxAge;
            this.angle = angle;
            this.rangeMod = 0.3F + random.nextFloat() * 0.7F;
            this.colorMod = 0.8F + random.nextFloat() * 0.2F;
            this.type = type;
            updateColor();
            this.visual = new NukeCloudParticle(level, sprites, this);
            Minecraft.getInstance().particleEngine.add(this.visual);
            syncVisual();
        }

        void update() {
            age++;
            if (age > cloudletLife) {
                isDead = true;
                removeVisual();
            }
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            double simDeltaX = x - posX;
            double simDeltaZ = z - posZ;
            double simPosX = x + Math.sqrt(simDeltaX * simDeltaX + simDeltaZ * simDeltaZ);

            if (type == TorexType.STANDARD) {
                getConvectionMotion(simPosX);
                double convectionX = computedMotionX;
                double convectionY = computedMotionY;
                double convectionZ = computedMotionZ;
                getLiftMotion(simPosX);
                double factor = Mth.clamp((posY - y) / coreHeight, 0.0D, 1.0D);
                double inverseFactor = 1.0D - factor;
                motionX = convectionX * factor + computedMotionX * inverseFactor;
                motionY = convectionY * factor + computedMotionY * inverseFactor;
                motionZ = convectionZ * factor + computedMotionZ * inverseFactor;
            } else if (type == TorexType.RING) {
                getRingMotion(simPosX);
                motionX = computedMotionX;
                motionY = computedMotionY;
                motionZ = computedMotionZ;
            } else if (type == TorexType.CONDENSATION) {
                getCondensationMotion();
                motionX = computedMotionX;
                motionY = computedMotionY;
                motionZ = computedMotionZ;
            } else if (type == TorexType.SHOCK) {
                getShockwaveMotion();
                motionX = computedMotionX;
                motionY = computedMotionY;
                motionZ = computedMotionZ;
            }

            double mult = motionMult * getSimulationSpeed();
            posX += motionX * mult;
            posY += motionY * mult;
            posZ += motionZ * mult;
            updateColor();
            syncVisual();
        }

        private void syncVisual() {
            visual.sync();
        }

        private void removeVisual() {
            visual.remove();
        }

        private void getCondensationMotion() {
            double speed = motionCondensationMult * SCALE * 0.125D;
            setNormalizedMotion(posX - x, 0.0D, posZ - z, speed);
        }

        private void getShockwaveMotion() {
            double speed = motionShockwaveMult * SCALE * 0.25D;
            setNormalizedMotion(posX - x, 0.0D, posZ - z, speed);
        }

        private void getRingMotion(double simPosX) {
            if (simPosX > x + torusWidth * 2.0D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }
            double torusPosX = x + torusWidth;
            double torusPosY = y + coreHeight * 0.5D;
            double deltaX = torusPosX - simPosX;
            double deltaY = torusPosY - posY;
            double roller = rollerSize * rangeMod * 0.25D;
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / roller - 1.0D;
            double func = 1.0D - Math.exp(-dist);
            float a = (float) (func * Math.PI * 0.5D);
            double rotX = -deltaX / dist;
            double rotY = -deltaY / dist;
            float sin = Mth.sin(a);
            float cos = Mth.cos(a);
            double rotatedX = rotX * cos + rotY * sin;
            double rotatedY = rotY * cos - rotX * sin;
            setNormalizedMotion(torusPosX + rotatedX - simPosX, torusPosY + rotatedY - posY, 0.0D, motionRingMult * 0.5D);
            rotateComputedMotionAroundY();
        }

        private void getConvectionMotion(double simPosX) {
            if (simPosX > x + torusWidth * 2.0D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }
            double torusPosX = x + torusWidth;
            double torusPosY = y + coreHeight;
            double deltaX = torusPosX - simPosX;
            double deltaY = torusPosY - posY;
            double roller = rollerSize * rangeMod;
            double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / roller - 1.0D;
            double func = 1.0D - Math.exp(-dist);
            float a = (float) (func * Math.PI * 0.5D);
            double rotX = -deltaX / dist;
            double rotY = -deltaY / dist;
            float sin = Mth.sin(a);
            float cos = Mth.cos(a);
            double rotatedX = rotX * cos + rotY * sin;
            double rotatedY = rotY * cos - rotX * sin;
            setNormalizedMotion(torusPosX + rotatedX - simPosX, torusPosY + rotatedY - posY, 0.0D, motionConvectionMult);
            rotateComputedMotionAroundY();
        }

        private void getLiftMotion(double simPosX) {
            double scale = Mth.clamp(1.0D - (simPosX - (x + torusWidth)), 0.0D, 1.0D) * motionLiftMult;
            setNormalizedMotion(x - posX, y + convectionHeight - posY, z - posZ, scale);
        }

        private void setComputedMotion(double x, double y, double z) {
            computedMotionX = x;
            computedMotionY = y;
            computedMotionZ = z;
        }

        private void setNormalizedMotion(double x, double y, double z, double speed) {
            double lengthSq = x * x + y * y + z * z;
            if (lengthSq < 1.0E-8D) {
                setComputedMotion(0.0D, 0.0D, 0.0D);
                return;
            }
            double scale = speed / Math.sqrt(lengthSq);
            setComputedMotion(x * scale, y * scale, z * scale);
        }

        private void rotateComputedMotionAroundY() {
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            double mx = computedMotionX;
            double mz = computedMotionZ;
            computedMotionX = mx * cos + mz * sin;
            computedMotionZ = mz * cos - mx * sin;
        }

        private void updateColor() {
            prevColorR = colorR;
            prevColorG = colorG;
            prevColorB = colorB;
            double exX = x;
            double exY = y + coreHeight;
            double exZ = z;
            double dx = exX - posX;
            double dy = exY - posY;
            double dz = exZ - posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            distSq /= type == TorexType.SHOCK ? heat * 3.0D : heat;
            double col = 2.0D / Math.max(distSq, 1.0D);
            colorR = NR2 + (NR1 - NR2) * col;
            colorG = NG2 + (NG1 - NG2) * col;
            colorB = NB2 + (NB1 - NB2) * col;
        }

        NukeTorexParticle owner() {
            return NukeTorexParticle.this;
        }

        Cloudlet setScale(float start, float grow) {
            startingScale = start;
            growingScale = grow;
            return this;
        }

        Cloudlet setMotion(double mult) {
            motionMult = mult;
            return this;
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new NukeTorexParticle(level, x, y, z, sprites);
        }
    }
}
