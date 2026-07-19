package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.AssemblyMachineBlock;
import com.yellowyotu.hbmneoforge.blockentity.AssemblyMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class AssemblyMachineBlockEntityRenderer implements BlockEntityRenderer<AssemblyMachineBlockEntity> {
    private static final double ROTATION_CENTER_X = 0.5D;
    private static final double ROTATION_CENTER_Z = 0.5D;

    private static final double DISPLAYED_ITEM_X = 0.5D;
    private static final double DISPLAYED_ITEM_Y = 0.5D;
    private static final double DISPLAYED_ITEM_Z = 0.5D;

    public AssemblyMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AssemblyMachineBlockEntity machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 0.0D, 0.5D);

        Direction facing = machine.getBlockState().getValue(AssemblyMachineBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        poseStack.translate(-0.5D, 0.0D, -0.5D);

        renderRotatingMechanism(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        renderDisplayedItem(machine, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void renderRotatingMechanism(AssemblyMachineBlockEntity machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(ROTATION_CENTER_X, 0.0D, ROTATION_CENTER_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) machine.getRing(partialTick)));
        poseStack.translate(-ROTATION_CENTER_X, 0.0D, -ROTATION_CENTER_Z);

        renderPart(machine, ModItems.ASSEMBLY_PART_RING.get(), poseStack, buffer, packedLight, packedOverlay);

        double[] arm1 = machine.getArmPositions(0, partialTick);
        double[] arm2 = machine.getArmPositions(1, partialTick);

        renderFirstArm(machine, arm1, poseStack, buffer, packedLight, packedOverlay);
        renderSecondArm(machine, arm2, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void renderFirstArm(AssemblyMachineBlockEntity machine, double[] arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 1.625D, 1.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[0]));
        poseStack.translate(-0.5D, -1.625D, -1.4375D);

        renderPart(machine, ModItems.ASSEMBLY_PART_ARM_LOWER_1.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.5D, 2.375D, 1.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[1]));
        poseStack.translate(-0.5D, -2.375D, -1.4375D);

        renderPart(machine, ModItems.ASSEMBLY_PART_ARM_UPPER_1.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.5D, 2.375D, 0.9375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) arm[2]));
        poseStack.translate(-0.5D, -2.375D, -0.9375D);

        renderPart(machine, ModItems.ASSEMBLY_PART_HEAD_1.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.0D, arm[3], 0.0D);

        renderPart(machine, ModItems.ASSEMBLY_PART_SPIKE_1.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void renderSecondArm(AssemblyMachineBlockEntity machine, double[] arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 1.625D, -0.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) -arm[0]));
        poseStack.translate(-0.5D, -1.625D, 0.4375D);

        renderPart(machine, ModItems.ASSEMBLY_PART_ARM_LOWER_2.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.5D, 2.375D, -0.4375D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) -arm[1]));
        poseStack.translate(-0.5D, -2.375D, 0.4375D);

        renderPart(machine, ModItems.ASSEMBLY_PART_ARM_UPPER_2.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.5D, 2.375D, 0.0625D);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) -arm[2]));
        poseStack.translate(-0.5D, -2.375D, -0.0625D);

        renderPart(machine, ModItems.ASSEMBLY_PART_HEAD_2.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.translate(0.0D, arm[3], 0.0D);

        renderPart(machine, ModItems.ASSEMBLY_PART_SPIKE_2.get(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void renderDisplayedItem(AssemblyMachineBlockEntity machine, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = machine.getDisplayedRecipeStack();

        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(DISPLAYED_ITEM_X, DISPLAYED_ITEM_Y, DISPLAYED_ITEM_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(1.25F, 1.25F, 1.25F);

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, machine.getLevel(), 0);

        poseStack.popPose();
    }

    private static void renderPart(AssemblyMachineBlockEntity machine, Item part, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // ItemRenderer.render(...) unconditionally applies poseStack.translate(-0.5, -0.5, -0.5)
        // after the display-context transform, for every ItemDisplayContext including NONE, in
        // order to re-center a block-space (0..1) baked model before drawing it. Our OBJ mesh
        // (Ring, arms, heads, spikes) is baked in raw block-space coordinates and all pivot math
        // above assumes those raw coordinates, so we cancel that hidden recenter here, local to
        // this single part's draw call, so it never leaks into the accumulated arm-chain matrix
        // used by the caller for the next joint in the hierarchy.
        poseStack.translate(0.5D, 0.5D, 0.5D);

        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(part), ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, machine.getLevel(), 0);

        poseStack.popPose();
    }
}