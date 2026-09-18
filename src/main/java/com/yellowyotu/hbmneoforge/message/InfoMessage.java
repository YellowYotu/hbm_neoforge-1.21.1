package com.yellowyotu.hbmneoforge.message;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID)
public final class InfoMessage {

    private static final String GITHUB_URL =
            "https://github.com/YellowYotu/hbm_neoforge-1.21.1";

    private static final String CURSEFORGE_URL =
            "https://www.curseforge.com/minecraft/mc-mods/hbms-nuclear-tech-unofficial-neoforge-edition";

    private static final String DISCORD_URL =
            "https://discord.gg/A9NK8xypwU";

    private InfoMessage() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.sendSystemMessage(Component.empty());

        player.sendSystemMessage(
                Component.literal("====================================================")
                        .withStyle(ChatFormatting.DARK_RED)
        );

        player.sendSystemMessage(
                Component.literal("HBM's Nuclear Tech - Unofficial NeoForge Edition")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );

        player.sendSystemMessage(
                Component.literal("BETA")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
        );

        player.sendSystemMessage(Component.empty());

        player.sendSystemMessage(
                Component.literal(
                                "This is a Beta version of the NeoForge port. "
                                        + "The mod is still under active development and some content "
                                        + "from the original HBM's Nuclear Tech Mod CE has not been ported yet."
                        )
                        .withStyle(ChatFormatting.GRAY)
        );

        player.sendSystemMessage(
                Component.literal(
                                "Some machines, mechanics, recipes and visuals may still contain bugs "
                                        + "or behave differently from the original version."
                        )
                        .withStyle(ChatFormatting.GRAY)
        );

        player.sendSystemMessage(Component.empty());

        player.sendSystemMessage(
                Component.literal(
                                "If you encounter a bug, please report it and include screenshots, logs "
                                        + "and steps to reproduce the issue whenever possible."
                        )
                        .withStyle(ChatFormatting.GRAY)
        );

        player.sendSystemMessage(Component.empty());

        player.sendSystemMessage(
                createLink("GitHub: ", GITHUB_URL)
        );

        player.sendSystemMessage(
                createLink("CurseForge: ", CURSEFORGE_URL)
        );

        player.sendSystemMessage(
                createLink("Discord: ", DISCORD_URL)
        );

        player.sendSystemMessage(
                Component.literal("====================================================")
                        .withStyle(ChatFormatting.DARK_RED)
        );

        player.sendSystemMessage(Component.empty());
    }

    private static MutableComponent createLink(String prefix, String url) {
        return Component.literal(prefix)
                .withStyle(ChatFormatting.GRAY)
                .append(
                        Component.literal(url)
                                .setStyle(
                                        Style.EMPTY
                                                .withColor(ChatFormatting.AQUA)
                                                .withUnderlined(true)
                                                .withClickEvent(
                                                        new ClickEvent(
                                                                ClickEvent.Action.OPEN_URL,
                                                                url
                                                        )
                                                )
                                )
                );
    }
}