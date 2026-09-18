package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.OilDerrickBlock;
import com.yellowyotu.hbmneoforge.blockentity.OilDerrickBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class OilDerrickBlockEntityRenderer implements BlockEntityRenderer<OilDerrickBlockEntity> {
    public OilDerrickBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            OilDerrickBlockEntity derrick,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        RenderSystem.disableCull();

        try {
            Direction facing = derrick.getBlockState().getValue(OilDerrickBlock.FACING);
            Direction visualRight = facing.getCounterClockWise();

            poseStack.pushPose();
            poseStack.translate(
                    0.5D + visualRight.getStepX() * 0.5D + facing.getStepX() * 0.5D,
                    0.5D,
                    0.5D + visualRight.getStepZ() * 0.5D + facing.getStepZ() * 0.5D
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    new ItemStack(ModItems.OIL_DERRICK_PART.get()),
                    ItemDisplayContext.NONE,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    derrick.getLevel(),
                    0
            );
            poseStack.popPose();
        } finally {
            RenderSystem.enableCull();
        }
    }

    private static float rotationFor(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public AABB getRenderBoundingBox(OilDerrickBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(OilDerrickBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
