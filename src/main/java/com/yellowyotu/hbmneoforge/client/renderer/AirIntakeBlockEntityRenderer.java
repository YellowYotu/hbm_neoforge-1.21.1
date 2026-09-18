package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.AirIntakeBlock;
import com.yellowyotu.hbmneoforge.blockentity.AirIntakeBlockEntity;
import com.yellowyotu.hbmneoforge.client.sound.MachineLoopSoundManager;
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

public final class AirIntakeBlockEntityRenderer implements BlockEntityRenderer<AirIntakeBlockEntity> {
    private static final Map<AirIntakeBlockEntity, AnimationState> ANIMATION = new WeakHashMap<>();

    public AirIntakeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AirIntakeBlockEntity intake, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RenderSystem.disableCull();
        try {
                MachineLoopSoundManager.update(intake, intake.isActive(), ModSounds.MOTOR.get(), 0.25F, intake::isActive);
                float fanRotation = ANIMATION.computeIfAbsent(intake, ignored -> new AnimationState()).getRotation(intake.isActive(), partialTick);

                Direction facing = intake.getBlockState().getValue(AirIntakeBlock.FACING);
                Direction left = facing.getCounterClockWise();

                double offsetX = -left.getStepX() * 0.5D + facing.getStepX() * 0.5D;
                double offsetZ = -left.getStepZ() * 0.5D + facing.getStepZ() * 0.5D;

                poseStack.pushPose();
                poseStack.translate(1.0D + offsetX, 0.5D, 1.0D + offsetZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationFor(facing)));

                renderPart(intake, ModItems.AIR_INTAKE_PART_BASE.get(), poseStack, buffer, packedLight, packedOverlay);

                poseStack.pushPose();
                poseStack.translate(-0.5D, 0.0D, -0.5D);
                poseStack.mulPose(Axis.YP.rotationDegrees(-fanRotation));
                poseStack.translate(0.5D, 0.0D, 0.5D);
                renderPart(intake, ModItems.AIR_INTAKE_PART_FAN.get(), poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();

                poseStack.popPose();
    
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

    private static void renderPart(AirIntakeBlockEntity intake, Item part, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(part), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, intake.getLevel(), 0);
    }

    @Override
    public AABB getRenderBoundingBox(AirIntakeBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(AirIntakeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static final class AnimationState {
        private double rotation;
        private long lastGameTime = Long.MIN_VALUE;

        private float getRotation(boolean active, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return (float) rotation;
            }
            long gameTime = minecraft.level.getGameTime();
            if (lastGameTime == Long.MIN_VALUE) {
                lastGameTime = gameTime;
            }
            if (active && gameTime > lastGameTime) {
                rotation = (rotation + (gameTime - lastGameTime) * 45.0D) % 360.0D;
            }
            lastGameTime = gameTime;
            return (float) ((rotation + (active ? partialTick * 45.0D : 0.0D)) % 360.0D);
        }
    }
}
