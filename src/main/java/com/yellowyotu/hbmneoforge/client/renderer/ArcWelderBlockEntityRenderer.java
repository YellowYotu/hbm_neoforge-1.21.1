package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.ArcWelderBlock;
import com.yellowyotu.hbmneoforge.blockentity.ArcWelderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ArcWelderBlockEntityRenderer implements BlockEntityRenderer<ArcWelderBlockEntity> {
    public ArcWelderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcWelderBlockEntity welder, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RenderSystem.disableCull();
        try {
                Direction facing = welder.getBlockState().getValue(ArcWelderBlock.FACING);
                Direction left = facing.getCounterClockWise();
                Direction right = facing.getClockWise();
                double offsetX = -left.getStepX() * 0.5D + facing.getStepX() * 0.5D;
                double offsetZ = -left.getStepZ() * 0.5D + facing.getStepZ() * 0.5D;

                poseStack.pushPose();
                poseStack.translate(0.5D + offsetX, 0.5D, 0.5D + offsetZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
                poseStack.translate(-0.5D, 0.0D, 0.0D);
                Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ModItems.ARC_WELDER_PART.get()), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, welder.getLevel(), 0);
                poseStack.popPose();

                ItemStack display = welder.getDisplayedStack();
                if (!display.isEmpty()) {
                    double displayOffsetX = right.getStepX() * 2.5D + facing.getStepX() * 0.5D + left.getStepX() * 3.0D - facing.getStepX();
                    double displayOffsetZ = right.getStepZ() * 2.5D + facing.getStepZ() * 0.5D + left.getStepZ() * 3.0D - facing.getStepZ();

                    poseStack.pushPose();
                    poseStack.translate(0.5D + offsetX + displayOffsetX, 0.0D, 0.5D + offsetZ + displayOffsetZ);
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
                    poseStack.translate(-0.5D, 0.0D, 0.0D);
                    poseStack.translate(0.0D, 1.125D, 0.0D);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.scale(0.76923075F, 0.76923075F, 0.76923075F);
                    Minecraft.getInstance().getItemRenderer().renderStatic(display.copyWithCount(1), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, welder.getLevel(), 0);
                    poseStack.popPose();
                }
    
        } finally {
            RenderSystem.enableCull();
        }
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
    public AABB getRenderBoundingBox(ArcWelderBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(ArcWelderBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
