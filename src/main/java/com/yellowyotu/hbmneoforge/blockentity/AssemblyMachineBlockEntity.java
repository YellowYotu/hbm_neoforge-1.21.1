package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemMachineUpgrade;
import com.yellowyotu.hbmneoforge.menu.AssemblyMachineMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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

public final class AssemblyMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FILTER = 1;
    public static final int SLOT_UPGRADE_1 = 2;
    public static final int SLOT_UPGRADE_2 = 3;
    public static final int INPUT_START = 4;
    public static final int INPUT_COUNT = 12;
    public static final int SLOT_OUTPUT = 16;
    public static final int MAX_ENERGY = 100_000;

    private final ItemStackHandler inventory = new ItemStackHandler(17) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return stack.is(ModItems.BATTERY_PACK.get());
            }
            if (slot == SLOT_FILTER || slot == SLOT_UPGRADE_1 || slot == SLOT_UPGRADE_2) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            return slot != SLOT_FILTER && slot != SLOT_OUTPUT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private int energy;
    private int progress;
    private int selectedRecipe = -1;
    private final AssemblerArm[] arms = {new AssemblerArm(0x41B5L), new AssemblerArm(0x91E1L)};
    private double previousRing;
    private double ring;
    private double ringSpeed;
    private double ringTarget;
    private int ringDelay;
    private boolean processing;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> MAX_ENERGY;
                case 2 -> progress;
                case 3 -> selectedRecipe >= 0 ? AssemblyMachineRecipes.get(selectedRecipe).duration() : 0;
                case 4 -> selectedRecipe;
                case 5 -> processing ? 1 : 0;
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
            return 6;
        }
    };

    public AssemblyMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_MACHINE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public AssemblyMachineRecipes.Recipe getSelectedRecipe() {
        return selectedRecipe >= 0 ? AssemblyMachineRecipes.get(selectedRecipe) : null;
    }

    public ItemStack getDisplayedRecipeStack() {
        AssemblyMachineRecipes.Recipe recipe = getSelectedRecipe();
        return recipe == null ? ItemStack.EMPTY : recipe.resultStack();
    }

    public void selectRecipe(int index) {
        if (index < 0 || index >= AssemblyMachineRecipes.RECIPES.size()) {
            selectedRecipe = -1;
        } else {
            selectedRecipe = index;
        }
        progress = 0;
        setChangedAndSync();
    }

    public double getRing(float partialTick) {
        return previousRing + (ring - previousRing) * partialTick;
    }

    public double[] getArmPositions(int index, float partialTick) {
        return arms[index].getPositions(partialTick);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity machine) {
        machine.chargeFromBattery();
        machine.pullEnergyFromNetwork();
        AssemblyMachineRecipes.Recipe recipe = machine.getSelectedRecipe();
        boolean canCraft = recipe != null && machine.hasIngredients(recipe) && machine.canAcceptOutput(recipe.resultStack());
        boolean wasProcessing = machine.processing;
        int speedLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.SPEED);
        int powerLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.POWER);
        int overdriveLevel = machine.getUpgradeLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        double speed = 1.0D + Math.min(speedLevel, 3) * 0.25D + Math.min(overdriveLevel, 3) * 3.0D;
        double powerMultiplier = Math.max(0.25D, 1.0D - Math.min(powerLevel, 3) * 0.25D) * (1.0D + Math.min(speedLevel, 3) * 0.5D + Math.min(overdriveLevel, 3) * 3.0D);
        int consumption = recipe == null ? 0 : Math.max(1, (int) Math.ceil(recipe.energyPerTick() * powerMultiplier));
        if (canCraft && machine.energy >= consumption) {
            machine.energy -= consumption;
            machine.progress += Math.max(1, (int) Math.floor(speed));
            machine.processing = true;
            if (!wasProcessing || level.getGameTime() % 45L == 0L) {
                level.playSound(null, pos, ModSounds.ASSEMBLER_OPERATE.get(), SoundSource.BLOCKS, 0.5F, 0.75F);
            }
            if (machine.progress >= recipe.duration()) {
                machine.consumeIngredients(recipe);
                machine.insertOutput(recipe.resultStack());
                machine.progress = 0;
            }
        } else {
            machine.processing = false;
            machine.progress = 0;
            if (wasProcessing) {
                level.playSound(null, pos, ModSounds.ASSEMBLER_STOP.get(), SoundSource.BLOCKS, 0.25F, 1.5F);
            }
        }
        machine.updateAnimation(level.random);
        machine.setChangedAndSync();
    }


    private int getUpgradeLevel(ItemMachineUpgrade.UpgradeType type) {
        int level = 0;
        for (int slot : new int[]{SLOT_FILTER, SLOT_UPGRADE_1, SLOT_UPGRADE_2}) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == type) {
                level += upgrade.getLevel();
            }
        }
        return Math.min(level, 3);
    }

    private void updateAnimation(RandomSource random) {
        for (AssemblerArm arm : arms) {
            arm.updateInterpolation();
            if (processing) {
                if (arm.updateArm() && arm.justStruck()) {
                    if (level != null) {
                        level.playSound(null, worldPosition, ModSounds.ASSEMBLER_STRIKE.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
                    }
                }
            } else {
                arm.returnToNullPosition();
            }
        }
        previousRing = ring;
        if (!processing) {
            return;
        }
        if (ring != ringTarget) {
            double delta = Math.abs(ringTarget - ring);
            if (delta <= ringSpeed) {
                ring = ringTarget;
            } else if (ringTarget > ring) {
                ring += ringSpeed;
            } else {
                ring -= ringSpeed;
            }
            if (ring == ringTarget) {
                double correction = ringTarget >= 360.0D ? -360.0D : 360.0D;
                ringTarget += correction;
                ring += correction;
                previousRing += correction;
                ringDelay = 20 + random.nextInt(21);
            }
        } else if (ringDelay > 0) {
            ringDelay--;
        } else {
            ringTarget += (random.nextDouble() * 2.0D - 1.0D) * 135.0D;
            ringSpeed = 10.0D + random.nextDouble() * 5.0D;
            if (level != null) {
                level.playSound(null, worldPosition, ModSounds.ASSEMBLER_START.get(), SoundSource.BLOCKS, 0.25F, 1.25F + random.nextFloat() * 0.25F);
            }
        }
    }

    private void chargeFromBattery() {
        ItemStack stack = inventory.getStackInSlot(SLOT_BATTERY);
        if (!stack.is(ModItems.BATTERY_PACK.get()) || energy >= MAX_ENERGY) {
            return;
        }
        int extracted = ItemBatteryPack.extractEnergy(stack, Math.min(1000, MAX_ENERGY - energy));
        energy += extracted;
    }

    private void pullEnergyFromNetwork() {
        if (level == null || energy >= MAX_ENERGY) {
            return;
        }
        BatterySocketBlockEntity source = findPowerSource();
        if (source == null) {
            return;
        }
        energy += source.extractEnergyForMachine(Math.min(1000, MAX_ENERGY - energy));
    }

    private BatterySocketBlockEntity findPowerSource() {
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
            }
            if (controller == null || !controllers.add(controller)) {
                continue;
            }
            if (level.getBlockEntity(controller) instanceof BatterySocketBlockEntity socket && socket.canOutput() && socket.getEnergy() > 0) {
                return socket;
            }
        }
        return null;
    }

    private boolean hasIngredients(AssemblyMachineRecipes.Recipe recipe) {
        for (AssemblyMachineRecipes.Ingredient ingredient : recipe.ingredients()) {
            int remaining = ingredient.count();
            ItemStack expected = ingredient.stack().get();
            for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (ItemStack.isSameItemSameComponents(found, expected)) {
                    remaining -= found.getCount();
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private void consumeIngredients(AssemblyMachineRecipes.Recipe recipe) {
        for (AssemblyMachineRecipes.Ingredient ingredient : recipe.ingredients()) {
            int remaining = ingredient.count();
            ItemStack expected = ingredient.stack().get();
            for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT && remaining > 0; slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (!ItemStack.isSameItemSameComponents(found, expected)) {
                    continue;
                }
                int removed = Math.min(remaining, found.getCount());
                found.shrink(removed);
                inventory.setStackInSlot(slot, found.isEmpty() ? ItemStack.EMPTY : found);
                remaining -= removed;
            }
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
        tag.putInt("Progress", progress);
        tag.putInt("SelectedRecipe", selectedRecipe);
        tag.putBoolean("Processing", processing);
        tag.putDouble("PreviousRing", previousRing);
        tag.putDouble("Ring", ring);
        tag.putDouble("RingSpeed", ringSpeed);
        tag.putDouble("RingTarget", ringTarget);
        tag.putInt("RingDelay", ringDelay);
        tag.put("Arm0", arms[0].save());
        tag.put("Arm1", arms[1].save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy = tag.getInt("Energy");
        progress = tag.getInt("Progress");
        selectedRecipe = tag.contains("SelectedRecipe") ? tag.getInt("SelectedRecipe") : -1;
        processing = tag.getBoolean("Processing");
        previousRing = tag.getDouble("PreviousRing");
        ring = tag.getDouble("Ring");
        ringSpeed = tag.getDouble("RingSpeed");
        ringTarget = tag.getDouble("RingTarget");
        ringDelay = tag.getInt("RingDelay");
        if (tag.contains("Arm0")) {
            arms[0].load(tag.getCompound("Arm0"));
        }
        if (tag.contains("Arm1")) {
            arms[1].load(tag.getCompound("Arm1"));
        }
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

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        if (packet.getTag() != null) {
            loadAdditional(packet.getTag(), registries);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.assembly_machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AssemblyMachineMenu(containerId, inventory, this);
    }

    private static final class AssemblerArm {
        private static final double[][] POSITIONS = {{45.0D, -15.0D, -5.0D}, {15.0D, 15.0D, -15.0D}, {25.0D, 10.0D, -15.0D}, {30.0D, 0.0D, -10.0D}, {70.0D, -10.0D, -25.0D}};
        private final double[] angles = new double[4];
        private final double[] previousAngles = new double[4];
        private final double[] targetAngles = new double[4];
        private final double[] speed = new double[4];
        private final RandomSource random;
        private ArmActionState state = ArmActionState.ASSUME_POSITION;
        private int actionDelay;
        private boolean struck;

        private AssemblerArm(long seed) {
            random = RandomSource.create(seed);
            resetSpeed();
        }

        private void updateInterpolation() {
            System.arraycopy(angles, 0, previousAngles, 0, angles.length);
            struck = false;
        }

        private void returnToNullPosition() {
            for (int i = 0; i < 4; i++) {
                targetAngles[i] = 0.0D;
            }
            for (int i = 0; i < 3; i++) {
                speed[i] = 3.0D;
            }
            speed[3] = 0.25D;
            state = ArmActionState.RETRACT_STRIKER;
            move();
        }

        private void resetSpeed() {
            speed[0] = 15.0D;
            speed[1] = 15.0D;
            speed[2] = 15.0D;
            speed[3] = 0.5D;
        }

        private boolean updateArm() {
            resetSpeed();
            if (actionDelay > 0) {
                actionDelay--;
                return false;
            }
            switch (state) {
                case ASSUME_POSITION -> {
                    if (move()) {
                        actionDelay = 2;
                        state = ArmActionState.EXTEND_STRIKER;
                        targetAngles[3] = -0.75D;
                    }
                }
                case EXTEND_STRIKER -> {
                    if (move()) {
                        struck = true;
                        state = ArmActionState.RETRACT_STRIKER;
                        targetAngles[3] = 0.0D;
                    }
                }
                case RETRACT_STRIKER -> {
                    if (move()) {
                        actionDelay = 2 + random.nextInt(5);
                        chooseNewArmPosition();
                        state = ArmActionState.ASSUME_POSITION;
                    }
                }
            }
            return struck;
        }

        private boolean justStruck() {
            return struck;
        }

        private void chooseNewArmPosition() {
            double[] position = POSITIONS[random.nextInt(POSITIONS.length)];
            targetAngles[0] = position[0];
            targetAngles[1] = position[1];
            targetAngles[2] = position[2];
        }

        private boolean move() {
            boolean moved = false;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i]) {
                    continue;
                }
                moved = true;
                double delta = Math.abs(angles[i] - targetAngles[i]);
                if (delta <= speed[i]) {
                    angles[i] = targetAngles[i];
                } else if (angles[i] < targetAngles[i]) {
                    angles[i] += speed[i];
                } else {
                    angles[i] -= speed[i];
                }
            }
            return !moved;
        }

        private double[] getPositions(float partialTick) {
            double[] result = new double[4];
            for (int i = 0; i < result.length; i++) {
                result[i] = previousAngles[i] + (angles[i] - previousAngles[i]) * partialTick;
            }
            return result;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            for (int i = 0; i < 4; i++) {
                tag.putDouble("Angle" + i, angles[i]);
                tag.putDouble("PreviousAngle" + i, previousAngles[i]);
                tag.putDouble("TargetAngle" + i, targetAngles[i]);
            }
            tag.putInt("State", state.ordinal());
            tag.putInt("ActionDelay", actionDelay);
            return tag;
        }

        private void load(CompoundTag tag) {
            for (int i = 0; i < 4; i++) {
                angles[i] = tag.getDouble("Angle" + i);
                previousAngles[i] = tag.getDouble("PreviousAngle" + i);
                targetAngles[i] = tag.getDouble("TargetAngle" + i);
            }
            int stateIndex = tag.getInt("State");
            state = ArmActionState.values()[Math.max(0, Math.min(stateIndex, ArmActionState.values().length - 1))];
            actionDelay = tag.getInt("ActionDelay");
        }
    }

    private enum ArmActionState {
        ASSUME_POSITION,
        EXTEND_STRIKER,
        RETRACT_STRIKER
    }

}
