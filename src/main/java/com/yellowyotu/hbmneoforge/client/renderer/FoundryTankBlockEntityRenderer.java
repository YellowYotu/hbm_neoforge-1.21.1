package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yellowyotu.hbmneoforge.blockentity.FoundryTankBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;

public final class FoundryTankBlockEntityRenderer implements BlockEntityRenderer<FoundryTankBlockEntity> {
    public FoundryTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FoundryTankBlockEntity tank, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (tank.getLevel() == null || tank.getAmount() <= 0 || tank.getMaterial().isBlank()) {
            return;
        }
        BlockPos pos = tank.getBlockPos();
        boolean below = tank.getLevel().getBlockEntity(pos.below()) instanceof FoundryTankBlockEntity;
        boolean above = tank.getLevel().getBlockEntity(pos.above()) instanceof FoundryTankBlockEntity;
        float bottom = below ? 0.0F : 0.125F;
        float max = 0.75F + (below ? 0.125F : 0.0F) + (above ? 0.125F : 0.0F);
        float top = bottom + tank.getAmount() * max / tank.getCapacity();
        FoundryRenderUtil.top(
                poseStack,
                buffers,
                FoundryRenderUtil.MOLTEN_TEXTURE,
                tank.getMoltenColor(),
                0.001F,
                top,
                0.001F,
                0.999F,
                0.999F);
    }
}
