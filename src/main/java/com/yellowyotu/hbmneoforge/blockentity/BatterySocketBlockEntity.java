package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.menu.BatterySocketMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BatterySocketBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MODE_INPUT = 0;
    public static final int MODE_BOTH = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;
    private static final int TRANSFER_RATE = 100;
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.BATTERY_PACK.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private int modeWithoutSignal = MODE_BOTH;
    private int modeWithSignal = MODE_BOTH;
    private int lastDelta;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> modeWithoutSignal;
                case 1 -> modeWithSignal;
                case 2 -> getEnergy();
                case 3 -> ItemBatteryPack.CAPACITY;
                case 4 -> lastDelta;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                modeWithoutSignal = value;
            } else if (index == 1) {
                modeWithSignal = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public BatterySocketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_SOCKET.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public int getEnergy() {
        ItemStack stack = inventory.getStackInSlot(0);
        return stack.is(ModItems.BATTERY_PACK.get()) ? ItemBatteryPack.getEnergy(stack) : 0;
    }

    public int getActiveMode() {
        return level != null && level.hasNeighborSignal(worldPosition) ? modeWithSignal : modeWithoutSignal;
    }

    public boolean canInput() {
        int mode = getActiveMode();
        return mode == MODE_INPUT || mode == MODE_BOTH;
    }

    public boolean canOutput() {
        int mode = getActiveMode();
        return mode == MODE_OUTPUT || mode == MODE_BOTH;
    }

    public int extractEnergyForMachine(int amount) {
        if (!canOutput() || amount <= 0) {
            return 0;
        }
        ItemStack stack = inventory.getStackInSlot(0);
        if (!stack.is(ModItems.BATTERY_PACK.get())) {
            return 0;
        }
        int extracted = ItemBatteryPack.extractEnergy(stack, amount);
        if (extracted > 0) {
            lastDelta = -extracted;
            setChangedAndSync();
        }
        return extracted;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BatterySocketBlockEntity socket) {
        socket.lastDelta = 0;
        if (!socket.canOutput()) {
            return;
        }
        ItemStack sourceStack = socket.inventory.getStackInSlot(0);
        if (!sourceStack.is(ModItems.BATTERY_PACK.get())) {
            return;
        }
        int sourceEnergy = ItemBatteryPack.getEnergy(sourceStack);
        if (sourceEnergy <= 0) {
            return;
        }
        BatterySocketBlockEntity target = socket.findConsumer(sourceEnergy);
        if (target == null || target == socket || !target.canInput()) {
            return;
        }
        ItemStack targetStack = target.inventory.getStackInSlot(0);
        if (!targetStack.is(ModItems.BATTERY_PACK.get())) {
            return;
        }
        int targetEnergy = ItemBatteryPack.getEnergy(targetStack);
        if (targetEnergy >= ItemBatteryPack.CAPACITY) {
            return;
        }
        int transferable;
        if (socket.canInput() && target.canOutput()) {
            if (sourceEnergy <= targetEnergy) {
                return;
            }
            transferable = Math.max(0, (sourceEnergy - targetEnergy) / 2);
        } else {
            transferable = sourceEnergy;
        }
        int requested = Math.min(TRANSFER_RATE, Math.min(ItemBatteryPack.CAPACITY - targetEnergy, transferable));
        if (requested <= 0) {
            return;
        }
        int extracted = ItemBatteryPack.extractEnergy(sourceStack, requested);
        int accepted = ItemBatteryPack.insertEnergy(targetStack, extracted);
        if (accepted < extracted) {
            ItemBatteryPack.insertEnergy(sourceStack, extracted - accepted);
        }
        if (accepted > 0) {
            socket.lastDelta = -accepted;
            target.lastDelta = accepted;
            socket.setChangedAndSync();
            target.setChangedAndSync();
        }
    }

    private BatterySocketBlockEntity findConsumer(int sourceEnergy) {
        if (level == null) {
            return null;
        }
        BatterySocketBlockEntity bestTarget = null;
        int bestEnergy = Integer.MAX_VALUE;
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> checkedControllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos part = worldPosition.offset(x, y, z);
                    for (Direction direction : Direction.values()) {
                        queue.add(part.relative(direction));
                    }
                }
            }
        }
        int scanned = 0;
        while (!queue.isEmpty() && scanned++ < 4096) {
            BlockPos currentPos = queue.removeFirst();
            if (!visited.add(currentPos) || currentPos.distManhattan(worldPosition) > 64) {
                continue;
            }
            BlockState currentState = level.getBlockState(currentPos);
            if (currentState.is(ModBlocks.RED_CABLE.get())) {
                for (Direction direction : Direction.values()) {
                    queue.add(currentPos.relative(direction));
                }
                continue;
            }
            BlockPos controllerPos = null;
            if (currentState.is(ModBlocks.MACHINE_BATTERY_SOCKET.get())) {
                controllerPos = currentPos;
            } else if (currentState.is(ModBlocks.MACHINE_BATTERY_SOCKET_DUMMY.get())) {
                controllerPos = BatterySocketDummyBlock.findController(level, currentPos);
            }
            if (controllerPos == null || controllerPos.equals(worldPosition) || !checkedControllers.add(controllerPos)) {
                continue;
            }
            if (!(level.getBlockEntity(controllerPos) instanceof BatterySocketBlockEntity target) || target == this || !target.canInput()) {
                continue;
            }
            ItemStack targetStack = target.inventory.getStackInSlot(0);
            if (!targetStack.is(ModItems.BATTERY_PACK.get())) {
                continue;
            }
            int targetEnergy = ItemBatteryPack.getEnergy(targetStack);
            if (targetEnergy >= ItemBatteryPack.CAPACITY) {
                continue;
            }
            if (canInput() && target.canOutput() && targetEnergy >= sourceEnergy) {
                continue;
            }
            if (targetEnergy < bestEnergy) {
                bestEnergy = targetEnergy;
                bestTarget = target;
            }
        }
        return bestTarget;
    }

    public void cycleMode(boolean poweredMode) {
        if (poweredMode) {
            modeWithSignal = (modeWithSignal + 1) % 4;
        } else {
            modeWithoutSignal = (modeWithoutSignal + 1) % 4;
        }
        setChangedAndSync();
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        ItemStack stack = inventory.extractItem(0, 1, false);
        if (!stack.isEmpty()) {
            Block.popResource(level, worldPosition, stack);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("ModeWithoutSignal", modeWithoutSignal);
        tag.putInt("ModeWithSignal", modeWithSignal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        modeWithoutSignal = tag.contains("ModeWithoutSignal") ? tag.getInt("ModeWithoutSignal") : MODE_BOTH;
        modeWithSignal = tag.contains("ModeWithSignal") ? tag.getInt("ModeWithSignal") : MODE_BOTH;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.battery_socket");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BatterySocketMenu(containerId, playerInventory, this);
    }
}
