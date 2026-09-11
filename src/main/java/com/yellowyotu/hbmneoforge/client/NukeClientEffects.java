package com.yellowyotu.hbmneoforge.client;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, value = Dist.CLIENT)
public final class NukeClientEffects {
    private static final long FLASH_DURATION_MS = 5_000L;
    private static final long SHAKE_DURATION_MS = 1_500L;
    private static long flashStart;
    private static long shakeStart;

    private NukeClientEffects() {
    }

    public static void startFlash() {
        flashStart = System.currentTimeMillis();
    }

    public static void startShake() {
        shakeStart = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void renderFlash(RenderGuiEvent.Post event) {
        long remaining = flashStart + FLASH_DURATION_MS - System.currentTimeMillis();
        if (remaining <= 0L) {
            return;
        }

        float brightness = Math.min(1.0F, remaining / (float) FLASH_DURATION_MS);
        int alpha = Math.min(255, Math.max(0, (int) (brightness * 255.0F)));
        int color = alpha << 24 | 0xFFFFFF;
        event.getGuiGraphics().fill(0, 0, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), color);
    }

    @SubscribeEvent
    public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
        long remaining = shakeStart + SHAKE_DURATION_MS - System.currentTimeMillis();
        if (remaining <= 0L || Minecraft.getInstance().player == null) {
            return;
        }

        double strength = remaining / (double) SHAKE_DURATION_MS;
        double time = System.currentTimeMillis();
        float yaw = (float) (Math.sin(time * 0.020D) * 4.0D * strength);
        float pitch = (float) (Math.sin(time * 0.031D + 1.7D) * 3.0D * strength);
        float roll = (float) (Math.sin(time * 0.017D + 0.8D) * 5.0D * strength);
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
    }
}
