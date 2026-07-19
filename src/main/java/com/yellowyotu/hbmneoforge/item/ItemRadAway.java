package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModEffects;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.RadiationValues;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ItemRadAway extends Item {

    public ItemRadAway(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            MobEffectInstance currentEffect = player.getEffect(ModEffects.RADAWAY);
            int duration = RadiationValues.RADAWAY_DURATION_TICKS;
            if (currentEffect != null) {
                duration += currentEffect.getDuration();
            }

            stack.shrink(1);
            player.addEffect(new MobEffectInstance(ModEffects.RADAWAY, duration, 0));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.RADAWAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            giveEmptyIv(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void giveEmptyIv(Player player) {
        ItemStack emptyIv = new ItemStack(ModItems.IV_BAG.get());
        if (!player.getInventory().add(emptyIv)) {
            player.drop(emptyIv, false);
        }
    }
}
