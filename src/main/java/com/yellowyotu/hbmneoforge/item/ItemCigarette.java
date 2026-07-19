package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class ItemCigarette extends Item {

    private static final int USE_DURATION_TICKS = 30;

    public ItemCigarette(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity livingEntity
    ) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (!level.isClientSide()) {
            applyCigaretteEffects(player);
            spawnSmoke(level, player);
        }

        return stack;
    }

    private static void applyCigaretteEffects(Player player) {
        RadiationSystem.addRadiation(player, 100.0F);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.PLAYER_COUGH.get(),
                player.getSoundSource(),
                1.0F,
                1.0F
        );

        player.displayClientMessage(
                Component.literal("My lungs are burning.")
                        .withStyle(ChatFormatting.RED),
                true
        );
    }

    private static void spawnSmoke(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                30,
                0.2,
                0.15,
                0.2,
                0.01
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("✓ Asbestos filter").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("✓ High in tar").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("✓ Tobacco contains 100% Polonium-210")
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("✓ Yum").withStyle(ChatFormatting.RED));
    }
}