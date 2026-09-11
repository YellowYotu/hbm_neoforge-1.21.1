package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

final class FoundryRenderUtil {
    static final ResourceLocation MOLTEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/block/lava_gray.png");
    static final ResourceLocation CRUCIBLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/block/lava_gray.png");

    private FoundryRenderUtil() {
    }

    static void top(PoseStack poseStack, MultiBufferSource buffers, ResourceLocation texture, int color,
                    float minX, float y, float minZ, float maxX, float maxZ) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        float scroll = textureScroll();
        vertex(consumer, matrix, minX, y, minZ, 0.0F, scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, minX, y, maxZ, 0.0F, 1.0F + scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, maxX, y, maxZ, 1.0F, 1.0F + scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, maxX, y, minZ, 1.0F, scroll, r, g, b, 0.0F, 1.0F, 0.0F);
    }

    /** Original CE channel look: only the liquid floor and liquid surface are rendered, never side walls. */
    static void channelLiquid(PoseStack poseStack, MultiBufferSource buffers, int color,
                              float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(MOLTEN_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int r = brighten((color >> 16) & 255);
        int g = brighten((color >> 8) & 255);
        int b = brighten(color & 255);
        float scroll = textureScroll();

        vertex(consumer, matrix, minX, minY, minZ, 0.0F, scroll, r, g, b, 0.0F, -1.0F, 0.0F);
        vertex(consumer, matrix, maxX, minY, minZ, 1.0F, scroll, r, g, b, 0.0F, -1.0F, 0.0F);
        vertex(consumer, matrix, maxX, minY, maxZ, 1.0F, 1.0F + scroll, r, g, b, 0.0F, -1.0F, 0.0F);
        vertex(consumer, matrix, minX, minY, maxZ, 0.0F, 1.0F + scroll, r, g, b, 0.0F, -1.0F, 0.0F);

        vertex(consumer, matrix, minX, maxY, minZ, 0.0F, scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, minX, maxY, maxZ, 0.0F, 1.0F + scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, maxX, maxY, maxZ, 1.0F, 1.0F + scroll, r, g, b, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, maxX, maxY, minZ, 1.0F, scroll, r, g, b, 0.0F, 1.0F, 0.0F);
    }

    /**
     * Port of CE ParticleFoundry geometry. It has a short horizontal neck, bend and a thin animated falling stream
     * instead of the old solid rectangular column.
     */
    static void foundryStream(PoseStack poseStack, MultiBufferSource buffers, int color, Direction direction,
                              float x, float y, float z, float length, float base, float offset, float partialTick) {
        if (length <= 0.0F) {
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(MOLTEN_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int r = brighten((color >> 16) & 255);
        int g = brighten((color >> 8) & 255);
        int b = brighten(color & 255);

        float phase = ((System.currentTimeMillis() % 2000L) / 2000.0F + partialTick * 0.01F) % 1.0F;
        float width = 0.0625F + phase * 0.0625F;
        float girth = Math.max(0.018F, 0.125F * (1.0F - phase));
        float scroll = ((System.currentTimeMillis() / 100L) % 16L) / 16.0F;

        Direction rot = direction.getClockWise();
        float dx = direction.getStepX() * girth;
        float dz = direction.getStepZ() * girth;
        float rx = rot.getStepX() * width;
        float rz = rot.getStepZ() * width;
        float ox = direction.getStepX() * offset;
        float oz = direction.getStepZ() * offset;

        // Falling stream: same four-sided tapered shape used by CE's ParticleFoundry.
        streamQuad(consumer, matrix,
                x + rx, y + girth, z + rz, x - rx, y + girth, z - rz,
                x - rx, y - length, z - rz, x + rx, y - length, z + rz,
                0.5F - width, length + scroll, 0.5F + width, scroll, r, g, b);
        streamQuad(consumer, matrix,
                x + dx + rx, y, z + dz + rz, x + dx - rx, y, z + dz - rz,
                x + dx - rx, y - length, z + dz - rz, x + dx + rx, y - length, z + dz + rz,
                0.5F - width, length + scroll, 0.5F + width, scroll, r, g, b);
        streamQuad(consumer, matrix,
                x + rx, y + girth, z + rz, x + dx + rx, y, z + dz + rz,
                x + dx + rx, y - length, z + dz + rz, x + rx, y - length, z + rz,
                0.0F, length + scroll, girth, scroll, r, g, b);
        streamQuad(consumer, matrix,
                x - rx, y + girth, z - rz, x + dx - rx, y, z + dz - rz,
                x + dx - rx, y - length, z + dz - rz, x - rx, y - length, z - rz,
                0.0F, length + scroll, girth, scroll, r, g, b);

        // Horizontal neck from the outlet/crucible lip to the bend.
        streamQuad(consumer, matrix,
                x + rx, y, z + rz, x - rx, y, z - rz,
                x - rx - ox, y + base, z - rz - oz, x + rx - ox, y + base, z + rz - oz,
                0.5F - width, offset - scroll, 0.5F + width, -scroll, r, g, b);
        streamQuad(consumer, matrix,
                x + rx, y + girth, z + rz, x - rx, y + girth, z - rz,
                x - rx - ox, y + base + girth, z - rz - oz, x + rx - ox, y + base + girth, z + rz - oz,
                0.5F - width, offset - scroll + 0.25F, 0.5F + width, -scroll + 0.25F, r, g, b);
        streamQuad(consumer, matrix,
                x + rx, y, z + rz, x + rx, y + girth, z + rz,
                x + rx - ox, y + base + girth, z + rz - oz, x + rx - ox, y + base, z + rz - oz,
                0.0F, offset - scroll + 0.75F, girth, -scroll + 0.75F, r, g, b);
        streamQuad(consumer, matrix,
                x - rx, y, z - rz, x - rx, y + girth, z - rz,
                x - rx - ox, y + base + girth, z - rz - oz, x - rx - ox, y + base, z - rz - oz,
                0.0F, offset - scroll + 0.75F, girth, -scroll + 0.75F, r, g, b);
    }

    private static void streamQuad(VertexConsumer consumer, Matrix4f matrix,
                                   float x1, float y1, float z1, float x2, float y2, float z2,
                                   float x3, float y3, float z3, float x4, float y4, float z4,
                                   float uMin, float vMax, float uMax, float vMin, int r, int g, int b) {
        float ux = x2 - x1;
        float uy = y2 - y1;
        float uz = z2 - z1;
        float vx = x3 - x1;
        float vy = y3 - y1;
        float vz = z3 - z1;
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        vertex(consumer, matrix, x1, y1, z1, uMax, vMax, r, g, b, nx, ny, nz);
        vertex(consumer, matrix, x2, y2, z2, uMin, vMax, r, g, b, nx, ny, nz);
        vertex(consumer, matrix, x3, y3, z3, uMin, vMin, r, g, b, nx, ny, nz);
        vertex(consumer, matrix, x4, y4, z4, uMax, vMin, r, g, b, nx, ny, nz);
    }

    private static float textureScroll() {
        return ((System.currentTimeMillis() / 100L) % 16L) / 16.0F;
    }

    private static int brighten(int channel) {
        return (int) (255.0D - (255.0D - channel) * 0.7D);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                               float u, float v, int r, int g, int b, float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }
}
