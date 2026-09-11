package com.yellowyotu.hbmneoforge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/**
 * Render-only view for one CE Torex Cloudlet.
 * Position, color, alpha and scale are driven entirely by NukeTorexParticle.Cloudlet.
 */
final class NukeCloudParticle extends TextureSheetParticle {
    private static final float ONE_THIRD = 1.0F / 3.0F;

    private final NukeTorexParticle.Cloudlet cloud;
    private final NukeTorexParticle parent;

    NukeCloudParticle(ClientLevel level, SpriteSet sprites, NukeTorexParticle.Cloudlet cloud) {
        super(level, cloud.posX, cloud.posY, cloud.posZ);
        this.cloud = cloud;
        this.parent = cloud.owner();
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = Integer.MAX_VALUE;
        this.pickSprite(sprites);
    }

    void sync() {
        this.xo = cloud.prevPosX;
        this.yo = cloud.prevPosY;
        this.zo = cloud.prevPosZ;
        this.setPos(cloud.posX, cloud.posY, cloud.posZ);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float lifeFrac = (float) cloud.age / (float) cloud.cloudletLife;
        float a = (1.0F - lifeFrac) * parent.getCloudAlpha();
        if (cloud.type == NukeTorexParticle.TorexType.CONDENSATION) {
            a *= 0.25F;
        }
        this.alpha = Mth.clamp(a, 0.0001F, 1.0F);
        this.quadSize = cloud.startingScale + lifeFrac * cloud.growingScale;

        float brightness = cloud.type == NukeTorexParticle.TorexType.CONDENSATION
                ? 0.9F
                : 0.75F * cloud.colorMod;
        double greying = cloud.type == NukeTorexParticle.TorexType.RING ? 0.05D : 0.0D;
        double cr = cloud.type == NukeTorexParticle.TorexType.CONDENSATION
                ? 1.0D
                : Mth.lerp(partialTick, cloud.prevColorR, cloud.colorR) + greying;
        double cg = cloud.type == NukeTorexParticle.TorexType.CONDENSATION
                ? 1.0D
                : Mth.lerp(partialTick, cloud.prevColorG, cloud.colorG) + greying;
        double cb = cloud.type == NukeTorexParticle.TorexType.CONDENSATION
                ? 1.0D
                : Mth.lerp(partialTick, cloud.prevColorB, cloud.colorB) + greying;
        this.rCol = clampColor(cr, brightness);
        this.gCol = clampColor(cg, brightness);
        this.bCol = clampColor(cb, brightness);

        super.render(consumer, camera, partialTick);
    }

    @Override
    public void tick() {
        if (!parent.isAlive() || cloud.isDead) {
            remove();
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        float avg = Math.min(1.0F, (rCol + gCol + bCol) * ONE_THIRD);
        int br = Math.max(48, (int) (avg * 240.0F));
        return br | br << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static float clampColor(double color, float brightness) {
        return Mth.clamp((float) color * brightness, 0.15F, 1.0F);
    }
}
