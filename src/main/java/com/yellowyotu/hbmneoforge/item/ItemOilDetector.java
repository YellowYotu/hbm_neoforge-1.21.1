package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ItemOilDetector extends Item {
    public ItemOilDetector(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_neoforge.oil_detector.desc1"));
        tooltip.add(Component.translatable("item.hbm_neoforge.oil_detector.desc2"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int x = Mth.floor(player.getX());
        int y = Mth.floor(player.getY());
        int z = Mth.floor(player.getZ());
        int minY = level.getMinBuildHeight();
        int topY = Math.min(level.getMaxBuildHeight() - 1, y + 15);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        boolean directOil = false;
        for (int scanY = topY; scanY >= minY; scanY--) {
            if (isOil(level, mutable.set(x, scanY, z))) {
                directOil = true;
                break;
            }
        }
        boolean directBedrock = false;
        for (int scanY = minY; scanY <= Math.min(minY + 4, level.getMaxBuildHeight() - 1); scanY++) {
            if (isBedrockOil(level, mutable.set(x, scanY, z))) {
                directBedrock = true;
                break;
            }
        }

        boolean oil = false;
        boolean bedrockOil = false;
        ChunkPos chunk = new ChunkPos(player.blockPosition());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        int maxX = chunk.getMaxBlockX();
        int maxZ = chunk.getMaxBlockZ();
        for (int scanX = minX; scanX <= maxX && !oil && !bedrockOil; scanX++) {
            for (int scanZ = minZ; scanZ <= maxZ && !oil && !bedrockOil; scanZ++) {
                for (int scanY = topY; scanY >= minY; scanY--) {
                    if (isOil(level, mutable.set(scanX, scanY, scanZ))) {
                        oil = true;
                        break;
                    }
                }
                if (!oil) {
                    for (int scanY = minY; scanY <= Math.min(minY + 4, level.getMaxBuildHeight() - 1); scanY++) {
                        if (isBedrockOil(level, mutable.set(scanX, scanY, scanZ))) {
                            bedrockOil = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!level.isClientSide()) {
            if (directBedrock) {
                player.sendSystemMessage(Component.translatable("item.hbm_neoforge.oil_detector.bullseyeBedrock").withStyle(ChatFormatting.DARK_GREEN));
            } else if (directOil) {
                player.sendSystemMessage(Component.translatable("item.hbm_neoforge.oil_detector.bullseye").withStyle(ChatFormatting.GREEN));
            } else if (bedrockOil) {
                player.sendSystemMessage(Component.translatable("item.hbm_neoforge.oil_detector.detectedBedrock").withStyle(ChatFormatting.GOLD));
            } else if (oil) {
                player.sendSystemMessage(Component.translatable("item.hbm_neoforge.oil_detector.detected").withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendSystemMessage(Component.translatable("item.hbm_neoforge.oil_detector.noOil").withStyle(ChatFormatting.RED));
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static boolean isOil(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ORE_OIL.get());
    }

    private static boolean isBedrockOil(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ORE_BEDROCK_OIL.get());
    }
}
