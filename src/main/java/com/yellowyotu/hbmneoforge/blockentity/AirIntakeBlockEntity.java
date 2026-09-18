package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.AirIntakeBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AirIntakeBlockEntity extends BlockEntity implements FluidNode {
    public static final int MAX_POWER = 2_000;
    public static final int POWER_PER_TICK = MAX_POWER / 20;
    public static final int AIR_CAPACITY = 1_000;

    private int power;
    private int air;
    private boolean active;

    public AirIntakeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_INTAKE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AirIntakeBlockEntity intake) {
        intake.pullEnergyFromNetwork();

        boolean wasActive = intake.active;
        intake.active = intake.power >= POWER_PER_TICK;
        if (intake.active) {
            intake.air = AIR_CAPACITY;
            intake.power -= POWER_PER_TICK;
        }

        intake.pushAir();
        if (wasActive != intake.active || level.getGameTime() % 10L == 0L) {
            intake.setChangedAndSync();
        } else {
            intake.setChanged();
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getPower() {
        return power;
    }

    public int getAir() {
        return air;
    }

    private void pushAir() {
        if (level == null || air <= 0) {
            return;
        }
        for (BlockPos anchor : AirIntakeBlock.getConnectionAnchors(worldPosition, getBlockState().getValue(AirIntakeBlock.FACING))) {
            if (air <= 0) {
                break;
            }
            int moved = FluidNetworkUtil.fillNetwork(level, anchor, NTMFluidType.AIR, air, worldPosition);
            air -= moved;
        }
    }

    private void pullEnergyFromNetwork() {
        if (level == null || power >= MAX_POWER) {
            return;
        }
        MachineEnergySource source = findPowerSource();
        if (source != null) {
            power += source.extractEnergyForMachine(Math.min(1_000, MAX_POWER - power));
        }
    }

    private MachineEnergySource findPowerSource() {
        if (level == null) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos part : AirIntakeBlock.getFootprint(worldPosition, getBlockState().getValue(AirIntakeBlock.FACING))) {
            for (Direction direction : Direction.values()) {
                queue.add(part.relative(direction));
            }
        }

        int scanned = 0;
        while (!queue.isEmpty() && scanned++ < 4096) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current) || current.distManhattan(worldPosition) > 64) {
                continue;
            }
            BlockState state = level.getBlockState(current);
            if (state.is(ModBlocks.RED_CABLE.get())) {
                for (Direction direction : Direction.values()) {
                    queue.add(current.relative(direction));
                }
                continue;
            }

            BlockPos controller = null;
            if (state.is(ModBlocks.MACHINE_BATTERY_SOCKET.get())) {
                controller = current;
            } else if (state.is(ModBlocks.MACHINE_BATTERY_SOCKET_DUMMY.get())) {
                controller = BatterySocketDummyBlock.findController(level, current);
            } else if (state.is(ModBlocks.WOOD_BURNER.get())) {
                controller = current;
            } else if (state.is(ModBlocks.WOOD_BURNER_DUMMY.get()) && level.getBlockEntity(current) instanceof WoodBurnerDummyBlockEntity dummy) {
                controller = dummy.getController();
            }

            if (controller == null || !controllers.add(controller)) {
                continue;
            }
            if (level.getBlockEntity(controller) instanceof BatterySocketBlockEntity socket && socket.canOutput() && socket.getEnergy() > 0) {
                return socket;
            }
            if (level.getBlockEntity(controller) instanceof WoodBurnerBlockEntity burner && burner.getStoredEnergy() > 0) {
                return burner;
            }
        }
        return null;
    }

    @Override
    public NTMFluidType getFluidType() {
        return NTMFluidType.AIR;
    }

    @Override
    public int getFluidAmount() {
        return air;
    }

    @Override
    public int getFluidCapacity() {
        return AIR_CAPACITY;
    }

    @Override
    public int fill(NTMFluidType type, int amount) {
        return 0;
    }

    @Override
    public int drain(NTMFluidType type, int amount) {
        if (type != NTMFluidType.AIR || amount <= 0 || air <= 0) {
            return 0;
        }
        int moved = Math.min(amount, air);
        air -= moved;
        setChangedAndSync();
        return moved;
    }

    @Override
    public boolean accepts(NTMFluidType type) {
        return type == NTMFluidType.AIR;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("power", power);
        tag.putInt("air", air);
        tag.putBoolean("active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getInt("power");
        air = tag.getInt("air");
        active = tag.getBoolean("active");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }
}
