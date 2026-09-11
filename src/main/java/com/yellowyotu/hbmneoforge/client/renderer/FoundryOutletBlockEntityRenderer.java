package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.FoundryOutletBlock;
import com.yellowyotu.hbmneoforge.blockentity.FoundryBaseBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FoundryOutletBlockEntity;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FoundryOutletBlockEntityRenderer implements BlockEntityRenderer<FoundryOutletBlockEntity> {
    public FoundryOutletBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FoundryOutletBlockEntity outlet, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (outlet.getLevel() == null || outlet.getVisualTicks() <= 0 || outlet.getVisualMaterial().isBlank()) {
            return;
        }
        BlockPos target = outlet.getPourTargetPos(outlet.getVisualMaterial());
        if (target == null) {
            return;
        }
        float hitY = target.getY() + 1.0F;
        float length = Math.max(1.0F, outlet.getBlockPos().getY() - (float) (Math.ceil(hitY) - 0.875D));
        float maxLength = outlet.getBlockState().is(ModBlocks.FOUNDRY_SLAGTAP.get()) ? 15.0F : 4.0F;
        length = Math.min(length, maxLength);
        Direction direction = outlet.getBlockState().getValue(FoundryOutletBlock.FACING);
        FoundryRenderUtil.foundryStream(poseStack, buffers,
                FoundryMaterialRegistry.color(outlet.getVisualMaterial()), direction,
                0.5F - direction.getStepX() * 0.125F, 0.125F,
                0.5F - direction.getStepZ() * 0.125F,
                length, 0.0F, 0.375F, partialTick);
    }

}

