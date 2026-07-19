package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.radiation.RadiationContaminationType;
import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class ItemNukaCola extends Item {

    private static final int USE_DURATION_TICKS = 32;
    private static final int EFFECT_DURATION_TICKS = 30 * 20;

    public ItemNukaCola(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        if (!level.isClientSide()) {
            applyEffects(player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                giveContainers(player);
            }
        }
        return stack;
    }

    private static void applyEffects(Player player) {
        player.heal(4.0F);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, EFFECT_DURATION_TICKS, 1));
        RadiationSystem.contaminate(player, RadiationContaminationType.RAD_BYPASS, 5.0F);
    }

    private static void giveContainers(Player player) {
        giveItem(player, new ItemStack(ModItems.CAP_NUKA.get()));
        giveItem(player, new ItemStack(ModItems.BOTTLE_EMPTY.get()));
    }

    private static void giveItem(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Contains about 210 kcal and 1500 mSv.").withStyle(ChatFormatting.GRAY));
    }
}
