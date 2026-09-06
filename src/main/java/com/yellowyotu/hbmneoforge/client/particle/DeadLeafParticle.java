package com.yellowyotu.hbmneoforge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class DeadLeafParticle extends TextureSheetParticle {

    private final boolean flipU;
    private final boolean flipV;

    private DeadLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        float color = 1.0F - random.nextFloat() * 0.2F;
        setColor(color, color, color);
        quadSize = 0.1F;
        lifetime = 200 + random.nextInt(50);
        gravity = 0.2F;
        flipU = random.nextBoolean();
        flipV = random.nextBoolean();
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!onGround) {
            xd += random.nextGaussian() * 0.002D;
            zd += random.nextGaussian() * 0.002D;
            if (yd < -0.025D) {
                yd = -0.025D;
            }
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected float getU0() {
        return flipU ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return flipU ? super.getU0() : super.getU1();
    }

    @Override
    protected float getV0() {
        return flipV ? super.getV1() : super.getV0();
    }

    @Override
    protected float getV1() {
        return flipV ? super.getV0() : super.getV1();
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new DeadLeafParticle(level, x, y, z, sprites);
        }
    }
}
