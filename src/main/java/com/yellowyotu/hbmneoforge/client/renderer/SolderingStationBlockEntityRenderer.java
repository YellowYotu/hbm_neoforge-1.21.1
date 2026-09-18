package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.blockentity.SolderingStationBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SolderingStationBlockEntityRenderer implements BlockEntityRenderer<SolderingStationBlockEntity> {
    private static final double MODEL_CENTER_X = 1.0D;
    private static final double MODEL_CENTER_Z = 1.0D;
    private static final double DISPLAY_X = -0.34375D;
    private static final double DISPLAY_Y = 1.125D;
    private static final double DISPLAY_Z = 0.5D;

    public SolderingStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SolderingStationBlockEntity station, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        RenderSystem.disableCull();
        try {
                Direction facing = station.getBlockState().getValue(SolderingStationBlock.FACING);

                poseStack.pushPose();
                poseStack.translate(MODEL_CENTER_X, 0.0D, MODEL_CENTER_Z);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
                renderStationModel(station, poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.popPose();

                renderDisplayedItem(station, poseStack, bufferSource, packedLight, packedOverlay);
    
        } finally {
            RenderSystem.enableCull();
        }
    }

    private static void renderStationModel(SolderingStationBlockEntity station, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.translate(-0.5D, 0.25D, -0.5D);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ModBlocks.SOLDERING_STATION.get()), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, bufferSource, station.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderDisplayedItem(SolderingStationBlockEntity station, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = station.getDisplayedStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(1.0D, DISPLAY_Y, 1.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(0.76923075F, 0.76923075F, 0.76923075F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack.copyWithCount(1), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, bufferSource, station.getLevel(), 0);
        poseStack.popPose();
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    @Override
    public AABB getRenderBoundingBox(SolderingStationBlockEntity blockEntity) {
        return AABB.INFINITE;
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
