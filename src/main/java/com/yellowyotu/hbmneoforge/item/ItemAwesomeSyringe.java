package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ItemAwesomeSyringe extends Item {
    public ItemAwesomeSyringe(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            applyEffects(player);
            consumeAndReturnEmpty(level, player, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target == player) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            applyEffects(target);
            consumeAndReturnEmpty(player.level(), player, stack);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    private static void consumeAndReturnEmpty(Level level, Player player, ItemStack stack) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SYRINGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack empty = new ItemStack(ModItems.SYRINGE_EMPTY.get());
            if (!player.getInventory().add(empty)) {
                player.drop(empty, false);
            }
        }
    }

    private static void applyEffects(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50 * 20, 9));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50 * 20, 9));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * 20, 24));
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 50 * 20, 9));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50 * 20, 6));
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 50 * 20, 9));
        entity.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 50 * 20, 9));
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 50 * 20, 4));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 4));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_neoforge.syringe_awesome.desc"));
    }
}
