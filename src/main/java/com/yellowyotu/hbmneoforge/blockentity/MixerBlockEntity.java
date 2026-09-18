package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifier;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.menu.MixerMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
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
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MixerBlockEntity extends BlockEntity implements MenuProvider, FluidNode {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_SOLID_INPUT = 1;
    public static final int SLOT_FLUID_IDENTIFIER = 2;
    public static final int SLOT_UPGRADE_1 = 3;
    public static final int SLOT_UPGRADE_2 = 4;
    public static final int INVENTORY_SIZE = 5;
    public static final int MAX_ENERGY = 10_000;
    public static final int INPUT_CAPACITY = 16_000;
    public static final int OUTPUT_CAPACITY = 24_000;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return ItemBatteryPack.isBattery(stack);
            }
            if (slot == SLOT_SOLID_INPUT) {
                return MixerRecipes.isValidSolidInput(stack);
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
        protected void onContentsChanged(int slot) {
            if ((slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) && level != null && !level.isClientSide() && !getStackInSlot(slot).isEmpty()) {
                level.playSound(null, worldPosition, ModSounds.UPGRADE_PLUG.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            setChangedAndSync();
        }
    };

    private int energy;
    private int progress;
    private int recipeIndex;
    private boolean processing;
    private NTMFluidType inputType1;
    private int inputAmount1;
    private NTMFluidType inputType2;
    private int inputAmount2;
    private NTMFluidType outputType;
    private int outputAmount;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            MixerRecipes.Recipe recipe = getRecipe();
            return switch (index) {
                case 0 -> energy;
                case 1 -> MAX_ENERGY;
                case 2 -> progress;
                case 3 -> recipe == null ? 0 : getEffectiveProcessTime(recipe);
                case 4 -> recipeIndex;
                case 5 -> processing ? 1 : 0;
                case 6 -> inputAmount1;
                case 7 -> inputType1 == null ? -1 : inputType1.ordinal();
                case 8 -> inputAmount2;
                case 9 -> inputType2 == null ? -1 : inputType2.ordinal();
                case 10 -> outputAmount;
                case 11 -> outputType == null ? -1 : outputType.ordinal();
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
                recipeIndex = value;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXER.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isProcessing() {
        return processing;
    }

    public int getTotalFluidAmount() {
        return inputAmount1 + inputAmount2 + outputAmount;
    }

    public int getTotalFluidCapacity() {
        int total = OUTPUT_CAPACITY;
        if (inputType1 != null) {
            total += INPUT_CAPACITY;
        }
        if (inputType2 != null) {
            total += INPUT_CAPACITY;
        }
        return total;
    }

    public NTMFluidType getRenderedFluidType() {
        return outputType != null ? outputType : inputType1 != null ? inputType1 : inputType2;
    }

    public MixerRecipes.Recipe getRecipe() {
        List<MixerRecipes.Recipe> recipes = MixerRecipes.getRecipes(outputType);
        if (recipes.isEmpty()) {
            return null;
        }
        recipeIndex = Math.floorMod(recipeIndex, recipes.size());
        return recipes.get(recipeIndex);
    }

    public void cycleRecipe() {
        List<MixerRecipes.Recipe> recipes = MixerRecipes.getRecipes(outputType);
        if (recipes.size() <= 1) {
            recipeIndex = 0;
        } else {
            recipeIndex = (recipeIndex + 1) % recipes.size();
        }
        progress = 0;
        setChangedAndSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntity machine) {
        machine.updateOutputSelection();
        machine.chargeFromBattery();
        machine.pullEnergyFromNetwork();
        machine.pushOutputFluid();

        MixerRecipes.Recipe recipe = machine.getRecipe();
        boolean canProcess = recipe != null && machine.canProcess(recipe);
        machine.processing = canProcess;

        if (canProcess) {
            int consumption = machine.getConsumption();
            machine.energy -= consumption;
            machine.progress++;
            if (machine.progress >= machine.getEffectiveProcessTime(recipe)) {
                machine.process(recipe);
                machine.progress = 0;
            }
        } else {
            machine.progress = 0;
        }

        if (state.getValue(com.yellowyotu.hbmneoforge.block.MixerBlock.PROCESSING) != machine.processing) {
            level.setBlock(pos, state.setValue(com.yellowyotu.hbmneoforge.block.MixerBlock.PROCESSING, machine.processing), 3);
        }
        machine.setChangedAndSync();
    }

    private void updateOutputSelection() {
        ItemStack identifier = inventory.getStackInSlot(SLOT_FLUID_IDENTIFIER);
        NTMFluidType selected = null;
        if (identifier.getItem() instanceof ItemFluidIdentifier fluidIdentifier) {
            selected = fluidIdentifier.getFluidType();
        } else if (identifier.getItem() instanceof ItemFluidIdentifierMulti) {
            selected = ItemFluidIdentifierMulti.getType(identifier, true);
        }
        if (outputAmount <= 0) {
            if (outputType != selected) {
                outputType = selected;
                recipeIndex = 0;
                progress = 0;
            }
        }
    }

    private boolean canProcess(MixerRecipes.Recipe recipe) {
        configureInputTypes(recipe);
        if (energy < getConsumption()) {
            return false;
        }
        if (outputType != recipe.outputType() || outputAmount + recipe.outputAmount() > OUTPUT_CAPACITY) {
            return false;
        }
        if (!hasFluid(inputType1, inputAmount1, recipe.input1())) {
            return false;
        }
        if (!hasFluid(inputType2, inputAmount2, recipe.input2())) {
            return false;
        }
        return recipe.solidInput() == null || recipe.solidInput().matches(inventory.getStackInSlot(SLOT_SOLID_INPUT));
    }

    private void configureInputTypes(MixerRecipes.Recipe recipe) {
        inputType1 = configureInputType(inputType1, inputAmount1, recipe.input1());
        inputType2 = configureInputType(inputType2, inputAmount2, recipe.input2());
    }

    private static NTMFluidType configureInputType(NTMFluidType current, int amount, MixerRecipes.FluidIngredient ingredient) {
        if (amount <= 0) {
            return ingredient == null ? null : ingredient.type();
        }
        return current;
    }

    private static boolean hasFluid(NTMFluidType currentType, int currentAmount, MixerRecipes.FluidIngredient ingredient) {
        if (ingredient == null) {
            return true;
        }
        return currentType == ingredient.type() && currentAmount >= ingredient.amount();
    }

    private void process(MixerRecipes.Recipe recipe) {
        if (recipe.input1() != null) {
            inputAmount1 -= recipe.input1().amount();
            if (inputAmount1 <= 0) {
                inputAmount1 = 0;
            }
        }
        if (recipe.input2() != null) {
            inputAmount2 -= recipe.input2().amount();
            if (inputAmount2 <= 0) {
                inputAmount2 = 0;
            }
        }
        if (recipe.solidInput() != null) {
            inventory.extractItem(SLOT_SOLID_INPUT, recipe.solidInput().count(), false);
        }
        outputType = recipe.outputType();
        outputAmount = Math.min(OUTPUT_CAPACITY, outputAmount + recipe.outputAmount());
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

    public int getConsumption() {
        int speedLevel = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int powerLevel = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdriveLevel = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        double consumption = 50.0D + speedLevel * 150.0D;
        consumption -= consumption * powerLevel * 0.25D;
        consumption *= overdriveLevel * 3.0D + 1.0D;
        return Math.max(1, (int) consumption);
    }

    private int getEffectiveProcessTime(MixerRecipes.Recipe recipe) {
        int speedLevel = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int overdriveLevel = getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        int time = recipe.processTime();
        time -= time * speedLevel / 4;
        time /= overdriveLevel + 1;
        return Math.max(1, time);
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
        for (int y = 0; y <= 2; y++) {
            BlockPos part = worldPosition.above(y);
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

    private void pushOutputFluid() {
        if (level == null || outputType == null || outputAmount <= 0) {
            return;
        }
        int moved = FluidNetworkUtil.fillNetwork(level, worldPosition, outputType, outputAmount, worldPosition);
        if (moved > 0) {
            outputAmount -= moved;
        }
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
        return outputAmount > 0 ? outputType : null;
    }

    @Override
    public int getFluidAmount() {
        return outputAmount;
    }

    @Override
    public int getFluidCapacity() {
        return OUTPUT_CAPACITY;
    }

    @Override
    public int fill(NTMFluidType type, int amount) {
        if (type == null || amount <= 0) {
            return 0;
        }
        int accepted = 0;
        if (inputType1 == type || inputAmount1 == 0 && expectedInputType(0) == type) {
            if (inputAmount1 == 0) {
                inputType1 = type;
            }
            int move = Math.min(amount, INPUT_CAPACITY - inputAmount1);
            inputAmount1 += move;
            accepted += move;
            amount -= move;
        }
        if (amount > 0 && (inputType2 == type || inputAmount2 == 0 && expectedInputType(1) == type)) {
            if (inputAmount2 == 0) {
                inputType2 = type;
            }
            int move = Math.min(amount, INPUT_CAPACITY - inputAmount2);
            inputAmount2 += move;
            accepted += move;
        }
        if (accepted > 0) {
            setChangedAndSync();
        }
        return accepted;
    }

    private NTMFluidType expectedInputType(int index) {
        MixerRecipes.Recipe recipe = getRecipe();
        if (recipe == null) {
            return null;
        }
        MixerRecipes.FluidIngredient ingredient = index == 0 ? recipe.input1() : recipe.input2();
        return ingredient == null ? null : ingredient.type();
    }

    @Override
    public int drain(NTMFluidType type, int amount) {
        if (type == null || type != outputType || amount <= 0) {
            return 0;
        }
        int drained = Math.min(amount, outputAmount);
        outputAmount -= drained;
        if (outputAmount <= 0) {
            outputAmount = 0;
        }
        if (drained > 0) {
            setChangedAndSync();
        }
        return drained;
    }

    @Override
    public boolean accepts(NTMFluidType type) {
        if (type == null) {
            return false;
        }
        return expectedInputType(0) == type && inputAmount1 < INPUT_CAPACITY || expectedInputType(1) == type && inputAmount2 < INPUT_CAPACITY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.machine_mixer");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MixerMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy);
        tag.putInt("Progress", progress);
        tag.putInt("Recipe", recipeIndex);
        tag.putBoolean("Processing", processing);
        saveTank(tag, "Input1", inputType1, inputAmount1);
        saveTank(tag, "Input2", inputType2, inputAmount2);
        saveTank(tag, "Output", outputType, outputAmount);
    }

    private static void saveTank(CompoundTag tag, String prefix, NTMFluidType type, int amount) {
        if (type != null) {
            tag.putString(prefix + "Type", type.id());
        }
        tag.putInt(prefix + "Amount", amount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        energy = tag.getInt("Energy");
        progress = tag.getInt("Progress");
        recipeIndex = tag.getInt("Recipe");
        processing = tag.getBoolean("Processing");
        inputType1 = NTMFluidType.byId(tag.getString("Input1Type"));
        inputAmount1 = tag.getInt("Input1Amount");
        inputType2 = NTMFluidType.byId(tag.getString("Input2Type"));
        inputAmount2 = tag.getInt("Input2Amount");
        outputType = NTMFluidType.byId(tag.getString("OutputType"));
        outputAmount = tag.getInt("OutputAmount");
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookupProvider) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, lookupProvider);
        }
    }
}
