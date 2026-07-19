package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SolderingStationBlockEntityRenderer implements BlockEntityRenderer<SolderingStationBlockEntity> {

    public SolderingStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SolderingStationBlockEntity station, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = station.getDisplayedStack();
        if (stack.isEmpty()) {
            return;
        }

        Direction facing = station.getBlockState().getValue(SolderingStationBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
        poseStack.translate(-0.5D, 0.0D, 0.5D);
        poseStack.translate(0.0D, 1.125D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(0.76923075F, 0.76923075F, 0.76923075F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack.copyWithCount(1), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, station.getLevel(), 0);
        poseStack.popPose();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            case EAST -> 0.0F;
            default -> 0.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(SolderingStationBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
