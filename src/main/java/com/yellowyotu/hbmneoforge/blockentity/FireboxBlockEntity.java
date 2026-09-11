package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public final class FireboxBlockEntity extends AbstractHeaterBlockEntity {
    public static final int MAX_HEAT = 100_000;
    public FireboxBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.FIREBOX.get(), pos, state); }
    @Override protected int getBaseHeat() { return 100; }
    @Override protected double getTimeMultiplier() { return 1.0D; }
    @Override public int getMaxHeat() { return MAX_HEAT; }
    @Override protected Component getMachineName() { return Component.translatable("container.hbm_neoforge.heater_firebox"); }
}
