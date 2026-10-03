package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.material.MaterialForm;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public final class MaterialFormItem extends Item {
    private final MaterialForm form;

    public MaterialFormItem(MaterialForm form, Properties properties) {
        super(properties);
        this.form = form;
    }

    public MaterialForm form() { return form; }
    public int color() { return form.material().color(); }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("material_form.hbm_neoforge." + form.shape().name().toLowerCase(),
                Component.translatable("material.hbm_neoforge." + form.material().id()));
    }
}
