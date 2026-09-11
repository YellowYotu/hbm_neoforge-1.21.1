package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yellowyotu.hbmneoforge.blockentity.FoundryCastingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class FoundryCastingBlockEntityRenderer implements BlockEntityRenderer<FoundryCastingBlockEntity> {
    public FoundryCastingBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FoundryCastingBlockEntity casting, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack mold = casting.getInventory().getStackInSlot(0);
        if (!mold.isEmpty()) {
            renderItem(casting, mold, 0.13D, poseStack, buffers, packedLight, packedOverlay);
        }
        ItemStack output = casting.getInventory().getStackInSlot(1);
        if (!output.isEmpty()) {
            renderItem(casting, output, casting.isBasin() ? 0.875D : 0.25D, poseStack, buffers, packedLight, packedOverlay);
        }
        if (casting.getAmount() <= 0 || casting.getCapacity() <= 0 || casting.getMaterial().isBlank()) {
            return;
        }
        float liquidY = (float) (0.125D + casting.getAmount() * (casting.isBasin() ? 0.75D : 0.25D) / casting.getCapacity());
        FoundryRenderUtil.top(poseStack, buffers, FoundryRenderUtil.MOLTEN_TEXTURE, casting.getMoltenColor(),
                0.125F, liquidY, 0.125F, 0.875F, 0.875F);
    }

    private static void renderItem(FoundryCastingBlockEntity casting, ItemStack stack, double height,
                                   PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, height, 0.5D);
        if (!(stack.getItem() instanceof BlockItem)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
        } else {
            poseStack.translate(0.0D, -0.352D, 0.0D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(1.5F, 1.5F, 1.5F);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack.copyWithCount(1), ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, buffers, casting.getLevel(), 0);
        poseStack.popPose();
    }
}
