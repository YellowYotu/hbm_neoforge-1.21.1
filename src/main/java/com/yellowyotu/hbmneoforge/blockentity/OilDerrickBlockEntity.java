package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.OilDerrickBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemEmptyPortableFluidContainer;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.item.ItemPortableFluidContainer;
import com.yellowyotu.hbmneoforge.menu.OilDerrickMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class OilDerrickBlockEntity extends BlockEntity implements MenuProvider, MachineEnergySource {
    public static final int MAX_ENERGY = 100_000;
    public static final int TANK_CAPACITY = 64_000;
    private static final int BASE_CONSUMPTION = 100;
    private static final int BASE_DELAY = 50;
    private static final int OIL_PER_DEPOSIT = 500;
    private static final int GAS_PER_DEPOSIT_MIN = 100;
    private static final int GAS_PER_DEPOSIT_MAX = 500;
    private static final double DRAIN_CHANCE = 0.05D;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_OIL_INPUT = 1;
    public static final int SLOT_OIL_OUTPUT = 2;
    public static final int SLOT_GAS_INPUT = 3;
    public static final int SLOT_GAS_OUTPUT = 4;
    public static final int SLOT_UPGRADE_1 = 5;
    public static final int SLOT_UPGRADE_2 = 6;
    public static final int INVENTORY_SIZE = 7;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return ItemBatteryPack.isBattery(stack);
            }
            if (slot == SLOT_OIL_INPUT || slot == SLOT_GAS_INPUT) {
                return stack.getItem() instanceof ItemEmptyPortableFluidContainer || stack.getItem() instanceof ItemPortableFluidContainer;
            }
            if (slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_BATTERY || slot >= SLOT_UPGRADE_1 ? 1 : super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };

    private int energy;
    private int oilAmount;
    private int gasAmount;
    private int indicator;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> MAX_ENERGY;
                case 2 -> oilAmount;
                case 3 -> gasAmount;
                case 4 -> indicator;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = value;
                case 2 -> oilAmount = value;
                case 3 -> gasAmount = value;
                case 4 -> indicator = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public OilDerrickBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_DERRICK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OilDerrickBlockEntity derrick) {
        derrick.chargeFromBattery();
        derrick.pullEnergyFromNetwork();
        derrick.handlePortableContainer(SLOT_OIL_INPUT, SLOT_OIL_OUTPUT, NTMFluidType.OIL);
        derrick.handlePortableContainer(SLOT_GAS_INPUT, SLOT_GAS_OUTPUT, NTMFluidType.GAS);
        derrick.pushFluidToNetwork();
        int consumption = derrick.getEffectiveConsumption();
        if (derrick.energy < consumption || derrick.oilAmount >= TANK_CAPACITY || derrick.gasAmount >= TANK_CAPACITY) {
            derrick.indicator = 0;
            derrick.setChangedAndSync();
            return;
        }

        derrick.energy -= consumption;
        if (level.getGameTime() % derrick.getEffectiveDelay() == 0L) {
            derrick.indicator = 0;
            derrick.drillOrPump((ServerLevel) level);
        }
        derrick.setChangedAndSync();
    }

    private void drillOrPump(ServerLevel level) {
        int minY = level.getMinBuildHeight() + 5;
        for (int y = worldPosition.getY() - 1; y >= minY; y--) {
            BlockPos drillPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
            BlockState state = level.getBlockState(drillPos);
            if (state.is(ModBlocks.OIL_PIPE.get())) {
                if (y == minY) {
                    indicator = 1;
                }
                continue;
            }
            if (trySuck(level, drillPos)) {
                return;
            }
            if (state.getDestroySpeed(level, drillPos) < 0.0F || state.is(Blocks.BEDROCK)) {
                indicator = 2;
                return;
            }
            derrickDrillEffect(level, drillPos, state);
            level.setBlock(drillPos, ModBlocks.OIL_PIPE.get().defaultBlockState(), 3);
            return;
        }
        indicator = 1;
    }

    private boolean trySuck(ServerLevel level, BlockPos start) {
        BlockState startState = level.getBlockState(start);
        if (!isOilSearchBlock(startState)) {
            return false;
        }

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        int layers = 0;
        while (!queue.isEmpty() && layers++ <= 64) {
            int layerSize = queue.size();
            for (int i = 0; i < layerSize; i++) {
                BlockPos current = queue.removeFirst();
                BlockState state = level.getBlockState(current);
                if (state.is(ModBlocks.ORE_OIL.get())) {
                    pumpDeposit(level, current);
                    return true;
                }
                if (!state.is(ModBlocks.ORE_OIL_EMPTY.get())) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    BlockPos next = current.relative(direction);
                    if (visited.add(next) && isOilSearchBlock(level.getBlockState(next))) {
                        queue.addLast(next);
                    }
                }
            }
        }
        return false;
    }

    private static boolean isOilSearchBlock(BlockState state) {
        return state.is(ModBlocks.ORE_OIL.get()) || state.is(ModBlocks.ORE_OIL_EMPTY.get());
    }

    private void pumpDeposit(ServerLevel level, BlockPos depositPos) {
        oilAmount = Math.min(TANK_CAPACITY, oilAmount + OIL_PER_DEPOSIT);
        gasAmount = Math.min(TANK_CAPACITY, gasAmount + GAS_PER_DEPOSIT_MIN + level.random.nextInt(GAS_PER_DEPOSIT_MAX - GAS_PER_DEPOSIT_MIN + 1));
        level.playSound(null, worldPosition, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 2.0F, 0.5F);
        if (level.random.nextDouble() < DRAIN_CHANCE) {
            level.setBlock(depositPos, ModBlocks.ORE_OIL_EMPTY.get().defaultBlockState(), 3);
        }
    }

    private int getEffectiveConsumption() {
        int speed = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int power = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdrive = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE) + 1;
        int consumption = BASE_CONSUMPTION + BASE_CONSUMPTION * speed / 4 - BASE_CONSUMPTION * power / 4;
        return Math.max(1, consumption * overdrive);
    }

    private int getEffectiveDelay() {
        int speed = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int power = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdrive = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE) + 1;
        int delay = BASE_DELAY - BASE_DELAY * speed / 4 + BASE_DELAY * power / 10;
        return Math.max(1, delay / overdrive);
    }

    private void derrickDrillEffect(ServerLevel level, BlockPos drillPos, BlockState drilledState) {
        if (drilledState.is(ModBlocks.ORE_URANIUM.get())) {
            spawnDrillingHazard(level, drillPos, true);
        } else if (drilledState.is(ModBlocks.ORE_ASBESTOS.get())) {
            spawnDrillingHazard(level, drillPos, false);
        }
    }

    private void spawnDrillingHazard(ServerLevel level, BlockPos drillPos, boolean radon) {
        BlockState gas = (radon ? ModBlocks.GAS_RADON_DENSE.get() : ModBlocks.GAS_ASBESTOS.get()).defaultBlockState();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos gasPos = worldPosition.offset(x, 10, z);
                if (level.getBlockState(gasPos).canBeReplaced()) {
                    level.setBlock(gasPos, gas, 3);
                }
            }
        }
    }

    private int getUpgradeLevel(ItemMachineUpgrade.UpgradeType type) {
        int result = 0;
        for (int slot = SLOT_UPGRADE_1; slot <= SLOT_UPGRADE_2; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == type) {
                result += upgrade.getLevel();
            }
        }
        return Math.min(result, 3);
    }

    private void chargeFromBattery() {
        ItemStack stack = inventory.getStackInSlot(SLOT_BATTERY);
        if (!ItemBatteryPack.isBattery(stack) || energy >= MAX_ENERGY) {
            return;
        }
        energy += ItemBatteryPack.extractEnergy(stack, Math.min(1_000, MAX_ENERGY - energy));
    }

    private void pullEnergyFromNetwork() {
        if (level == null || energy >= MAX_ENERGY) {
            return;
        }
        MachineEnergySource source = findPowerSource();
        if (source != null) {
            energy += source.extractEnergyForMachine(Math.min(1_000, MAX_ENERGY - energy));
        }
    }

    @Nullable
    private MachineEnergySource findPowerSource() {
        if (level == null) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos part : OilDerrickBlock.getMachinePositions(worldPosition)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                queue.add(part.relative(direction));
            }
        }
        int scanned = 0;
        while (!queue.isEmpty() && scanned++ < 4_096) {
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
            BlockEntity entity = level.getBlockEntity(current);
            if (entity instanceof BatterySocketBlockEntity socket && socket.canOutput() && socket.getStoredEnergy() > 0) {
                return socket;
            }
            if (entity instanceof MachineEnergySource source && source != this && source.getStoredEnergy() > 0) {
                return source;
            }
        }
        return null;
    }

    private void pushFluidToNetwork() {
        if (level == null) {
            return;
        }

        if (oilAmount > 0) {
            oilAmount -= pushFluidThroughPorts(NTMFluidType.OIL, oilAmount);
        }

        if (gasAmount > 0) {
            gasAmount -= pushFluidThroughPorts(NTMFluidType.GAS, gasAmount);
        }
    }

    private int pushFluidThroughPorts(NTMFluidType type, int available) {
        if (level == null || available <= 0) {
            return 0;
        }

        int remaining = available;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (remaining <= 0) {
                break;
            }

            // Original CE connection anchors are the four empty lower side-center positions.
            // A fluid pipe is placed directly in one of these positions next to the controller.
            BlockPos connectionPos = worldPosition.relative(direction);

            int moved = FluidNetworkUtil.fillNetwork(
                    level,
                    connectionPos,
                    type,
                    Math.min(1_000, remaining),
                    worldPosition
            );

            remaining -= moved;
        }

        return available - remaining;
    }

    private void handlePortableContainer(int inputSlot, int outputSlot, NTMFluidType type) {
        ItemStack input = inventory.getStackInSlot(inputSlot);
        int available = type == NTMFluidType.OIL ? oilAmount : gasAmount;
        if (input.isEmpty() || available <= 0) {
            return;
        }

        if (input.getItem() instanceof ItemEmptyPortableFluidContainer empty) {
            int capacity = empty.getCapacity();
            if (available < capacity) {
                return;
            }
            ItemStack filled = new ItemStack(empty.getFilledItem());
            ItemPortableFluidContainer.setFluid(filled, type, capacity);
            if (!canAcceptOutput(outputSlot, filled)) {
                return;
            }
            input.shrink(1);
            inventory.setStackInSlot(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
            addOutput(outputSlot, filled);
            if (type == NTMFluidType.OIL) {
                oilAmount -= capacity;
            } else {
                gasAmount -= capacity;
            }
            return;
        }

        if (input.getItem() instanceof ItemPortableFluidContainer container) {
            NTMFluidType storedType = ItemPortableFluidContainer.getFluidType(input);
            int stored = ItemPortableFluidContainer.getAmount(input);
            int needed = container.getCapacity() - stored;
            if (storedType != type || stored <= 0 || needed <= 0 || available < needed) {
                return;
            }
            ItemStack filled = input.copyWithCount(1);
            ItemPortableFluidContainer.setFluid(filled, type, container.getCapacity());
            if (!canAcceptOutput(outputSlot, filled)) {
                return;
            }
            input.shrink(1);
            inventory.setStackInSlot(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
            addOutput(outputSlot, filled);
            if (type == NTMFluidType.OIL) {
                oilAmount -= needed;
            } else {
                gasAmount -= needed;
            }
        }
    }

    private boolean canAcceptOutput(int slot, ItemStack stack) {
        ItemStack existing = inventory.getStackInSlot(slot);
        return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() + stack.getCount() <= existing.getMaxStackSize();
    }

    private void addOutput(int slot, ItemStack stack) {
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            inventory.setStackInSlot(slot, stack.copy());
            return;
        }
        existing.grow(stack.getCount());
        inventory.setStackInSlot(slot, existing);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public int getEnergy() {
        return energy;
    }

    public int getOilAmount() {
        return oilAmount;
    }

    public int getGasAmount() {
        return gasAmount;
    }

    public int getIndicator() {
        return indicator;
    }

    @Override
    public int getStoredEnergy() {
        return energy;
    }

    @Override
    public int extractEnergyForMachine(int amount) {
        int extracted = Math.min(Math.max(0, amount), energy);
        energy -= extracted;
        if (extracted > 0) {
            setChangedAndSync();
        }
        return extracted;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.oil_derrick");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OilDerrickMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy);
        tag.putInt("Oil", oilAmount);
        tag.putInt("Gas", gasAmount);
        tag.putInt("Indicator", indicator);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        energy = tag.getInt("Energy");
        oilAmount = tag.getInt("Oil");
        gasAmount = tag.getInt("Gas");
        indicator = tag.getInt("Indicator");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            }
        }
        inventory.setSize(INVENTORY_SIZE);
    }
}
