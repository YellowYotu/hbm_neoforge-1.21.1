package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yellowyotu.hbmneoforge.block.FoundryChannelBlock;
import com.yellowyotu.hbmneoforge.blockentity.FoundryChannelBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public final class FoundryChannelBlockEntityRenderer implements BlockEntityRenderer<FoundryChannelBlockEntity> {
    public FoundryChannelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FoundryChannelBlockEntity channel, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (channel.getAmount() <= 0 || channel.getMaterial().isBlank()) {
            return;
        }
        float level = Math.min(0.371F, (float) (0.125D + channel.getAmount() * 0.25D / channel.getCapacity()));
        int color = channel.getMoltenColor();
        BlockState state = channel.getBlockState();
        FoundryRenderUtil.channelLiquid(poseStack, buffers, color, 0.375F, 0.125F, 0.375F, 0.625F, level, 0.625F);
        if (state.getValue(FoundryChannelBlock.EAST)) {
            FoundryRenderUtil.channelLiquid(poseStack, buffers, color, 0.625F, 0.125F, 0.3125F, 1.0F, level, 0.6875F);
        }
        if (state.getValue(FoundryChannelBlock.WEST)) {
            FoundryRenderUtil.channelLiquid(poseStack, buffers, color, 0.0F, 0.125F, 0.3125F, 0.375F, level, 0.6875F);
        }
        if (state.getValue(FoundryChannelBlock.SOUTH)) {
            FoundryRenderUtil.channelLiquid(poseStack, buffers, color, 0.3125F, 0.125F, 0.625F, 0.6875F, level, 1.0F);
        }
        if (state.getValue(FoundryChannelBlock.NORTH)) {
            FoundryRenderUtil.channelLiquid(poseStack, buffers, color, 0.3125F, 0.125F, 0.0F, 0.6875F, level, 0.375F);
        }
    }
}
