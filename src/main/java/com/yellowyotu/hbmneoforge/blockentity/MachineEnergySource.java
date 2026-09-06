package com.yellowyotu.hbmneoforge.blockentity;

public interface MachineEnergySource {
    int extractEnergyForMachine(int amount);
    int getStoredEnergy();
}
