package com.yellowyotu.hbmneoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.item.GasMaskItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class GasMaskOverlay {
    private GasMaskOverlay() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        ItemStack helmet = minecraft.player.getItemBySlot(EquipmentSlot.HEAD);
        ResourceLocation texture;
        if (helmet.is(ModItems.HAZMAT_HELMET.get())) {
            texture = ResourceLocation.fromNamespaceAndPath(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                    "textures/misc/overlay_hazmat.png");
        } else if (helmet.getItem() instanceof GasMaskItem mask) {
            GasMaskItem.MaskType type = mask.getMaskType();
            if (type != GasMaskItem.MaskType.GAS_MASK && type != GasMaskItem.MaskType.M65) {
                return;
            }

            int damageStage = Math.min(
                    5,
                    (int) ((double) helmet.getDamageValue() / Math.max(1, helmet.getMaxDamage()) * 6.0D));
            String base = type == GasMaskItem.MaskType.M65 ? "overlay_goggles_" : "overlay_gasmask_";
            texture = ResourceLocation.fromNamespaceAndPath(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID,
                    "textures/misc/" + base + damageStage + ".png");
        } else {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(
                texture,
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                0.0F,
                0.0F,
                256,
                256,
                256,
                256);
        RenderSystem.disableBlend();
    }
}
