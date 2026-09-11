package com.yellowyotu.hbmneoforge.heat;

public interface HeatSource {
    int getHeatStored();
    int extractHeat(int amount);
}
