package com.yellowyotu.hbmneoforge.item;

import net.minecraft.world.item.Item;

public final class ItemSolderingFluidCell extends Item {

    public enum FluidType {
        SULFURIC_ACID(0xFFD7D76A),
        PEROXIDE(0xFFE7F2FF),
        SOLVENT(0xFF8AB0B8),
        HELIUM4(0xFFD8F6FF),
        PERFLUOROMETHYL(0xFFBCD7E4),
        PERFLUOROMETHYL_COLD(0xFF8DC9F4);

        private final int color;

        FluidType(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    private final FluidType fluidType;

    public ItemSolderingFluidCell(Properties properties, FluidType fluidType) {
        super(properties);
        this.fluidType = fluidType;
    }

    public FluidType getFluidType() {
        return fluidType;
    }
}
