package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.client.model.GasMaskArmorModel;
import com.yellowyotu.hbmneoforge.client.model.M65MaskArmorModel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import net.minecraft.client.model.HumanoidModel;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public final class GasMaskItem extends ArmorItem {
    public enum MaskType {
        GAS_MASK,
        M65,
        HALF,
        LEATHER
    }

    private static final String FILTER_ID = "hfrFilterId";
    private static final String FILTER_DAMAGE = "hfrFilterDamage";
    private final MaskType maskType;

    public GasMaskItem(net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> material, MaskType maskType) {
        super(material, Type.HELMET, new Item.Properties().stacksTo(1).durability(Type.HELMET.getDurability(15)));
        this.maskType = maskType;
    }

    public MaskType getMaskType() {
        return maskType;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HumanoidModel<?> model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity livingEntity,
                    ItemStack itemStack,
                    EquipmentSlot equipmentSlot,
                    HumanoidModel<?> original) {
                if (model == null) {
                    model = maskType == MaskType.GAS_MASK
                            ? new GasMaskArmorModel(GasMaskArmorModel.createBodyLayer().bakeRoot())
                            : new M65MaskArmorModel(M65MaskArmorModel.createBodyLayer().bakeRoot());
                }

                original.copyPropertiesTo((HumanoidModel) model);
                model.setAllVisible(false);
                model.head.visible = true;
                model.hat.visible = false;
                if (model instanceof GasMaskArmorModel gasMask) {
                    gasMask.applyOriginalScale();
                } else if (model instanceof M65MaskArmorModel m65) {
                    m65.applyOriginalScale();
                    m65.setFilterVisible(hasFilter(itemStack));
                }
                return model;
            }
        });
    }

    public static boolean hasFilter(ItemStack mask) {
        return !getInstalledFilter(mask).isEmpty();
    }

    public static ItemStack getInstalledFilter(ItemStack mask) {
        CompoundTag tag = mask.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(FILTER_ID)) {
            return ItemStack.EMPTY;
        }

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(FILTER_ID));
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack filter = new ItemStack(item);
        if (filter.isEmpty()) {
            return ItemStack.EMPTY;
        }

        filter.setDamageValue(Math.min(tag.getInt(FILTER_DAMAGE), Math.max(0, filter.getMaxDamage())));
        return filter;
    }

    public static int getFilterRemaining(ItemStack mask) {
        ItemStack filter = getInstalledFilter(mask);
        if (filter.isEmpty() || filter.getMaxDamage() <= 0) {
            return 0;
        }
        return Math.max(0, filter.getMaxDamage() - filter.getDamageValue());
    }

    public static void installFilter(ItemStack mask, ItemStack filter) {
        if (filter.isEmpty()) {
            removeFilter(mask);
            return;
        }

        CompoundTag tag = mask.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(FILTER_ID, BuiltInRegistries.ITEM.getKey(filter.getItem()).toString());
        tag.putInt(FILTER_DAMAGE, filter.getDamageValue());
        mask.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void installFilter(ItemStack mask) {
        installFilter(mask, new ItemStack(com.yellowyotu.hbmneoforge.ModItems.GAS_MASK_FILTER.get()));
    }

    public static void removeFilter(ItemStack mask) {
        CompoundTag tag = mask.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(FILTER_ID);
        tag.remove(FILTER_DAMAGE);
        mask.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void damageFilter(ItemStack mask, int amount) {
        if (amount <= 0) {
            return;
        }

        ItemStack filter = getInstalledFilter(mask);
        if (filter.isEmpty() || filter.getMaxDamage() <= 0) {
            return;
        }

        filter.setDamageValue(filter.getDamageValue() + amount);
        if (filter.getDamageValue() > filter.getMaxDamage()) {
            removeFilter(mask);
        } else {
            installFilter(mask, filter);
        }
    }

    public static float protectionMultiplier(ItemStack mask) {
        if (!(mask.getItem() instanceof GasMaskItem gasMask) || !hasFilter(mask)) {
            return 1.0F;
        }
        return gasMask.maskType == MaskType.HALF ? 0.55F : 0.10F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hasFilter(stack)) {
            if (!level.isClientSide()) {
                ItemStack filter = getInstalledFilter(stack);
                removeFilter(stack);
                if (!player.getInventory().add(filter)) {
                    player.drop(filter, false);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        ItemStack filter = getInstalledFilter(stack);
        if (filter.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.hbm_neoforge.gas_mask.no_filter").withStyle(ChatFormatting.RED));
        } else {
            int remaining = Math.max(0, filter.getMaxDamage() - filter.getDamageValue());
            tooltip.add(Component.translatable("tooltip.hbm_neoforge.gas_mask.filter_installed").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("  ").append(filter.getHoverName()).append(Component.literal(" (" + remaining + "/" + filter.getMaxDamage() + ")")));
        }

        if (maskType == MaskType.HALF) {
            tooltip.add(Component.translatable("tooltip.hbm_neoforge.gas_mask.half_warning").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
