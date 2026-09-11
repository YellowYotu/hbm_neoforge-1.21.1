package com.yellowyotu.hbmneoforge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class GasMaskFilterItem extends Item {
    public GasMaskFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack filter = player.getItemInHand(hand);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof GasMaskItem)) {
            return InteractionResultHolder.pass(filter);
        }
        if (!level.isClientSide()) {
            ItemStack previous = GasMaskItem.getInstalledFilter(helmet);
            ItemStack installed = filter.copy();
            installed.setCount(1);
            GasMaskItem.installFilter(helmet, installed);

            if (!player.getAbilities().instabuild) {
                filter.shrink(1);
            }

            if (!previous.isEmpty()) {
                if (filter.isEmpty()) {
                    filter = previous;
                } else if (!player.getInventory().add(previous)) {
                    player.drop(previous, false);
                }
            }

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    com.yellowyotu.hbmneoforge.ModSounds.GAS_MASK_SCREW.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }
        return InteractionResultHolder.sidedSuccess(filter, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.gas_mask_filter.use").withStyle(ChatFormatting.GRAY));
    }
}
