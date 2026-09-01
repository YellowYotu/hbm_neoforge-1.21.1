package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ItemEmptyFluidCell extends Item {
    public ItemEmptyFluidCell(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidNode node)) {
            return InteractionResult.PASS;
        }

        NTMFluidType type = node.getFluidType();
        Item filled = getFilledCell(type);

        if (filled == null || node.getFluidAmount() < 1_000) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide() && node.drain(type, 1_000) == 1_000 && context.getPlayer() != null) {
            context.getPlayer().setItemInHand(context.getHand(), new ItemStack(filled));
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private static Item getFilledCell(NTMFluidType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case SULFURIC_ACID -> ModItems.CELL_SULFURIC_ACID.get();
            case PEROXIDE -> ModItems.CELL_PEROXIDE.get();
            case SOLVENT -> ModItems.CELL_SOLVENT.get();
            case HELIUM4 -> ModItems.CELL_HELIUM4.get();
            case PERFLUOROMETHYL -> ModItems.CELL_PERFLUOROMETHYL.get();
            case PERFLUOROMETHYL_COLD -> ModItems.CELL_PERFLUOROMETHYL_COLD.get();
            case WATER, COOLANT -> null;
            default -> null;
        };
    }
}
