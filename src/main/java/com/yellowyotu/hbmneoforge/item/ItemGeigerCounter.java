package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.GeigerDisplay;
import com.yellowyotu.hbmneoforge.radiation.GeigerSounds;
import com.yellowyotu.hbmneoforge.radiation.RadiationSystem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

        if (!(entity instanceof LivingEntity livingEntity) || level.isClientSide() || level.getGameTime() % 5L != 0L) {
            return;
        }

        float radiation = RadiationSystem.getEnvironmentRadiationBuffer(livingEntity);

        if (radiation > 1.0E-5F) {
            SoundEvent sound = GeigerSounds.select(radiation, livingEntity.getRandom());

            if (sound != null) {
                level.playSound(null, livingEntity.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        } else if (livingEntity.getRandom().nextInt(50) == 0) {
            level.playSound(null, livingEntity.blockPosition(), ModSounds.GEIGER_1.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), ModSounds.TECH_BOOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            GeigerDisplay.printGeigerData(player);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}