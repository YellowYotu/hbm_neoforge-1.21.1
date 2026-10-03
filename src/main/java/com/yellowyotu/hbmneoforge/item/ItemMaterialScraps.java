package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.material.HBMMaterialCatalog;
import com.yellowyotu.hbmneoforge.material.HBMMaterialDefinition;
import com.yellowyotu.hbmneoforge.material.MaterialKind;
import com.yellowyotu.hbmneoforge.material.MaterialScrapCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/** One data-backed scrap item, matching CE's metadata-backed ItemScraps. */
public final class ItemMaterialScraps extends Item {
    private static final String MATERIAL = "hbmMaterial";
    private static final String AMOUNT = "hbmAmount";
    private static final String LIQUID = "hbmLiquid";

    public ItemMaterialScraps(Properties properties) { super(properties); }

    public static ItemStack create(Item item, HBMMaterialDefinition material, int amount, boolean liquid) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putString(MATERIAL, material.id());
        tag.putInt(AMOUNT, Math.max(1, amount));
        tag.putBoolean(LIQUID, liquid);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static HBMMaterialDefinition material(ItemStack stack) {
        String id = data(stack).getString(MATERIAL);
        return HBMMaterialCatalog.get(id).orElseGet(() -> HBMMaterialCatalog.get("bismuth").orElseThrow());
    }

    public static int amount(ItemStack stack) {
        int amount = data(stack).getInt(AMOUNT);
        return amount > 0 ? amount : 72;
    }

    public static boolean liquid(ItemStack stack) { return data(stack).getBoolean(LIQUID); }

    public static boolean supports(HBMMaterialDefinition material) {
        return MaterialScrapCatalog.supports(material);
    }

    public static int color(ItemStack stack) { return material(stack).color(); }

    @Override
    public Component getName(ItemStack stack) {
        return liquid(stack)
                ? Component.translatable("material.hbm_neoforge." + material(stack).id())
                : Component.translatable("item.hbm_neoforge.scraps.material",
                        Component.translatable("material.hbm_neoforge." + material(stack).id()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_neoforge.scraps.amount", amount(stack)).withStyle(ChatFormatting.GRAY));
        if (liquid(stack) && material(stack).kind() == MaterialKind.ADDITIVE) {
            tooltip.add(Component.translatable("item.hbm_neoforge.scraps.additive").withStyle(ChatFormatting.DARK_RED));
        }
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
}
