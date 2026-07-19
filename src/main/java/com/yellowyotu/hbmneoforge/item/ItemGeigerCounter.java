package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.GeigerSounds;
import com.yellowyotu.hbmneoforge.radiation.RadiationResistance;
import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemGeigerCounter extends Item {

    public ItemGeigerCounter(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player) || level.isClientSide() || level.getGameTime() % 5L != 0L) {
            return;
        }

        float radiation = RadiationSystem.getEnvironmentRadiationBuffer(player);
        if (radiation > 0.00001F) {
            SoundEvent sound = GeigerSounds.select(radiation, player.getRandom());
            if (sound != null) {
                level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        } else if (player.getRandom().nextInt(50) == 0) {
            level.playSound(null, player.blockPosition(), ModSounds.GEIGER_1.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            float chunkRadiation = ChunkRadiationManager.getRadiation(level, player.blockPosition());
            float environmentRadiation = RadiationSystem.getEnvironmentRadiationBuffer(player);
            float playerRadiation = RadiationSystem.getRadiation(player);
            float resistance = RadiationResistance.getResistance(player);
            float protectionPercent = RadiationResistance.getProtectionPercent(player);

            player.sendSystemMessage(Component.literal("===== ☢ GEIGER COUNTER ☢ =====").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Chunk radiation: " + format(chunkRadiation) + " RAD/s").withStyle(colorForRate(chunkRadiation)));
            player.sendSystemMessage(Component.literal("Environmental radiation: " + format(environmentRadiation) + " RAD/s").withStyle(colorForRate(environmentRadiation)));
            player.sendSystemMessage(Component.literal("Player dose: " + format(playerRadiation) + " RAD").withStyle(colorForDose(playerRadiation)));
            player.sendSystemMessage(Component.literal("Radiation resistance: " + format(protectionPercent) + "% (" + format(resistance) + ")").withStyle(resistance > 0.0F ? ChatFormatting.GREEN : ChatFormatting.WHITE));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static ChatFormatting colorForRate(float radiation) {
        if (radiation == 0.0F) {
            return ChatFormatting.GREEN;
        }
        if (radiation < 1.0F) {
            return ChatFormatting.YELLOW;
        }
        if (radiation < 10.0F) {
            return ChatFormatting.GOLD;
        }
        if (radiation < 100.0F) {
            return ChatFormatting.RED;
        }
        if (radiation < 1000.0F) {
            return ChatFormatting.DARK_RED;
        }
        return ChatFormatting.DARK_GRAY;
    }

    private static ChatFormatting colorForDose(float radiation) {
        if (radiation < 200.0F) {
            return ChatFormatting.GREEN;
        }
        if (radiation < 400.0F) {
            return ChatFormatting.YELLOW;
        }
        if (radiation < 600.0F) {
            return ChatFormatting.GOLD;
        }
        if (radiation < 800.0F) {
            return ChatFormatting.RED;
        }
        if (radiation < 1000.0F) {
            return ChatFormatting.DARK_RED;
        }
        return ChatFormatting.DARK_GRAY;
    }

    private static String format(float value) {
        return String.valueOf(Math.floor(value * 10.0F) / 10.0F);
    }
}
