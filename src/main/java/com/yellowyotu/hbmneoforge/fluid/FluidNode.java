package com.yellowyotu.hbmneoforge.fluid;

public interface FluidNode {
    NTMFluidType getFluidType();
    int getFluidAmount();
    int getFluidCapacity();
    int fill(NTMFluidType type, int amount);
    int drain(NTMFluidType type, int amount);
    boolean accepts(NTMFluidType type);
}
