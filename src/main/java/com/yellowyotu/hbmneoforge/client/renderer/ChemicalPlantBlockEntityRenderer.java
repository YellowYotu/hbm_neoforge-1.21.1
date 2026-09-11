package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.client.sound.MachineLoopSoundManager;
import com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock;
import com.yellowyotu.hbmneoforge.blockentity.ChemicalPlantBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public final class ChemicalPlantBlockEntityRenderer implements BlockEntityRenderer<ChemicalPlantBlockEntity> {
    private static final Map<ChemicalPlantBlockEntity, AnimationState> ANIMATION = new WeakHashMap<>();

    public ChemicalPlantBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChemicalPlantBlockEntity machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MachineLoopSoundManager.update(machine, machine.isProcessing(), ModSounds.CHEMICAL_PLANT.get(), 1.0F, machine::isProcessing);
        AnimationState animation = ANIMATION.computeIfAbsent(machine, ignored -> new AnimationState());
        animation.tick(machine.isProcessing(), partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        Direction facing = machine.getBlockState().getValue(ChemicalPlantBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        renderPart(machine, ModItems.CHEMICAL_PART_BASE.get(), poseStack, buffer, packedLight, packedOverlay);
        if (machine.getBlockState().getValue(ChemicalPlantBlock.FRAME)) {
            renderPart(machine, ModItems.CHEMICAL_PART_FRAME.get(), poseStack, buffer, packedLight, packedOverlay);
        }

        poseStack.pushPose();
        poseStack.translate(sps(animation.value * 0.125D) * 0.375D, 0.0D, 0.0D);
        renderPart(machine, ModItems.CHEMICAL_PART_SLIDER.get(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(1.0D, 0.0D, 1.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) ((animation.value * 15.0D) % 360.0D)));
        poseStack.translate(-1.0D, 0.0D, -1.0D);
        renderPart(machine, ModItems.CHEMICAL_PART_SPINNER.get(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ChemicalPlantBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static double sps(double value) {
        double wrapped = value - Math.floor(value);
        return Math.sin(wrapped * Math.PI * 2.0D);
    }

    private static void renderPart(ChemicalPlantBlockEntity machine, Item part, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(part), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, machine.getLevel(), 0);
        poseStack.popPose();
    }

    private static final class AnimationState {
        private double value;
        private long lastGameTime = Long.MIN_VALUE;

        private void tick(boolean processing, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            long gameTime = minecraft.level.getGameTime();
            if (lastGameTime == Long.MIN_VALUE) {
                lastGameTime = gameTime;
            }
            if (processing && gameTime > lastGameTime) {
                value += gameTime - lastGameTime;
            }
            lastGameTime = gameTime;
            if (processing) {
                value += partialTick / 20.0D;
            }
        }
    }
}
