package com.yellowyotu.hbmneoforge.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.blockentity.BatterySocketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class BatterySocketBlockEntityRenderer implements BlockEntityRenderer<BatterySocketBlockEntity> {
    public BatterySocketBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BatterySocketBlockEntity socket, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RenderSystem.disableCull();
        try {
                ItemStack battery = socket.getInventory().getStackInSlot(0);
                if (battery.isEmpty()) {
                    return;
                }
                ItemStack renderedBattery = battery;
                if (ItemBatteryPack.isSelfCharging(battery)) {
                    renderedBattery = new ItemStack(ModItems.BATTERY_SC_SOCKET_DISPLAY.get());
                } else if (ItemBatteryPack.isInfinite(battery)) {
                    renderedBattery = new ItemStack(ModItems.BATTERY_INFINITE_SOCKET_DISPLAY.get());
                }
                poseStack.pushPose();

                // ItemRenderer.render(...) unconditionally applies poseStack.translate(-0.5, -0.5, -0.5)
                // internally (see AssemblyMachineBlockEntityRenderer for the full explanation). Canceling
                // it restores the model to its raw OBJ coordinates. battery_pack.obj's "Battery" part uses
                // the exact same coordinates as the "Battery" cavity in the static battery_socket.obj frame
                // (verified byte-for-byte identical), so no further offset is needed — it already nests
                // perfectly into the socket once this internal shift is canceled.
                poseStack.translate(1.0D, 1.0D, 1.0D);

                Minecraft.getInstance().getItemRenderer().renderStatic(renderedBattery, ItemDisplayContext.NONE, packedLight, packedOverlay, poseStack, buffer, socket.getLevel(), 0);
                poseStack.popPose();
    
        } finally {
            RenderSystem.enableCull();
        }
    }

    @Override
    public AABB getRenderBoundingBox(BatterySocketBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public boolean shouldRenderOffScreen(BatterySocketBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}