package com.yellowyotu.hbmneoforge.client;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.blockentity.AirIntakeBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.AirIntakeDummyBlockEntity;
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
public final class AirIntakeOverlay {
    private AirIntakeOverlay() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }
        BlockPos pos = hitResult.getBlockPos();
        AirIntakeBlockEntity intake = minecraft.level.getBlockEntity(pos) instanceof AirIntakeBlockEntity direct ? direct : null;
        if (intake == null && minecraft.level.getBlockEntity(pos) instanceof AirIntakeDummyBlockEntity dummy) {
            intake = dummy.getCore();
        }
        if (intake == null) {
            return;
        }
        ChatFormatting powerColor = intake.getPower() < AirIntakeBlockEntity.POWER_PER_TICK ? ChatFormatting.RED : ChatFormatting.GREEN;
        Component text = Component.literal("Air Intake  ").withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("Power: " + intake.getPower() + "HE  ").withStyle(powerColor))
            .append(Component.literal("<- Compressed Air: " + intake.getAir() + "/" + AirIntakeBlockEntity.AIR_CAPACITY + "mB").withStyle(ChatFormatting.WHITE));
        minecraft.player.displayClientMessage(text, true);
    }
}
