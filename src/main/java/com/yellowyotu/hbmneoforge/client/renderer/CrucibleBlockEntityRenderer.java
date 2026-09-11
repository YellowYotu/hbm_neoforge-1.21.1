package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yellowyotu.hbmneoforge.block.CrucibleBlock;
import com.yellowyotu.hbmneoforge.blockentity.CrucibleBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FoundryBaseBlockEntity;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    public CrucibleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrucibleBlockEntity crucible, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        int total = crucible.getMoltenAmount();
        if (total > 0) {
            float level = (float) (0.5D + ((double) total / (CrucibleBlockEntity.RECIPE_CAPACITY + CrucibleBlockEntity.WASTE_CAPACITY)) * 0.875D);
            FoundryRenderUtil.top(poseStack, buffers, FoundryRenderUtil.CRUCIBLE_TEXTURE,
                    crucible.getPrimaryMoltenColor(), -0.5F, level, -0.5F, 1.5F, 1.5F);
        }
        if (crucible.getLevel() == null || !crucible.getBlockState().hasProperty(CrucibleBlock.FACING)) {
            return;
        }
        Direction front = crucible.getBlockState().getValue(CrucibleBlock.FACING);
        renderStream(crucible, front, crucible.getRecipeStack(), poseStack, buffers);
        renderStream(crucible, front.getOpposite(), crucible.getWasteStack(), poseStack, buffers);
    }

    private static void renderStream(CrucibleBlockEntity crucible, Direction direction, Map<String, Integer> stack,
                                     PoseStack poseStack, MultiBufferSource buffers) {
        if (stack.isEmpty()) {
            return;
        }
        String material = stack.keySet().iterator().next();
        BlockPos mouth = crucible.getBlockPos().relative(direction, 2);
        BlockPos hit = findTarget(crucible, mouth, material);
        if (hit == null) {
            return;
        }
        int color = FoundryMaterialRegistry.color(material);
        float x = 0.5F + direction.getStepX() * 1.875F;
        float z = 0.5F + direction.getStepZ() * 1.875F;
        float hitY = hit.getY() + 1.0F;
        float length = Math.max(1.0F, crucible.getBlockPos().getY() - (float) (Math.ceil(hitY) - 0.875D));
        length = Math.min(length, 6.0F);
        FoundryRenderUtil.foundryStream(poseStack, buffers, color, direction, x, 0.0F, z,
                length, 0.625F, 0.625F, 0.0F);
    }

    private static BlockPos findTarget(CrucibleBlockEntity crucible, BlockPos mouth, String material) {
        for (int distance = 0; distance <= 6; distance++) {
            BlockPos pos = mouth.below(distance);
            BlockEntity entity = crucible.getLevel().getBlockEntity(pos);
            if (entity instanceof com.yellowyotu.hbmneoforge.blockentity.FoundryCastingBlockEntity casting) {
                return casting.canReceivePour(Direction.UP, material) ? pos : null;
            }
            if (entity instanceof com.yellowyotu.hbmneoforge.blockentity.FoundryTankBlockEntity tank) {
                return tank.canReceiveFrom(Direction.UP, material) ? pos : null;
            }
            if (entity instanceof FoundryBaseBlockEntity foundry) {
                return foundry.canReceiveFrom(Direction.UP, material) ? pos : null;
            }
            if (!crucible.getLevel().getBlockState(pos).isAir()) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean shouldRenderOffScreen(CrucibleBlockEntity blockEntity) {
        return true;
    }
}
