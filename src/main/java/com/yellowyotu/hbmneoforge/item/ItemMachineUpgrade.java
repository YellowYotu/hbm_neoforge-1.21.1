package com.yellowyotu.hbmneoforge.item;

import net.minecraft.world.item.Item;

public final class ItemMachineUpgrade extends Item {

    public enum UpgradeType {
        SPEED,
        POWER,
        OVERDRIVE
    }

    private final UpgradeType upgradeType;
    private final int level;

    public ItemMachineUpgrade(Properties properties, UpgradeType upgradeType, int level) {
        super(properties);
        this.upgradeType = upgradeType;
        this.level = level;
    }

    public UpgradeType getUpgradeType() {
        return upgradeType;
    }

    public int getLevel() {
        return level;
    }
}
