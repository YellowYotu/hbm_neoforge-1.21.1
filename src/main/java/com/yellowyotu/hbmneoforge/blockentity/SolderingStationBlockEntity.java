package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModParticles;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
import com.yellowyotu.hbmneoforge.menu.SolderingStationMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class SolderingStationBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_TOPPING_START = 0;
    public static final int SLOT_TOPPING_COUNT = 3;
    public static final int SLOT_PCB_START = 3;
    public static final int SLOT_PCB_COUNT = 2;
    public static final int SLOT_SOLDER = 5;
    public static final int SLOT_OUTPUT = 6;
    public static final int SLOT_BATTERY = 7;
    public static final int SLOT_FLUID_CELL = 8;
    public static final int SLOT_UPGRADE_1 = 9;
    public static final int SLOT_UPGRADE_2 = 10;
    public static final int INVENTORY_SIZE = 11;
    public static final int DEFAULT_MAX_ENERGY = 2_000;
    public static final int FLUID_CAPACITY = 8_000;
    public static final int FLUID_CELL_AMOUNT = 1_000;

    private boolean handlingFluidSlot;
    private boolean loading;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= SLOT_TOPPING_START && slot < SLOT_TOPPING_START + SLOT_TOPPING_COUNT) {
                return SolderingStationRecipes.isValidTopping(stack);
            }
            if (slot >= SLOT_PCB_START && slot < SLOT_PCB_START + SLOT_PCB_COUNT) {
                return SolderingStationRecipes.isValidPcb(stack);
            }
            if (slot == SLOT_SOLDER) {
                return SolderingStationRecipes.isValidSolder(stack);
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (slot == SLOT_BATTERY) {
                return stack.is(ModItems.BATTERY_PACK.get());
            }
            if (slot == SLOT_FLUID_CELL) {
                return stack.is(ModItems.CELL_EMPTY.get()) || stack.getItem() instanceof ItemSolderingFluidCell;
            }
            if (slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= SLOT_BATTERY ? 1 : super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loading && slot == SLOT_FLUID_CELL && !handlingFluidSlot) {
                handleFluidCell();
            }
            if (!loading && (slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2)) {
                ItemStack stack = getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade && level != null && !level.isClientSide()) {
                    level.playSound(null, worldPosition, ModSounds.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            setChangedAndSync();
        }
    };

    private final IItemHandler automationInventory = new IItemHandler() {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!inventory.isItemValid(slot, stack)) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == SLOT_OUTPUT || slot == SLOT_FLUID_CELL) {
                return inventory.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(slot, stack);
        }
    };

    private int energy;
    private int maxEnergy = DEFAULT_MAX_ENERGY;
    private int consumption = 100;
    private int progress;
    private int processTime = 1;
    private boolean collisionPrevention;
    private boolean processing;
    private ItemSolderingFluidCell.FluidType fluidType;
    private int fluidAmount;
    private ItemStack display = ItemStack.EMPTY;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> maxEnergy;
                case 2 -> consumption;
                case 3 -> progress;
                case 4 -> processTime;
                case 5 -> collisionPrevention ? 1 : 0;
                case 6 -> processing ? 1 : 0;
                case 7 -> fluidAmount;
                case 8 -> fluidType == null ? -1 : fluidType.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = value;
                case 1 -> maxEnergy = value;
                case 2 -> consumption = value;
                case 3 -> progress = value;
                case 4 -> processTime = value;
                case 5 -> collisionPrevention = value != 0;
                case 6 -> processing = value != 0;
                case 7 -> fluidAmount = value;
                case 8 -> fluidType = value < 0 || value >= ItemSolderingFluidCell.FluidType.values().length ? null : ItemSolderingFluidCell.FluidType.values()[value];
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    public SolderingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLDERING_STATION.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolderingStationBlockEntity station) {
        station.chargeFromBattery();
        station.pullEnergyFromNetwork();

        SolderingStationRecipes.Recipe recipe = SolderingStationRecipes.find(station.inventory);
        station.display = recipe == null ? ItemStack.EMPTY : recipe.resultStack();
        station.updateRecipeParameters(recipe);

        boolean wasProcessing = station.processing;
        station.processing = recipe != null && station.canProcess(recipe);

        if (station.processing) {
            int overdrive = station.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
            station.energy -= station.consumption;
            station.progress += 1 + overdrive;

            if (level.getGameTime() % 20L == 0L) {
                station.spawnSolderingEffect((ServerLevel) level, state);
            }

            if (station.progress >= station.processTime) {
                station.progress = 0;
                station.consumeIngredients(recipe);
                station.consumeFluid(recipe);
                station.insertOutput(recipe.resultStack());
            }
        } else {
            station.progress = 0;
        }

        if (wasProcessing != station.processing || level.getGameTime() % 5L == 0L) {
            station.setChangedAndSync();
        } else {
            station.setChanged();
        }
    }

    private void updateRecipeParameters(@Nullable SolderingStationRecipes.Recipe recipe) {
        if (recipe == null) {
            consumption = 100;
            processTime = 1;
            maxEnergy = Math.max(DEFAULT_MAX_ENERGY, energy);
            return;
        }

        int speed = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int power = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdrive = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        processTime = Math.max(1, recipe.duration() - recipe.duration() * speed / 6 + recipe.duration() * power / 3);
        double speedMultiplier = 1.0D + speed * 0.5D;
        double powerMultiplier = Math.max(0.25D, 1.0D - power * 0.25D);
        long calculatedConsumption = Math.max(1L, (long) Math.ceil(recipe.energyPerTick() * speedMultiplier * powerMultiplier));
        calculatedConsumption *= 1L << Math.min(overdrive, 20);
        consumption = (int) Math.clamp(calculatedConsumption, 1L, Integer.MAX_VALUE);
        maxEnergy = Math.max(Math.max(DEFAULT_MAX_ENERGY, consumption * 20), energy);
    }

    private int getUpgradeLevel(ItemMachineUpgrade.UpgradeType type) {
        int level = 0;
        for (int slot = SLOT_UPGRADE_1; slot <= SLOT_UPGRADE_2; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == type) {
                level += upgrade.getLevel();
            }
        }
        return Math.min(level, 3);
    }

    private boolean canProcess(SolderingStationRecipes.Recipe recipe) {
        if (energy < consumption || !canAcceptOutput(recipe.resultStack())) {
            return false;
        }
        if (recipe.fluid() != null) {
            if (fluidType != recipe.fluid().type() || fluidAmount < recipe.fluid().amount()) {
                return false;
            }
        } else if (collisionPrevention && fluidAmount > 0) {
            return false;
        }
        return true;
    }

    private void consumeIngredients(SolderingStationRecipes.Recipe recipe) {
        consumeGroup(SLOT_TOPPING_START, SLOT_TOPPING_COUNT, recipe.toppings());
        consumeGroup(SLOT_PCB_START, SLOT_PCB_COUNT, recipe.pcb());
        consumeGroup(SLOT_SOLDER, 1, recipe.solder());
    }

    private void consumeGroup(int firstSlot, int slotCount, List<SolderingStationRecipes.Ingredient> ingredients) {
        List<SolderingStationRecipes.Ingredient> remaining = new java.util.ArrayList<>(ingredients);
        for (int slot = firstSlot; slot < firstSlot + slotCount; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty()) {
                continue;
            }

            SolderingStationRecipes.Ingredient match = null;
            for (SolderingStationRecipes.Ingredient ingredient : remaining) {
                if (input.getCount() >= ingredient.count() && ItemStack.isSameItemSameComponents(input, ingredient.stack().get())) {
                    match = ingredient;
                    break;
                }
            }
            if (match != null) {
                input.shrink(match.count());
                inventory.setStackInSlot(slot, input.isEmpty() ? ItemStack.EMPTY : input);
                remaining.remove(match);
            }
        }
    }

    private void consumeFluid(SolderingStationRecipes.Recipe recipe) {
        if (recipe.fluid() == null) {
            return;
        }
        fluidAmount -= recipe.fluid().amount();
        if (fluidAmount <= 0) {
            fluidAmount = 0;
            fluidType = null;
        }
    }

    private boolean canAcceptOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void insertOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
    }

    private void spawnSolderingEffect(ServerLevel level, BlockState state) {
        Direction facing = state.getValue(SolderingStationBlock.FACING);
        Direction right = facing.getClockWise();
        double x = worldPosition.getX() + 0.5D - facing.getStepX() * 0.5D + right.getStepX() * 0.5D;
        double y = worldPosition.getY() + 1.125D;
        double z = worldPosition.getZ() + 0.5D - facing.getStepZ() * 0.5D + right.getStepZ() * 0.5D;
        level.sendParticles(ModParticles.SOLDER_TAU.get(), x, y, z, 3, 0.05D, 0.03D, 0.05D, 0.0D);
    }

    private void chargeFromBattery() {
        ItemStack stack = inventory.getStackInSlot(SLOT_BATTERY);
        if (!stack.is(ModItems.BATTERY_PACK.get()) || energy >= maxEnergy) {
            return;
        }
        energy += ItemBatteryPack.extractEnergy(stack, Math.min(1_000, maxEnergy - energy));
    }

    private void pullEnergyFromNetwork() {
        if (level == null || energy >= maxEnergy) {
            return;
        }
        BatterySocketBlockEntity source = findPowerSource();
        if (source != null) {
            energy += source.extractEnergyForMachine(Math.min(1_000, maxEnergy - energy));
        }
    }

    @Nullable
    private BatterySocketBlockEntity findPowerSource() {
        if (level == null) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Direction facing = getBlockState().getValue(SolderingStationBlock.FACING);
        for (BlockPos part : SolderingStationBlock.getAllPositions(worldPosition, facing)) {
            for (Direction direction : Direction.values()) {
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
            BlockPos controller = null;
            if (state.is(ModBlocks.MACHINE_BATTERY_SOCKET.get())) {
                controller = current;
            } else if (state.is(ModBlocks.MACHINE_BATTERY_SOCKET_DUMMY.get())) {
                controller = BatterySocketDummyBlock.findController(level, current);
            }
            if (controller != null && controllers.add(controller) && level.getBlockEntity(controller) instanceof BatterySocketBlockEntity socket && socket.canOutput() && socket.getEnergy() > 0) {
                return socket;
            }
        }
        return null;
    }

    private void handleFluidCell() {
        ItemStack stack = inventory.getStackInSlot(SLOT_FLUID_CELL);
        if (stack.isEmpty()) {
            return;
        }
        handlingFluidSlot = true;
        try {
            if (stack.getItem() instanceof ItemSolderingFluidCell cell) {
                if ((fluidType == null || fluidType == cell.getFluidType()) && fluidAmount + FLUID_CELL_AMOUNT <= FLUID_CAPACITY) {
                    fluidType = cell.getFluidType();
                    fluidAmount += FLUID_CELL_AMOUNT;
                    inventory.setStackInSlot(SLOT_FLUID_CELL, new ItemStack(ModItems.CELL_EMPTY.get()));
                }
            } else if (stack.is(ModItems.CELL_EMPTY.get()) && fluidType != null && fluidAmount >= FLUID_CELL_AMOUNT) {
                Item fluidCell = getFluidCellItem(fluidType);
                fluidAmount -= FLUID_CELL_AMOUNT;
                inventory.setStackInSlot(SLOT_FLUID_CELL, new ItemStack(fluidCell));
                if (fluidAmount == 0) {
                    fluidType = null;
                }
            }
        } finally {
            handlingFluidSlot = false;
        }
    }

    private static Item getFluidCellItem(ItemSolderingFluidCell.FluidType type) {
        return switch (type) {
            case SULFURIC_ACID -> ModItems.CELL_SULFURIC_ACID.get();
            case PEROXIDE -> ModItems.CELL_PEROXIDE.get();
            case SOLVENT -> ModItems.CELL_SOLVENT.get();
            case HELIUM4 -> ModItems.CELL_HELIUM4.get();
            case PERFLUOROMETHYL -> ModItems.CELL_PERFLUOROMETHYL.get();
            case PERFLUOROMETHYL_COLD -> ModItems.CELL_PERFLUOROMETHYL_COLD.get();
        };
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getAutomationInventory() {
        return automationInventory;
    }

    public ContainerData getData() {
        return data;
    }

    public ItemStack getDisplayedStack() {
        return display.copy();
    }

    public int getFluidColor() {
        return fluidType == null ? 0 : fluidType.getColor();
    }

    public void toggleCollisionPrevention() {
        collisionPrevention = !collisionPrevention;
        setChangedAndSync();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.machine_soldering_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SolderingStationMenu(containerId, playerInventory, this);
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack);
            }
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy);
        tag.putInt("MaxEnergy", maxEnergy);
        tag.putInt("Consumption", consumption);
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", processTime);
        tag.putBoolean("CollisionPrevention", collisionPrevention);
        tag.putBoolean("Processing", processing);
        tag.putInt("FluidAmount", fluidAmount);
        tag.putInt("FluidType", fluidType == null ? -1 : fluidType.ordinal());
        tag.put("Display", display.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loading = true;
        try {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        } finally {
            loading = false;
        }
        energy = tag.getInt("Energy");
        maxEnergy = Math.max(DEFAULT_MAX_ENERGY, tag.getInt("MaxEnergy"));
        consumption = Math.max(1, tag.getInt("Consumption"));
        progress = tag.getInt("Progress");
        processTime = Math.max(1, tag.getInt("ProcessTime"));
        collisionPrevention = tag.getBoolean("CollisionPrevention");
        processing = tag.getBoolean("Processing");
        fluidAmount = tag.getInt("FluidAmount");
        int fluidId = tag.getInt("FluidType");
        fluidType = fluidId < 0 || fluidId >= ItemSolderingFluidCell.FluidType.values().length ? null : ItemSolderingFluidCell.FluidType.values()[fluidId];
        display = ItemStack.parseOptional(registries, tag.getCompound("Display"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}