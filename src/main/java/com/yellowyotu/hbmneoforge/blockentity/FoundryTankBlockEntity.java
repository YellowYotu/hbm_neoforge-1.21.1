package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import com.yellowyotu.hbmneoforge.foundry.FoundryTransfer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FoundryTankBlockEntity extends FoundryBaseBlockEntity {
    private int nextUpdate;

    public FoundryTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_TANK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryTankBlockEntity tank) {
        if (tank.material.isBlank() && tank.amount != 0) {
            tank.amount = 0;
        }

        tank.nextUpdate--;
        if (tank.nextUpdate <= 0 && tank.amount > 0 && !tank.material.isBlank()) {
            boolean hasOp = false;
            tank.nextUpdate = level.random.nextInt(6) + 5;

            BlockEntity below = level.getBlockEntity(pos.below());
            if (below instanceof FoundryTankBlockEntity lower
                    && (lower.material.isBlank() || lower.material.equals(tank.material) || lower.amount == 0)
                    && lower.amount < lower.getCapacity()) {
                lower.material = tank.material;
                int transfer = Math.min(tank.amount, lower.getCapacity() - lower.amount);
                tank.amount -= transfer;
                lower.amount += transfer;
                lower.syncIfChanged();
                hasOp = transfer > 0;
            }

            List<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
            Collections.shuffle(directions, new java.util.Random(level.random.nextLong()));

            if (!hasOp) {
                for (Direction direction : directions) {
                    BlockEntity target = level.getBlockEntity(pos.relative(direction));
                    if (target instanceof FoundryBaseBlockEntity
                            && !(target instanceof FoundryChannelBlockEntity)
                            && !(target instanceof FoundryTankBlockEntity)) {
                        int moved = FoundryTransfer.move(level, pos, pos.relative(direction), direction, tank, tank.amount);
                        if (moved > 0) {
                            hasOp = true;
                            break;
                        }
                    }
                }
            }

            if (!hasOp) {
                for (Direction direction : directions) {
                    BlockEntity target = level.getBlockEntity(pos.relative(direction));
                    if (!(target instanceof FoundryTankBlockEntity other)) {
                        continue;
                    }
                    if (!other.material.isBlank() && !other.material.equals(tank.material) && other.amount > 0) {
                        continue;
                    }
                    other.material = tank.material;
                    if (level.random.nextInt(5) == 0) {
                        int buffer = tank.amount;
                        tank.amount = other.amount;
                        other.amount = buffer;
                    } else {
                        int diff = tank.amount - other.amount;
                        if (diff > 0) {
                            diff /= 2;
                            tank.amount -= diff;
                            other.amount += diff;
                        }
                    }
                    other.syncIfChanged();
                }
            }
        }
        if (tank.amount == 0) {
            tank.material = "";
        }
        tank.syncIfChanged();
    }

    @Override
    public int getCapacity() {
        return FoundryMaterialRegistry.BLOCK * 4;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("NextUpdate", nextUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        nextUpdate = tag.getInt("NextUpdate");
    }
}
