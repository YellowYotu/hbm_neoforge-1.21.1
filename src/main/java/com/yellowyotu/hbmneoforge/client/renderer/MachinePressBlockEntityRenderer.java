package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.blockentity.MachinePressBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MachinePressBlockEntityRenderer
        implements BlockEntityRenderer<MachinePressBlockEntity> {

    private static final float MAX_HEAD_OFFSET = 0.875F;

    private final BlockRenderDispatcher blockRenderer;

    public MachinePressBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.blockRenderer =
                context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            MachinePressBlockEntity press,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        float progress =
                press.getInterpolatedPressProgress(partialTick)
                        / MachinePressBlockEntity.MAX_PRESS;

        progress =
                Math.max(0.0F, Math.min(1.0F, progress));

        /*
         * В оригинале:
         *
         * (1 - progress) * 0.875
         *
         * progress 0   → головка наверху;
         * progress 1   → головка полностью опущена.
         */
        float yOffset =
                (1.0F - progress) * MAX_HEAD_OFFSET;

        poseStack.pushPose();

        poseStack.translate(
                0.0F,
                yOffset,
                0.0F
        );

        blockRenderer.renderSingleBlock(
                ModBlocks.MACHINE_PRESS_HEAD_RENDER
                        .get()
                        .defaultBlockState(),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();

        ItemStack input = press.getInventory().getStackInSlot(MachinePressBlockEntity.SLOT_INPUT);
        ItemStack stamp = press.getInventory().getStackInSlot(MachinePressBlockEntity.SLOT_STAMP);
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.58D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            Minecraft.getInstance().getItemRenderer().renderStatic(input, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, press.getLevel(), 0);
            poseStack.popPose();
        }
        if (!stamp.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.38D + yOffset, 0.5D);
            poseStack.scale(0.45F, 0.45F, 0.45F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stamp, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, press.getLevel(), 0);
            poseStack.popPose();
        }
    }
}