package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModParticles;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.ArcWelderBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifier;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.menu.ArcWelderMenu;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ArcWelderBlockEntity extends BlockEntity implements MenuProvider, FluidNode {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_INPUT_3 = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_FLUID_IDENTIFIER = 5;
    public static final int SLOT_UPGRADE_1 = 6;
    public static final int SLOT_UPGRADE_2 = 7;
    public static final int INVENTORY_SIZE = 8;
    public static final int DEFAULT_MAX_ENERGY = 2_000;
    public static final int FLUID_CAPACITY = 24_000;

    private boolean loading;
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= SLOT_INPUT_1 && slot <= SLOT_INPUT_3) {
                return ArcWelderRecipes.isValidInputForSlot(slot, stack);
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (slot == SLOT_BATTERY) {
                return ItemBatteryPack.isBattery(stack);
            }
            if (slot == SLOT_FLUID_IDENTIFIER) {
                return stack.getItem() instanceof ItemFluidIdentifier || stack.getItem() instanceof ItemFluidIdentifierMulti;
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
            if (!loading && (slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) && level != null && !level.isClientSide() && !getStackInSlot(slot).isEmpty()) {
                level.playSound(null, worldPosition, ModSounds.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if (!loading) {
                setChangedAndSync();
            }
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
            if (slot < SLOT_INPUT_1 || slot > SLOT_INPUT_3 || !inventory.isItemValid(slot, stack)) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= SLOT_INPUT_1 && slot <= SLOT_INPUT_3 && inventory.isItemValid(slot, stack);
        }
    };

    private int energy;
    private int maxEnergy = DEFAULT_MAX_ENERGY;
    private int consumption = 100;
    private int progress;
    private int processTime = 1;
    private boolean processing;
    private NTMFluidType fluidType;
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
                case 5 -> processing ? 1 : 0;
                case 6 -> fluidAmount;
                case 7 -> fluidType == null ? -1 : fluidType.ordinal();
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
                case 5 -> processing = value != 0;
                case 6 -> fluidAmount = value;
                case 7 -> fluidType = value < 0 || value >= NTMFluidType.values().length ? null : NTMFluidType.values()[value];
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public ArcWelderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARC_WELDER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcWelderBlockEntity welder) {
        welder.updateFluidSelection();
        welder.chargeFromBattery();
        welder.pullEnergyFromNetwork();

        ArcWelderRecipes.Recipe recipe = ArcWelderRecipes.find(welder.inventory);
        ItemStack previousDisplay = welder.display;
        welder.display = recipe == null ? ItemStack.EMPTY : recipe.output().copyWithCount(1);
        welder.updateRecipeParameters(recipe);

        boolean displayChanged = !ItemStack.isSameItemSameComponents(previousDisplay, welder.display);
        boolean wasProcessing = welder.processing;
        welder.processing = recipe != null && welder.canProcess(recipe);

        if (welder.processing) {
            int overdrive = welder.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
            welder.energy -= welder.consumption;
            welder.progress += 1 + overdrive;

            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 2L == 0L) {
                welder.spawnWeldingParticles(serverLevel, state);
                if (level.getGameTime() % 20L == 0L) {
                    level.playSound(null, pos, ModSounds.SPARK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }

            if (welder.progress >= welder.processTime) {
                welder.progress = 0;
                welder.consumeRecipe(recipe);
                welder.insertOutput(recipe.output());
            }
        } else {
            welder.progress = 0;
        }

        if (displayChanged || wasProcessing != welder.processing || level.getGameTime() % 5L == 0L) {
            welder.setChangedAndSync();
        } else {
            welder.setChanged();
        }
    }

    private void updateRecipeParameters(@Nullable ArcWelderRecipes.Recipe recipe) {
        if (recipe == null) {
            consumption = 100;
            processTime = 1;
            maxEnergy = Math.max(DEFAULT_MAX_ENERGY, energy);
            return;
        }
        int speed = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int powerUpgrade = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdrive = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        processTime = Math.max(1, recipe.duration() - recipe.duration() * speed / 6 + recipe.duration() * powerUpgrade / 3);
        long calculatedConsumption = recipe.consumption() + (long) recipe.consumption() * speed - (long) recipe.consumption() * powerUpgrade / 6;
        calculatedConsumption *= 1L << overdrive;
        consumption = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, calculatedConsumption));
        maxEnergy = Math.max(DEFAULT_MAX_ENERGY, Math.max(energy, consumption * 20));
    }

    private boolean canProcess(ArcWelderRecipes.Recipe recipe) {
        if (energy < consumption) {
            return false;
        }
        if (recipe.fluid() != null && (fluidType != recipe.fluid() || fluidAmount < recipe.fluidAmount())) {
            return false;
        }
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        ItemStack result = recipe.output();
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void consumeRecipe(ArcWelderRecipes.Recipe recipe) {
        List<ArcWelderRecipes.Ingredient> remaining = new ArrayList<>(recipe.ingredients());
        for (int slot = SLOT_INPUT_1; slot <= SLOT_INPUT_3 && !remaining.isEmpty(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            for (int i = 0; i < remaining.size(); i++) {
                ArcWelderRecipes.Ingredient ingredient = remaining.get(i);
                if (ingredient.matches(stack)) {
                    inventory.extractItem(slot, ingredient.count(), false);
                    remaining.remove(i);
                    break;
                }
            }
        }
        if (recipe.fluid() != null) {
            fluidAmount -= recipe.fluidAmount();
            if (fluidAmount <= 0) {
                fluidAmount = 0;
                fluidType = selectedFluidType();
            }
        }
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

    private void spawnWeldingParticles(ServerLevel level, BlockState state) {
        Direction facing = state.getValue(ArcWelderBlock.FACING);
        double x = worldPosition.getX() + 0.5D - facing.getStepX() * 0.5D;
        double y = worldPosition.getY() + 1.25D;
        double z = worldPosition.getZ() + 0.5D - facing.getStepZ() * 0.5D;
        int count = level.getGameTime() % 20L == 0L ? 5 : 2;
        level.sendParticles(ModParticles.SOLDER_TAU.get(), x, y, z, count, 0.08D, 0.04D, 0.08D, 0.0D);
    }

    private void updateFluidSelection() {
        NTMFluidType selected = selectedFluidType();
        if (fluidAmount <= 0 && fluidType != selected) {
            fluidType = selected;
            setChangedAndSync();
        }
    }

    @Nullable
    private NTMFluidType selectedFluidType() {
        ItemStack identifier = inventory.getStackInSlot(SLOT_FLUID_IDENTIFIER);
        if (identifier.getItem() instanceof ItemFluidIdentifier fluidIdentifier) {
            return fluidIdentifier.getFluidType();
        }
        if (identifier.getItem() instanceof ItemFluidIdentifierMulti) {
            return ItemFluidIdentifierMulti.getType(identifier, true);
        }
        return null;
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

    private void chargeFromBattery() {
        ItemStack stack = inventory.getStackInSlot(SLOT_BATTERY);
        if (!ItemBatteryPack.isBattery(stack) || energy >= maxEnergy) {
            return;
        }
        energy += ItemBatteryPack.extractEnergy(stack, Math.min(1_000, maxEnergy - energy));
    }

    private void pullEnergyFromNetwork() {
        if (level == null || energy >= maxEnergy) {
            return;
        }
        MachineEnergySource source = findPowerSource();
        if (source != null) {
            energy += source.extractEnergyForMachine(Math.min(1_000, maxEnergy - energy));
        }
    }

    @Nullable
    private MachineEnergySource findPowerSource() {
        if (level == null) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Direction facing = getBlockState().getValue(ArcWelderBlock.FACING);
        for (BlockPos part : ArcWelderBlock.getAllPositions(worldPosition, facing)) {
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
            } else if (state.is(ModBlocks.WOOD_BURNER.get())) {
                controller = current;
            } else if (state.is(ModBlocks.WOOD_BURNER_DUMMY.get()) && level.getBlockEntity(current) instanceof WoodBurnerDummyBlockEntity dummy) {
                controller = dummy.getController();
            }
            if (controller != null && controllers.add(controller)) {
                BlockEntity sourceEntity = level.getBlockEntity(controller);
                if (sourceEntity instanceof BatterySocketBlockEntity socket && socket.canOutput() && socket.getStoredEnergy() > 0) {
                    return socket;
                }
                if (sourceEntity instanceof MachineEnergySource source && source.getStoredEnergy() > 0) {
                    return source;
                }
            }
        }
        return null;
    }

    @Override
    public NTMFluidType getFluidType() {
        return fluidType;
    }

    @Override
    public int getFluidAmount() {
        return fluidAmount;
    }

    @Override
    public int getFluidCapacity() {
        return FLUID_CAPACITY;
    }

    @Override
    public int fill(NTMFluidType type, int amount) {
        if (type == null || amount <= 0 || !accepts(type)) {
            return 0;
        }
        if (fluidAmount > 0 && fluidType != type) {
            return 0;
        }
        int accepted = Math.min(amount, FLUID_CAPACITY - fluidAmount);
        if (accepted <= 0) {
            return 0;
        }
        fluidType = type;
        fluidAmount += accepted;
        setChangedAndSync();
        return accepted;
    }

    @Override
    public int drain(NTMFluidType type, int amount) {
        if (type == null || fluidType != type || amount <= 0) {
            return 0;
        }
        int drained = Math.min(amount, fluidAmount);
        fluidAmount -= drained;
        if (fluidAmount <= 0) {
            fluidAmount = 0;
            fluidType = selectedFluidType();
        }
        setChangedAndSync();
        return drained;
    }

    @Override
    public boolean accepts(NTMFluidType type) {
        NTMFluidType selected = selectedFluidType();
        return selected != null && selected == type;
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
                level.addFreshEntity(entity);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
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

    public boolean isProcessing() {
        return processing;
    }

    public ItemStack getDisplayedStack() {
        return display.copy();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.arc_welder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ArcWelderMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy);
        tag.putInt("maxEnergy", maxEnergy);
        tag.putInt("consumption", consumption);
        tag.putInt("progress", progress);
        tag.putInt("processTime", processTime);
        tag.putBoolean("processing", processing);
        tag.putInt("fluidAmount", fluidAmount);
        if (fluidType != null) {
            tag.putString("fluidType", fluidType.id());
        }
        tag.put("display", display.saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loading = true;
        try {
            if (tag.contains("inventory")) {
                inventory.deserializeNBT(registries, tag.getCompound("inventory"));
            }
        } finally {
            loading = false;
        }
        energy = tag.getInt("energy");
        maxEnergy = Math.max(DEFAULT_MAX_ENERGY, tag.getInt("maxEnergy"));
        consumption = Math.max(1, tag.getInt("consumption"));
        progress = tag.getInt("progress");
        processTime = Math.max(1, tag.getInt("processTime"));
        processing = tag.getBoolean("processing");
        fluidAmount = tag.getInt("fluidAmount");
        fluidType = tag.contains("fluidType") ? NTMFluidType.byId(tag.getString("fluidType")) : null;
        display = ItemStack.parseOptional(registries, tag.getCompound("display"));
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

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
