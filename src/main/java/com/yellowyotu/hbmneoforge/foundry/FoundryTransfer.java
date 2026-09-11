package com.yellowyotu.hbmneoforge.foundry;

import com.yellowyotu.hbmneoforge.blockentity.FoundryBaseBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FoundryCastingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FoundryTransfer {
    private FoundryTransfer() {
    }

    public static int move(Level level, BlockPos from, BlockPos to, Direction direction, FoundryBaseBlockEntity source, int maxAmount) {
        if (source.getAmount() <= 0 || source.getMaterial().isBlank()) {
            return 0;
        }
        BlockEntity target = level.getBlockEntity(to);
        if (!(target instanceof FoundryBaseBlockEntity acceptor)) {
            return 0;
        }
        String material = source.getMaterial();
        int request = Math.min(maxAmount, source.getAmount());
        int moved;
        if (acceptor instanceof FoundryCastingBlockEntity casting && direction == Direction.DOWN) {
            moved = casting.insertPour(material, request);
        } else {
            if (!acceptor.canReceiveFrom(direction.getOpposite(), material)) {
                return 0;
            }
            moved = acceptor.insertMolten(material, request);
        }
        if (moved > 0) {
            source.removeMolten(moved);
        }
        return moved;
    }
}
