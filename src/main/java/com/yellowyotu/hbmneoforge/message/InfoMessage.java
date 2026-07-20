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
    private static final String DISCORD_URL = "https://discord.gg/A9NK8xypwU";
    private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/hbms-nuclear-tech-unofficial-neoforge-edition";
    private static final String GITHUB_URL = "https://github.com/YellowYotu/hbm_neoforge-1.21.1";
    private static final String CONTACT_EMAIL = "hbm_neoforge_edition@outlook.com";

    private InfoMessage() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.literal("====================================================").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.literal("HBM's Nuclear Tech - Unofficial NeoForge Edition").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("PRE-ALPHA VERSION WARNING").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.literal("This mod is currently in alpha and is not ready for survival gameplay.").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Features may be unfinished, unbalanced, missing or changed in future updates.").withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Bugs, crashes and world-breaking changes may occur.").withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(createLink("Discord server: ", DISCORD_URL));
        player.sendSystemMessage(createLink("CurseForge: ", CURSEFORGE_URL));
        player.sendSystemMessage(createLink("GitHub: ", GITHUB_URL));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.literal("Report bugs through Discord or email: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(CONTACT_EMAIL).withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)));
        player.sendSystemMessage(Component.literal("====================================================").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(Component.empty());
    }

    private static MutableComponent createLink(String name, String url) {
        return Component.literal(name).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(url).setStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))));
    }
}