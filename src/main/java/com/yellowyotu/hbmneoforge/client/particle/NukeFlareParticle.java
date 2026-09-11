package com.yellowyotu.hbmneoforge.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class NukeFlareParticle extends TextureSheetParticle {
    private static SpriteSet sprites;
    private final float baseSize;

    private NukeFlareParticle(ClientLevel level, double x, double y, double z, float size, int lifetime) {
        super(level, x, y, z);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = Math.max(1, lifetime);
        this.baseSize = size;
        this.quadSize = size;
        this.alpha = 1.0F;
        setColor(1.0F, 0.95F, 0.80F);
        if (sprites != null) {
            setSpriteFromAge(sprites);
        }
    }

    public static void spawn(ClientLevel level, double x, double y, double z, float size, int lifetime) {
        if (sprites == null) {
            return;
        }
        Minecraft.getInstance().particleEngine.add(new NukeFlareParticle(level, x, y, z, size, lifetime));
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        float life = age / (float) lifetime;
        quadSize = baseSize * (1.0F + life * 0.15F);
        alpha = 1.0F - life;
        if (sprites != null) {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
            NukeFlareParticle.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd) {
            return new NukeFlareParticle(level, x, y, z, 16.0F, 20);
        }
    }
}
