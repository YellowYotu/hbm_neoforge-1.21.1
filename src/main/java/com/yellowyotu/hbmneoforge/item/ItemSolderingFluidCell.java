package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemSolderingFluidCell extends Item {
    public enum FluidType {
        SULFURIC_ACID(0xFFD7D76A), PEROXIDE(0xFFE7F2FF), SOLVENT(0xFF8AB0B8), HELIUM4(0xFFD8F6FF), PERFLUOROMETHYL(0xFFBCD7E4), PERFLUOROMETHYL_COLD(0xFF8DC9F4);
        private final int color;
        FluidType(int color) { this.color = color; }
        public int getColor() { return color; }
    }

    private final FluidType fluidType;
    public ItemSolderingFluidCell(Properties properties, FluidType fluidType) { super(properties); this.fluidType = fluidType; }
    public FluidType getFluidType() { return fluidType; }

    @Override public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) { return InteractionResult.PASS; }
        NTMFluidType type = NTMFluidType.valueOf(fluidType.name());
        if (!node.accepts(type)) { return InteractionResult.PASS; }
        if (!context.getLevel().isClientSide() && node.fill(type, 1_000) == 1_000 && context.getPlayer() != null) {
            context.getPlayer().setItemInHand(context.getHand(), new ItemStack(ModItems.CELL_EMPTY.get()));
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
