package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
            stack.shrink(1);
            ItemStack empty = new ItemStack(ModItems.SYRINGE_EMPTY.get());
            if (!player.getInventory().add(empty)) {
                player.drop(empty, false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void applyEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50 * 20, 9));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50 * 20, 9));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * 20, 24));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 50 * 20, 9));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50 * 20, 6));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 50 * 20, 9));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 50 * 20, 9));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 50 * 20, 4));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 4));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_neoforge.syringe_awesome.desc"));
    }
}
