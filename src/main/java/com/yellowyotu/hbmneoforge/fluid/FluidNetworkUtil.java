package com.yellowyotu.hbmneoforge.fluid;

import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageDummyBlockEntity;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FluidNetworkUtil {
    private static final int MAX_VISITED = 4096;

    private FluidNetworkUtil() {
    }

    public static int fillNetwork(Level level, BlockPos entryPos, NTMFluidType type, int requested, BlockPos sourceCore) {
        if (level == null || level.isClientSide() || type == null || requested <= 0) {
            return 0;
        }

        ArrayDeque<PathNode> queue = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<BlockPos> visitedTargets = new HashSet<>();
        int remaining = requested;

        BlockEntity entry = level.getBlockEntity(entryPos);
        if (entry instanceof FluidPipeBlockEntity pipe) {
            if (!pipe.canCarry(type)) {
                return 0;
            }
            queue.add(new PathNode(entryPos, pipe.getTransferRate()));
        } else {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = entryPos.relative(direction);
                BlockEntity blockEntity = level.getBlockEntity(adjacent);
                if (blockEntity instanceof FluidPipeBlockEntity pipe && pipe.canCarry(type)) {
                    queue.add(new PathNode(adjacent, pipe.getTransferRate()));
                } else {
                    remaining -= tryFillTarget(blockEntity, type, remaining, sourceCore, visitedTargets, entryPos);
                    if (remaining <= 0) {
                        return requested;
                    }
                }
            }
        }

        while (!queue.isEmpty() && visitedPipes.size() < MAX_VISITED && remaining > 0) {
            PathNode node = queue.removeFirst();
            if (!visitedPipes.add(node.pos())) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(node.pos());
            if (!(blockEntity instanceof FluidPipeBlockEntity pipe) || !pipe.canCarry(type)) {
                continue;
            }

            int pathRate = Math.min(node.rate(), pipe.getTransferRate());
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = node.pos().relative(direction);
                BlockEntity adjacentEntity = level.getBlockEntity(adjacent);
                if (adjacentEntity instanceof FluidPipeBlockEntity adjacentPipe) {
                    if (adjacentPipe.canCarry(type) && pipe.canConnectTo(adjacentPipe)) {
                        queue.addLast(new PathNode(adjacent, Math.min(pathRate, adjacentPipe.getTransferRate())));
                    }
                    continue;
                }

                int offer = Math.min(remaining, pathRate);
                int accepted = tryFillTarget(adjacentEntity, type, offer, sourceCore, visitedTargets, node.pos());
                remaining -= accepted;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        return requested - remaining;
    }

    public static int drainNetwork(Level level, BlockPos entryPos, NTMFluidType type, int requested) {
        if (level == null || level.isClientSide() || type == null || requested <= 0) {
            return 0;
        }

        ArrayDeque<PathNode> queue = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<BlockPos> visitedTargets = new HashSet<>();
        int remaining = requested;

        BlockEntity entry = level.getBlockEntity(entryPos);
        if (entry instanceof FluidPipeBlockEntity pipe) {
            if (!pipe.canCarry(type)) {
                return 0;
            }
            queue.add(new PathNode(entryPos, pipe.getTransferRate()));
        }

        while (!queue.isEmpty() && visitedPipes.size() < MAX_VISITED && remaining > 0) {
            PathNode node = queue.removeFirst();
            if (!visitedPipes.add(node.pos())) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(node.pos());
            if (!(blockEntity instanceof FluidPipeBlockEntity pipe) || !pipe.canCarry(type)) {
                continue;
            }

            int pathRate = Math.min(node.rate(), pipe.getTransferRate());
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = node.pos().relative(direction);
                BlockEntity adjacentEntity = level.getBlockEntity(adjacent);
                if (adjacentEntity instanceof FluidPipeBlockEntity adjacentPipe) {
                    if (adjacentPipe.canCarry(type) && pipe.canConnectTo(adjacentPipe)) {
                        queue.addLast(new PathNode(adjacent, Math.min(pathRate, adjacentPipe.getTransferRate())));
                    }
                    continue;
                }

                int request = Math.min(remaining, pathRate);
                int drained = tryDrainTarget(adjacentEntity, type, request, visitedTargets, node.pos());
                remaining -= drained;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        return requested - remaining;
    }

    private static int tryFillTarget(BlockEntity blockEntity, NTMFluidType type, int requested, BlockPos sourceCore, Set<BlockPos> visitedTargets, BlockPos fromPos) {
        if (requested <= 0 || blockEntity == null) {
            return 0;
        }

        if (!canAccessTarget(blockEntity, fromPos)) {
            return 0;
        }
        FluidNode node = resolveNode(blockEntity);
        BlockPos targetPos = resolveCorePos(blockEntity);
        if (node == null || targetPos == null || targetPos.equals(sourceCore) || !visitedTargets.add(targetPos)) {
            return 0;
        }
        if (!node.accepts(type)) {
            return 0;
        }
        return node.fill(type, requested);
    }

    private static int tryDrainTarget(BlockEntity blockEntity, NTMFluidType type, int requested, Set<BlockPos> visitedTargets, BlockPos fromPos) {
        if (requested <= 0 || blockEntity == null) {
            return 0;
        }

        if (!canAccessTarget(blockEntity, fromPos)) {
            return 0;
        }
        FluidNode node = resolveNode(blockEntity);
        BlockPos targetPos = resolveCorePos(blockEntity);
        if (node == null || targetPos == null || !visitedTargets.add(targetPos) || node.getFluidType() != type) {
            return 0;
        }
        return node.drain(type, requested);
    }

    private static boolean canAccessTarget(BlockEntity blockEntity, BlockPos fromPos) {
        if (blockEntity == null || fromPos == null) {
            return false;
        }
        if (blockEntity instanceof FluidStorageDummyBlockEntity dummy) {
            BlockPos core = dummy.getController();
            if (core == null || dummy.getLevel() == null) {
                return false;
            }
            net.minecraft.world.level.block.state.BlockState state = dummy.getLevel().getBlockState(core);
            if (state.getBlock() instanceof com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock tank) {
                Direction outward = directionBetween(blockEntity.getBlockPos(), fromPos);
                return outward != null && tank.canConnectAt(dummy.getLevel(), blockEntity.getBlockPos(), outward);
            }
        }
        if (blockEntity instanceof FluidStorageBlockEntity storage && storage.getBlockState().getBlock() instanceof com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock tank) {
            Direction outward = directionBetween(blockEntity.getBlockPos(), fromPos);
            return outward != null && tank.canConnectAt(storage.getLevel(), blockEntity.getBlockPos(), outward);
        }
        return true;
    }

    private static Direction directionBetween(BlockPos origin, BlockPos target) {
        int dx = target.getX() - origin.getX();
        int dy = target.getY() - origin.getY();
        int dz = target.getZ() - origin.getZ();
        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == dx && direction.getStepY() == dy && direction.getStepZ() == dz) {
                return direction;
            }
        }
        return null;
    }

    private static FluidNode resolveNode(BlockEntity blockEntity) {
        if (blockEntity instanceof FluidStorageDummyBlockEntity dummy) {
            return dummy.getCoreStorage();
        }
        return blockEntity instanceof FluidNode node ? node : null;
    }

    private static BlockPos resolveCorePos(BlockEntity blockEntity) {
        if (blockEntity instanceof FluidStorageDummyBlockEntity dummy && dummy.getController() != null) {
            return dummy.getController();
        }
        return blockEntity.getBlockPos();
    }

    private record PathNode(BlockPos pos, int rate) {
    }
}
