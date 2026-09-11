package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemEmptyPortableFluidContainer;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.item.ItemPortableFluidContainer;
import com.yellowyotu.hbmneoforge.menu.ChemicalPlantMenu;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ChemicalPlantBlockEntity extends BlockEntity implements MenuProvider, FluidNode {
    public static final int SLOT_BATTERY = 0;
    public static final int INPUT_START = 1;
    public static final int INPUT_COUNT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int FLUID_SLOT_COUNT = 3;
    public static final int SLOT_FLUID_INPUT_CONTAINER_START = 5;
    public static final int SLOT_FLUID_INPUT_OUTPUT_START = SLOT_FLUID_INPUT_CONTAINER_START + FLUID_SLOT_COUNT;
    public static final int SLOT_UPGRADE_1 = SLOT_FLUID_INPUT_OUTPUT_START + FLUID_SLOT_COUNT;
    public static final int SLOT_UPGRADE_2 = SLOT_UPGRADE_1 + 1;
    public static final int SLOT_FLUID_OUTPUT_CONTAINER_START = SLOT_UPGRADE_2 + 1;
    public static final int SLOT_FLUID_OUTPUT_START = SLOT_FLUID_OUTPUT_CONTAINER_START + FLUID_SLOT_COUNT;
    public static final int INVENTORY_SIZE = SLOT_FLUID_OUTPUT_START + FLUID_SLOT_COUNT;
    public static final int MAX_ENERGY = 100_000;
    public static final int FLUID_CAPACITY = 24_000;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return ItemBatteryPack.isBattery(stack);
            }
            if (slot >= INPUT_START && slot < INPUT_START + INPUT_COUNT) {
                return true;
            }
            if (slot >= SLOT_FLUID_INPUT_CONTAINER_START && slot < SLOT_FLUID_INPUT_CONTAINER_START + FLUID_SLOT_COUNT) {
                return stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET) || stack.getItem() instanceof ItemPortableFluidContainer;
            }
            if (slot >= SLOT_FLUID_OUTPUT_CONTAINER_START && slot < SLOT_FLUID_OUTPUT_CONTAINER_START + FLUID_SLOT_COUNT) {
                return stack.is(Items.BUCKET) || stack.getItem() instanceof ItemEmptyPortableFluidContainer;
            }
            if (slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };

    private final EnumMap<NTMFluidType, Integer> fluids = new EnumMap<>(NTMFluidType.class);
    private int energy;
    private int progress;
    private int selectedRecipe;
    private boolean processing;
    private long lastOperatingSoundTick = -1L;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            ChemicalPlantRecipes.Recipe recipe = getSelectedRecipe();
            return switch (index) {
                case 0 -> energy;
                case 1 -> MAX_ENERGY;
                case 2 -> progress;
                case 3 -> recipe == null ? 0 : recipe.duration();
                case 4 -> selectedRecipe;
                case 5 -> processing ? 1 : 0;
                case 6, 8, 10 -> getDisplayedFluidAmount(recipe, true, (index - 6) / 2);
                case 7, 9, 11 -> getDisplayedFluidType(recipe, true, (index - 7) / 2);
                case 12, 14, 16 -> getDisplayedFluidAmount(recipe, false, (index - 12) / 2);
                case 13, 15, 17 -> getDisplayedFluidType(recipe, false, (index - 13) / 2);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                energy = value;
            } else if (index == 2) {
                progress = value;
            } else if (index == 4) {
                selectedRecipe = value;
            }
        }

        @Override
        public int getCount() {
            return 18;
        }
    };

    public ChemicalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_PLANT.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }
    public ChemicalPlantRecipes.Recipe getSelectedRecipe() { return selectedRecipe >= 0 && selectedRecipe < ChemicalPlantRecipes.RECIPES.size() ? ChemicalPlantRecipes.RECIPES.get(selectedRecipe) : null; }
    public boolean isProcessing() { return processing; }

    public void selectRecipe(int index) {
        if (ChemicalPlantRecipes.RECIPES.isEmpty() || index < 0) {
            selectedRecipe = -1;
        } else {
            selectedRecipe = Math.floorMod(index, ChemicalPlantRecipes.RECIPES.size());
        }
        progress = 0;
        setChangedAndSync();
    }

    private static void updateVisualState(Level level, BlockPos pos, BlockState state, boolean processing) {
        if (!(state.getBlock() instanceof com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock)) {
            return;
        }
        boolean frame = !level.getBlockState(pos.above(3)).isAir();
        if (state.getValue(com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock.FRAME) != frame || state.getValue(com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock.PROCESSING) != processing) {
            level.setBlock(pos, state.setValue(com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock.FRAME, frame).setValue(com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock.PROCESSING, processing), Block.UPDATE_CLIENTS);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChemicalPlantBlockEntity machine) {
        machine.chargeFromBattery();
        machine.pullEnergyFromNetwork();
        machine.handleFluidContainers();

        ChemicalPlantRecipes.Recipe recipe = machine.getSelectedRecipe();
        if (recipe == null) {
            machine.processing = false;
            machine.progress = 0;
            machine.lastOperatingSoundTick = -1L;
            updateVisualState(level, pos, state, false);
            machine.setChangedAndSync();
            return;
        }

        boolean canCraft = machine.hasIngredients(recipe) && machine.hasFluids(recipe) && machine.canAcceptOutput(recipe.resultStack()) && machine.canAcceptFluidOutputs(recipe);
        int speedLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int powerLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdriveLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        double speed = 1.0D + Math.min(speedLevel, 3) / 3.0D + Math.min(overdriveLevel, 3);
        double powerMultiplier = 1.0D - Math.min(powerLevel, 3) * 0.25D + Math.min(speedLevel, 3) + Math.min(overdriveLevel, 3) * 10.0D / 3.0D;
        int consumption = Math.max(1, (int) Math.ceil(recipe.energyPerTick() * powerMultiplier));

        if (canCraft && machine.energy >= consumption) {
            machine.energy -= consumption;
            machine.progress += Math.max(1, (int) Math.floor(speed));
            machine.processing = true;
            if (machine.progress >= recipe.duration()) {
                machine.consumeIngredients(recipe);
                machine.consumeFluids(recipe);
                machine.insertOutput(recipe.resultStack());
                machine.insertFluidOutputs(recipe);
                machine.progress = 0;
            }
        } else {
            machine.processing = false;
            machine.progress = 0;
            machine.lastOperatingSoundTick = -1L;
        }
        updateVisualState(level, pos, state, machine.processing);
        machine.setChangedAndSync();
    }

    private void handleFluidContainers() {
        for (int tank = 0; tank < FLUID_SLOT_COUNT; tank++) {
            handleFluidInputContainer(tank);
            handleFluidOutputContainer(tank);
        }
    }

    private void handleFluidInputContainer(int tank) {
        int inputSlot = SLOT_FLUID_INPUT_CONTAINER_START + tank;
        int outputSlot = SLOT_FLUID_INPUT_OUTPUT_START + tank;
        if (level == null || level.isClientSide() || !inventory.getStackInSlot(outputSlot).isEmpty()) {
            return;
        }
        NTMFluidType expectedType = getRecipeFluidType(true, tank);
        if (expectedType == null) {
            return;
        }
        ItemStack input = inventory.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return;
        }
        if (input.is(Items.WATER_BUCKET) && expectedType == NTMFluidType.WATER && fill(NTMFluidType.WATER, 1_000) == 1_000) {
            inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
            inventory.setStackInSlot(outputSlot, new ItemStack(Items.BUCKET));
            return;
        }
        if (input.is(Items.LAVA_BUCKET) && expectedType == NTMFluidType.LAVA && fill(NTMFluidType.LAVA, 1_000) == 1_000) {
            inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
            inventory.setStackInSlot(outputSlot, new ItemStack(Items.BUCKET));
            return;
        }
        if (input.getItem() instanceof ItemPortableFluidContainer container) {
            NTMFluidType type = ItemPortableFluidContainer.getFluidType(input);
            int stored = ItemPortableFluidContainer.getAmount(input);
            if (type != expectedType || stored <= 0 || FLUID_CAPACITY - getAmount(type) < stored || fill(type, stored) != stored) {
                return;
            }
            input.shrink(1);
            inventory.setStackInSlot(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
            inventory.setStackInSlot(outputSlot, new ItemStack(container.getEmptyItem()));
        }
    }

    private void handleFluidOutputContainer(int tank) {
        int inputSlot = SLOT_FLUID_OUTPUT_CONTAINER_START + tank;
        int outputSlot = SLOT_FLUID_OUTPUT_START + tank;
        if (level == null || level.isClientSide() || !inventory.getStackInSlot(outputSlot).isEmpty()) {
            return;
        }
        ItemStack input = inventory.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return;
        }
        NTMFluidType type = getRecipeFluidType(false, tank);
        if (type == null || getAmount(type) <= 0) {
            return;
        }
        if (input.is(Items.BUCKET)) {
            if (type != NTMFluidType.WATER && type != NTMFluidType.LAVA) {
                return;
            }
            int moved = drain(type, 1_000);
            if (moved < 1_000) {
                if (moved > 0) {
                    fill(type, moved);
                }
                return;
            }
            input.shrink(1);
            inventory.setStackInSlot(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
            inventory.setStackInSlot(outputSlot, new ItemStack(type == NTMFluidType.WATER ? Items.WATER_BUCKET : Items.LAVA_BUCKET));
            return;
        }
        if (input.getItem() instanceof ItemEmptyPortableFluidContainer empty) {
            int moved = drain(type, Math.min(empty.getCapacity(), getAmount(type)));
            if (moved <= 0) {
                return;
            }
            ItemStack filled = new ItemStack(empty.getFilledItem());
            ItemPortableFluidContainer.setFluid(filled, type, moved);
            input.shrink(1);
            inventory.setStackInSlot(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
            inventory.setStackInSlot(outputSlot, filled);
        }
    }

    private NTMFluidType getRecipeFluidType(boolean input, int tank) {
        ChemicalPlantRecipes.Recipe recipe = getSelectedRecipe();
        if (recipe == null) {
            return null;
        }
        java.util.List<ChemicalPlantRecipes.FluidIngredient> fluids = input ? recipe.inputFluids() : recipe.outputFluids();
        return tank >= 0 && tank < fluids.size() ? fluids.get(tank).type() : null;
    }

    private boolean hasIngredients(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.Ingredient ingredient : recipe.ingredients()) {
            int remaining = ingredient.count();
            for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (found.is(ingredient.item())) {
                    remaining -= found.getCount();
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasFluids(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.inputFluids()) {
            if (getAmount(fluid.type()) < fluid.amount()) {
                return false;
            }
        }
        return true;
    }

    private boolean canAcceptFluidOutputs(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.outputFluids()) {
            if (getAmount(fluid.type()) + fluid.amount() > FLUID_CAPACITY) {
                return false;
            }
        }
        return true;
    }

    private void consumeIngredients(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.Ingredient ingredient : recipe.ingredients()) {
            int remaining = ingredient.count();
            for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT && remaining > 0; slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (!found.is(ingredient.item())) {
                    continue;
                }
                int removed = Math.min(remaining, found.getCount());
                found.shrink(removed);
                remaining -= removed;
                inventory.setStackInSlot(slot, found.isEmpty() ? ItemStack.EMPTY : found);
            }
        }
    }

    private void consumeFluids(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.inputFluids()) {
            setAmount(fluid.type(), getAmount(fluid.type()) - fluid.amount());
        }
    }

    private void insertFluidOutputs(ChemicalPlantRecipes.Recipe recipe) {
        for (ChemicalPlantRecipes.FluidIngredient fluid : recipe.outputFluids()) {
            setAmount(fluid.type(), getAmount(fluid.type()) + fluid.amount());
        }
    }

    private boolean canAcceptOutput(ItemStack result) {
        if (result.isEmpty()) {
            return true;
        }
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void insertOutput(ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
    }

    private int getUpgradeLevel(ItemMachineUpgrade.UpgradeType type) {
        int level = 0;
        for (int slot : new int[]{SLOT_UPGRADE_1, SLOT_UPGRADE_2}) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == type) {
                level += upgrade.getLevel();
            }
        }
        return Math.min(level, 3);
    }

    private int getDisplayedFluidAmount(ChemicalPlantRecipes.Recipe recipe, boolean input, int slot) {
        if (recipe == null) {
            return 0;
        }
        java.util.List<ChemicalPlantRecipes.FluidIngredient> list = input ? recipe.inputFluids() : recipe.outputFluids();
        return slot >= 0 && slot < list.size() ? getAmount(list.get(slot).type()) : 0;
    }

    private int getDisplayedFluidType(ChemicalPlantRecipes.Recipe recipe, boolean input, int slot) {
        if (recipe == null) {
            return -1;
        }
        java.util.List<ChemicalPlantRecipes.FluidIngredient> list = input ? recipe.inputFluids() : recipe.outputFluids();
        return slot >= 0 && slot < list.size() ? list.get(slot).type().ordinal() : -1;
    }

    private int getAmount(NTMFluidType type) { return type == null ? 0 : fluids.getOrDefault(type, 0); }
    private void setAmount(NTMFluidType type, int amount) {
        if (amount <= 0) {
            fluids.remove(type);
        } else {
            fluids.put(type, Math.min(FLUID_CAPACITY, amount));
        }
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

    private MachineEnergySource findPowerSource() {
        if (level == null) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos part = worldPosition.offset(x, y, z);
                    for (Direction direction : Direction.values()) {
                        queue.add(part.relative(direction));
                    }
                }
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

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public NTMFluidType getFluidType() {
        for (Map.Entry<NTMFluidType, Integer> entry : fluids.entrySet()) {
            if (entry.getValue() > 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override public int getFluidAmount() { NTMFluidType type = getFluidType(); return type == null ? 0 : getAmount(type); }
    @Override public int getFluidCapacity() { return FLUID_CAPACITY; }

    @Override
    public int fill(NTMFluidType type, int amount) {
        if (!accepts(type) || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, FLUID_CAPACITY - getAmount(type));
        if (accepted > 0) {
            setAmount(type, getAmount(type) + accepted);
            setChangedAndSync();
        }
        return accepted;
    }

    @Override
    public int drain(NTMFluidType type, int amount) {
        if (type == null || amount <= 0) {
            return 0;
        }
        int drained = Math.min(amount, getAmount(type));
        if (drained > 0) {
            setAmount(type, getAmount(type) - drained);
            setChangedAndSync();
        }
        return drained;
    }

    @Override public boolean accepts(NTMFluidType type) { return type != null && getAmount(type) < FLUID_CAPACITY; }

    @Override public Component getDisplayName() { return Component.translatable("container.hbm_neoforge.chemical_plant"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new ChemicalPlantMenu(id, inventory, this); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy);
        tag.putInt("Progress", progress);
        tag.putInt("Recipe", selectedRecipe);
        tag.putBoolean("Processing", processing);
        ListTag fluidList = new ListTag();
        for (Map.Entry<NTMFluidType, Integer> entry : fluids.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            CompoundTag fluid = new CompoundTag();
            fluid.putString("Type", entry.getKey().id());
            fluid.putInt("Amount", entry.getValue());
            fluidList.add(fluid);
        }
        tag.put("Fluids", fluidList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            if (inventory.getSlots() != INVENTORY_SIZE) {
                java.util.List<ItemStack> previous = new java.util.ArrayList<>();
                for (int slot = 0; slot < inventory.getSlots(); slot++) {
                    previous.add(inventory.getStackInSlot(slot).copy());
                }
                inventory.setSize(INVENTORY_SIZE);
                for (int slot = 0; slot < Math.min(previous.size(), INVENTORY_SIZE); slot++) {
                    inventory.setStackInSlot(slot, previous.get(slot));
                }
            }
        }
        energy = tag.getInt("Energy");
        progress = tag.getInt("Progress");
        selectedRecipe = tag.contains("Recipe") ? tag.getInt("Recipe") : 0;
        processing = tag.getBoolean("Processing");
        fluids.clear();
        if (tag.contains("Fluids", Tag.TAG_LIST)) {
            ListTag fluidList = tag.getList("Fluids", Tag.TAG_COMPOUND);
            for (int i = 0; i < fluidList.size(); i++) {
                CompoundTag fluid = fluidList.getCompound(i);
                NTMFluidType type = NTMFluidType.byId(fluid.getString("Type"));
                if (type != null) {
                    setAmount(type, fluid.getInt("Amount"));
                }
            }
        } else if (tag.contains("Water")) {
            setAmount(NTMFluidType.WATER, tag.getInt("Water"));
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); saveAdditional(tag, registries); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) { CompoundTag tag = pkt.getTag(); if (tag != null) { loadAdditional(tag, lookupProvider); } }
}
