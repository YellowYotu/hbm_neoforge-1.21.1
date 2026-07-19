package com.yellowyotu.hbmneoforge.client;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.blockentity.BatterySocketBlockEntity;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, value = Dist.CLIENT)
public final class BatterySocketOverlay {
    private BatterySocketOverlay() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        BlockPos pos = hitResult.getBlockPos();
        BatterySocketBlockEntity socket = minecraft.level.getBlockEntity(pos) instanceof BatterySocketBlockEntity direct ? direct : null;
        if (socket == null) {
            BlockPos controller = BatterySocketDummyBlock.findController(minecraft.level, pos);
            if (controller != null && minecraft.level.getBlockEntity(controller) instanceof BatterySocketBlockEntity found) {
                socket = found;
            }
        }
        if (socket == null) {
            return;
        }
        int energy = socket.getEnergy();
        int percent = energy * 100 / ItemBatteryPack.CAPACITY;
        ChatFormatting color = percent >= 75 ? ChatFormatting.GREEN : percent >= 35 ? ChatFormatting.YELLOW : percent > 0 ? ChatFormatting.GOLD : ChatFormatting.RED;
        String batteryName = socket.getInventory().getStackInSlot(0).isEmpty() ? "Air" : "Redstone Battery";
        Component text = Component.literal(batteryName).withStyle(ChatFormatting.YELLOW).append(Component.literal("  " + energy + "/" + ItemBatteryPack.CAPACITY + "HE  ").withStyle(ChatFormatting.WHITE)).append(Component.literal("(" + percent + "%)").withStyle(color));
        minecraft.player.displayClientMessage(text, true);
    }
}
