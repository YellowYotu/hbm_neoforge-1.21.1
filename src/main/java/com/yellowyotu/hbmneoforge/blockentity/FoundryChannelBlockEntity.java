package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.foundry.FoundryTransfer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FoundryChannelBlockEntity extends FoundryBaseBlockEntity {
    private int nextUpdate;
    private Direction lastFlow;
    private String neighborType = "";
    private boolean hasCheckedNeighbors;
    private int unpropagateTime;

    public FoundryChannelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_CHANNEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryChannelBlockEntity channel) {
        if (!channel.hasCheckedNeighbors) {
            channel.neighborType = channel.checkNeighbors(new HashSet<>(Set.of(pos)));
            channel.hasCheckedNeighbors = true;
        }

        if (channel.material.isBlank() && channel.amount != 0) {
            channel.amount = 0;
        }

        channel.nextUpdate--;
        if (channel.nextUpdate <= 0 && channel.amount > 0 && !channel.material.isBlank()) {
            boolean hasOp = false;
            channel.nextUpdate = 5;

            List<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
            Collections.shuffle(directions, new java.util.Random(level.random.nextLong()));
            if (channel.lastFlow != null) {
                directions.remove(channel.lastFlow);
                directions.add(channel.lastFlow);
            }

            for (Direction direction : directions) {
                BlockEntity target = level.getBlockEntity(pos.relative(direction));
                if (target instanceof FoundryBaseBlockEntity && !(target instanceof FoundryChannelBlockEntity)) {
                    int moved = FoundryTransfer.move(level, pos, pos.relative(direction), direction, channel, channel.amount);
                    if (moved > 0) {
                        if (channel.amount == 0) {
                            channel.propagateMaterial("");
                        }
                        hasOp = true;
                        break;
                    }
                }
            }

            if (!hasOp) {
                for (Direction direction : directions) {
                    BlockEntity target = level.getBlockEntity(pos.relative(direction));
                    if (!(target instanceof FoundryChannelBlockEntity other)) {
                        continue;
                    }
                    if (other.amount > 0 && !other.material.equals(channel.material)) {
                        continue;
                    }
                    other.material = channel.material;
                    other.lastFlow = direction.getOpposite();
                    if (level.random.nextInt(5) == 0 || channel.amount == 1) {
                        int buffer = channel.amount;
                        channel.amount = other.amount;
                        other.amount = buffer;
                    } else {
                        int difference = channel.amount - other.amount;
                        if (difference > 0) {
                            difference /= 2;
                            channel.amount -= difference;
                            other.amount += difference;
                        }
                    }
                    other.syncIfChanged();
                }
            }
        }

        if (!channel.neighborType.isBlank() && channel.amount == 0) {
            channel.unpropagateTime++;
        }
        if (channel.unpropagateTime > 100) {
            channel.propagateMaterial("");
        }
        if (channel.amount == 0) {
            channel.material = "";
            channel.lastFlow = null;
            channel.nextUpdate = 5;
        } else {
            channel.unpropagateTime = 0;
        }
        channel.syncIfChanged();
    }

    @Override
    public int getCapacity() {
        return com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry.INGOT * 2;
    }

    @Override
    public boolean canReceiveFrom(Direction side, String incomingMaterial) {
        if (!hasCheckedNeighbors) {
            return false;
        }
        if (!neighborType.isBlank() && !neighborType.equals(incomingMaterial)) {
            return false;
        }
        return super.canReceiveFrom(side, incomingMaterial);
    }

    @Override
    public int insertMolten(String incomingMaterial, int incomingAmount) {
        if (!canReceiveFrom(Direction.UP, incomingMaterial)) {
            return 0;
        }
        propagateMaterial(incomingMaterial);
        return super.insertMolten(incomingMaterial, incomingAmount);
    }

    private void propagateMaterial(String type) {
        Set<BlockPos> visited = new HashSet<>();
        boolean hasMaterial = propagateMaterial(type, visited, false);
        if (type.isBlank() && !hasMaterial) {
            for (BlockPos visitedPos : visited) {
                BlockEntity blockEntity = level == null ? null : level.getBlockEntity(visitedPos);
                if (blockEntity instanceof FoundryChannelBlockEntity channel) {
                    channel.neighborType = "";
                    channel.syncIfChanged();
                }
            }
        }
    }

    private boolean propagateMaterial(String type, Set<BlockPos> visited, boolean hasMaterial) {
        if (level == null || !visited.add(worldPosition)) {
            return hasMaterial;
        }
        if (!type.isBlank()) {
            neighborType = type;
        } else {
            unpropagateTime = 0;
        }
        for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
            BlockEntity target = level.getBlockEntity(worldPosition.relative(direction));
            if (target instanceof FoundryChannelBlockEntity channel && !visited.contains(channel.worldPosition)) {
                if (channel.amount > 0) {
                    hasMaterial = true;
                }
                hasMaterial = channel.propagateMaterial(type, visited, hasMaterial);
            }
        }
        return hasMaterial;
    }

    private String checkNeighbors(Set<BlockPos> visited) {
        if (!neighborType.isBlank() || level == null) {
            return neighborType;
        }
        if (amount > 0 && !material.isBlank()) {
            return material;
        }
        for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
            BlockEntity target = level.getBlockEntity(worldPosition.relative(direction));
            if (target instanceof FoundryChannelBlockEntity channel && visited.add(channel.worldPosition)) {
                String found = channel.checkNeighbors(visited);
                if (!found.isBlank()) {
                    return found;
                }
            }
        }
        return "";
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("Flow", (byte) (lastFlow == null ? -1 : lastFlow.get3DDataValue()));
        tag.putString("NeighborType", neighborType);
        tag.putBoolean("Initialized", hasCheckedNeighbors);
        tag.putInt("UnpropagateTime", unpropagateTime);
        tag.putInt("NextUpdate", nextUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int flow = tag.getByte("Flow");
        lastFlow = flow < 0 ? null : Direction.from3DDataValue(flow);
        neighborType = tag.getString("NeighborType");
        hasCheckedNeighbors = tag.getBoolean("Initialized");
        unpropagateTime = tag.getInt("UnpropagateTime");
        nextUpdate = tag.getInt("NextUpdate");
    }
}
