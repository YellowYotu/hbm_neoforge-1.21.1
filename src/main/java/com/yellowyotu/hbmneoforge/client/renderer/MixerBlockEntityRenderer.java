package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.MixerBlock;
import com.yellowyotu.hbmneoforge.blockentity.MixerBlockEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MixerBlockEntityRenderer implements BlockEntityRenderer<MixerBlockEntity> {
    private static final Map<MixerBlockEntity, AnimationState> ANIMATION = new WeakHashMap<>();

    public MixerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MixerBlockEntity machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RenderSystem.disableCull();
        try {
                AnimationState animation = ANIMATION.computeIfAbsent(machine, ignored -> new AnimationState());
                float rotation = animation.getRotation(machine.isProcessing(), partialTick);

                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                Direction facing = machine.getBlockState().getValue(MixerBlock.FACING);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
                poseStack.translate(-0.5D, 0.0D, -0.5D);

                renderPart(machine, ModItems.MIXER_PART_MAIN.get(), poseStack, buffer, packedLight, packedOverlay);

                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                renderPart(machine, ModItems.MIXER_PART_ROTOR.get(), poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                int totalFill = machine.getTotalFluidAmount();
                int totalCapacity = machine.getTotalFluidCapacity();
                if (totalFill > 0 && totalCapacity > 0 && machine.getRenderedFluidType() != null) {
                    if (buffer instanceof MultiBufferSource.BufferSource source) {
                        source.endBatch();
                    }
                    float fill = Math.min(0.99F, (float) totalFill / (float) totalCapacity * 0.99F);
                    int color = machine.getRenderedFluidType().color();
                    float red = ((color >> 16) & 0xFF) / 255.0F;
                    float green = ((color >> 8) & 0xFF) / 255.0F;
                    float blue = (color & 0xFF) / 255.0F;
                    poseStack.pushPose();
                    poseStack.translate(0.0D, 1.0D, 0.0D);
                    poseStack.scale(1.0F, fill, 1.0F);
                    poseStack.translate(0.0D, -1.0D, 0.0D);
                    RenderSystem.setShaderColor(red, green, blue, 0.75F);
                    renderPart(machine, ModItems.MIXER_PART_FLUID.get(), poseStack, buffer, packedLight, packedOverlay);
                    if (buffer instanceof MultiBufferSource.BufferSource source) {
                        source.endBatch();
                    }
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    poseStack.popPose();
                }

                poseStack.popPose();
    
        } finally {
            RenderSystem.enableCull();
        }
    }

    private static void renderPart(MixerBlockEntity machine, Item part, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(part), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, machine.getLevel(), 0);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MixerBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(MixerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static final class AnimationState {
        private double rotation;
        private long lastGameTime = Long.MIN_VALUE;

        private float getRotation(boolean processing, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return (float) rotation;
            }
            long gameTime = minecraft.level.getGameTime();
            if (lastGameTime == Long.MIN_VALUE) {
                lastGameTime = gameTime;
            }
            if (processing && gameTime > lastGameTime) {
                rotation = (rotation + (gameTime - lastGameTime) * 20.0D) % 360.0D;
            }
            lastGameTime = gameTime;
            return (float) ((rotation + (processing ? partialTick * 20.0D : 0.0D)) % 360.0D);
        }
    }
}
