package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.heat.HeatSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class HeatingOvenBlockEntity extends AbstractHeaterBlockEntity {
    public static final int MAX_HEAT = 500_000;
    public HeatingOvenBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.HEATING_OVEN.get(), pos, state); }
    @Override protected void tickHeatInput(Level level) {
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        if (below instanceof HeatSource source) {
            int room = getMaxHeat() - getHeatStored();
            int pulled = source.extractHeat(Math.max(room, 0));
            addHeat(pulled / 2);
        }
    }
    @Override protected int getBaseHeat() { return 500; }
    @Override protected double getTimeMultiplier() { return 0.125D; }
    @Override public int getMaxHeat() { return MAX_HEAT; }
    @Override protected Component getMachineName() { return Component.translatable("container.hbm_neoforge.heater_oven"); }
}
