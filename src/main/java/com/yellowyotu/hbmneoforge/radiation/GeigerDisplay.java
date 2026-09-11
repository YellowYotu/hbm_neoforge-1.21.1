package com.yellowyotu.hbmneoforge.radiation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class GeigerDisplay {

    private GeigerDisplay() {
    }

    public static void printGeigerData(Player player) {
        double playerRadiation = truncate1(RadiationSystem.getRadiation(player));
        double chunkRadiation = truncate1(ChunkRadiationManager.getRadiation(player.level(), player.blockPosition()));
        double environmentRadiation = truncate1(RadiationSystem.getEnvironmentRadiationBuffer(player));

        double resistanceModifier = RadiationResistance.calculateRadiationModifier(player);
        double protectionPercent = ((int) (10000.0D - resistanceModifier * 10000.0D)) / 100.0D;
        double resistance = ((int) (RadiationResistance.getResistance(player) * 100.0D)) / 100.0D;

        player.sendSystemMessage(Component.literal("===== ☢ ").append(Component.translatable("geiger.title")).append(Component.literal(" ☢ =====")).withStyle(ChatFormatting.GOLD));

        player.sendSystemMessage(Component.translatable("geiger.chunkRad").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" " + chunkRadiation + " RAD/s").withStyle(getRadiationColor(chunkRadiation))));

        player.sendSystemMessage(Component.translatable("geiger.envRad").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" " + environmentRadiation + " RAD/s").withStyle(getRadiationColor(environmentRadiation))));

        player.sendSystemMessage(Component.translatable("geiger.playerRad").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" " + playerRadiation + " RAD").withStyle(getPlayerRadiationColor(playerRadiation))));

        player.sendSystemMessage(Component.translatable("geiger.playerRes").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" " + protectionPercent + "% (" + resistance + ")").withStyle(resistance > 0.0D ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
    }

    private static double truncate1(double value) {
        return ((int) (value * 10.0D)) / 10.0D;
    }

    private static ChatFormatting getRadiationColor(double radiation) {
        if (radiation == 0.0D) {
            return ChatFormatting.GREEN;
        }

        if (radiation < 1.0D) {
            return ChatFormatting.YELLOW;
        }

        if (radiation < 10.0D) {
            return ChatFormatting.GOLD;
        }

        if (radiation < 100.0D) {
            return ChatFormatting.RED;
        }

        if (radiation < 1000.0D) {
            return ChatFormatting.DARK_RED;
        }

        return ChatFormatting.DARK_GRAY;
    }

    private static ChatFormatting getPlayerRadiationColor(double radiation) {
        if (radiation < 200.0D) {
            return ChatFormatting.GREEN;
        }

        if (radiation < 400.0D) {
            return ChatFormatting.YELLOW;
        }

        if (radiation < 600.0D) {
            return ChatFormatting.GOLD;
        }

        if (radiation < 800.0D) {
            return ChatFormatting.RED;
        }

        if (radiation < 1000.0D) {
            return ChatFormatting.DARK_RED;
        }

        return ChatFormatting.DARK_GRAY;
    }
}