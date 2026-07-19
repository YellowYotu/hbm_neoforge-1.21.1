package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.BatterySocketDummyBlock;
import com.yellowyotu.hbmneoforge.item.ItemBatteryPack;
import com.yellowyotu.hbmneoforge.item.ItemShredderBlade;
import com.yellowyotu.hbmneoforge.menu.ShredderMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ShredderBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_START = 0;
    public static final int INPUT_COUNT = 9;
    public static final int OUTPUT_START = 9;
    public static final int OUTPUT_COUNT = 18;
    public static final int SLOT_BLADE_LEFT = 27;
    public static final int SLOT_BLADE_RIGHT = 28;
    public static final int SLOT_BATTERY = 29;
    public static final int INVENTORY_SIZE = 30;
    public static final int MAX_ENERGY = 10_000;
    public static final int PROCESSING_TIME = 60;
    public static final int ENERGY_PER_TICK = 5;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= INPUT_START && slot < INPUT_START + INPUT_COUNT) {
                return ShredderRecipes.canShred(stack);
            }
            if (slot >= OUTPUT_START && slot < OUTPUT_START + OUTPUT_COUNT) {
                return false;
            }
            if (slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT) {
                return stack.getItem() instanceof ItemShredderBlade;
            }
            if (slot == SLOT_BATTERY) {
                return stack.is(ModItems.BATTERY_PACK.get());
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT || slot == SLOT_BATTERY ? 1 : super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
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
            if (!canAutomateInsert(slot, stack)) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= OUTPUT_START && slot < OUTPUT_START + OUTPUT_COUNT) {
                return inventory.extractItem(slot, amount, simulate);
            }
            if ((slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT) && isBladeBroken(inventory.getStackInSlot(slot))) {
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
            return canAutomateInsert(slot, stack);
        }
    };

    private int energy;
    private int progress;
    private int soundCycle;
    private boolean processing;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> MAX_ENERGY;
                case 2 -> progress;
                case 3 -> PROCESSING_TIME;
                case 4 -> processing ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energy = value;
                case 2 -> progress = value;
                case 4 -> processing = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public ShredderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHREDDER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShredderBlockEntity shredder) {
        shredder.chargeFromBattery();
        shredder.pullEnergyFromNetwork();

        boolean canProcess = shredder.canProcess();
        boolean wasProcessing = shredder.processing;
        shredder.processing = canProcess && shredder.energy >= ENERGY_PER_TICK;

        if (shredder.processing) {
            shredder.energy -= ENERGY_PER_TICK;
            shredder.progress++;
            if (!wasProcessing || shredder.soundCycle <= 0) {
                level.playSound(null, pos, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, 1.0F, 0.75F);
                shredder.soundCycle = 50;
            }
            shredder.soundCycle--;

            if (shredder.progress >= PROCESSING_TIME) {
                shredder.progress = 0;
                shredder.damageBlade(SLOT_BLADE_LEFT);
                shredder.damageBlade(SLOT_BLADE_RIGHT);
                shredder.processItems();
            }
        } else {
            shredder.progress = 0;
            shredder.soundCycle = 0;
        }

        shredder.setChangedAndSync();
    }

    private boolean canAutomateInsert(int slot, ItemStack stack) {
        if (!inventory.isItemValid(slot, stack)) {
            return false;
        }
        if (slot < INPUT_START || slot >= INPUT_START + INPUT_COUNT) {
            return true;
        }

        ItemStack target = inventory.getStackInSlot(slot);
        if (target.isEmpty()) {
            return true;
        }
        int targetCount = target.getCount();
        for (int inputSlot = INPUT_START; inputSlot < INPUT_START + INPUT_COUNT; inputSlot++) {
            ItemStack other = inventory.getStackInSlot(inputSlot);
            if (other.isEmpty()) {
                return false;
            }
            if (ItemStack.isSameItemSameComponents(other, stack) && other.getCount() < targetCount) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBladeBroken(ItemStack stack) {
        return stack.getItem() instanceof ItemShredderBlade && stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }

    private boolean canProcess() {
        if (!isBladeUsable(inventory.getStackInSlot(SLOT_BLADE_LEFT)) || !isBladeUsable(inventory.getStackInSlot(SLOT_BLADE_RIGHT))) {
            return false;
        }

        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (!input.isEmpty() && ShredderRecipes.canShred(input) && hasSpace(ShredderRecipes.getResult(input))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBladeUsable(ItemStack stack) {
        return stack.getItem() instanceof ItemShredderBlade && (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage());
    }

    private void processItems() {
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty() || !ShredderRecipes.canShred(input)) {
                continue;
            }

            ItemStack result = ShredderRecipes.getResult(input);
            if (result.isEmpty() || !hasSpace(result)) {
                continue;
            }

            insertOutput(result);
            input.shrink(1);
            inventory.setStackInSlot(slot, input.isEmpty() ? ItemStack.EMPTY : input);
        }
    }

    private boolean hasSpace(ItemStack result) {
        int remaining = result.getCount();
        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT; slot++) {
            ItemStack output = inventory.getStackInSlot(slot);
            if (output.isEmpty()) {
                remaining -= result.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(output, result)) {
                remaining -= output.getMaxStackSize() - output.getCount();
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private void insertOutput(ItemStack result) {
        int remaining = result.getCount();

        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT && remaining > 0; slot++) {
            ItemStack output = inventory.getStackInSlot(slot);
            if (!output.isEmpty() && ItemStack.isSameItemSameComponents(output, result)) {
                int amount = Math.min(remaining, output.getMaxStackSize() - output.getCount());
                if (amount > 0) {
                    output.grow(amount);
                    inventory.setStackInSlot(slot, output);
                    remaining -= amount;
                }
            }
        }

        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT && remaining > 0; slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                ItemStack inserted = result.copy();
                int amount = Math.min(remaining, inserted.getMaxStackSize());
                inserted.setCount(amount);
                inventory.setStackInSlot(slot, inserted);
                remaining -= amount;
            }
        }
    }

    private void damageBlade(int slot) {
        ItemStack blade = inventory.getStackInSlot(slot);
        if (!blade.isDamageableItem()) {
            return;
        }
        blade.setDamageValue(Math.min(blade.getMaxDamage(), blade.getDamageValue() + 1));
        inventory.setStackInSlot(slot, blade);
    }

    private void chargeFromBattery() {
        ItemStack stack = inventory.getStackInSlot(SLOT_BATTERY);
        if (!stack.is(ModItems.BATTERY_PACK.get()) || energy >= MAX_ENERGY) {
            return;
        }
        energy += ItemBatteryPack.extractEnergy(stack, Math.min(1000, MAX_ENERGY - energy));
    }

    private void pullEnergyFromNetwork() {
        if (level == null || energy >= MAX_ENERGY) {
            return;
        }
        BatterySocketBlockEntity source = findPowerSource();
        if (source != null) {
            energy += source.extractEnergyForMachine(Math.min(1000, MAX_ENERGY - energy));
        }
    }

    private BatterySocketBlockEntity findPowerSource() {
        if (level == null) {
            return null;
        }

        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (Direction direction : Direction.values()) {
            queue.add(worldPosition.relative(direction));
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

    public int getBladeState(int slot) {
        ItemStack blade = inventory.getStackInSlot(slot);
        if (!(blade.getItem() instanceof ItemShredderBlade)) {
            return 0;
        }
        if (!blade.isDamageableItem()) {
            return 1;
        }
        if (blade.getDamageValue() >= blade.getMaxDamage()) {
            return 3;
        }
        return blade.getDamageValue() < blade.getMaxDamage() / 2 ? 1 : 2;
    }

    public IItemHandler getAutomationInventory() {
        return automationInventory;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
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
            BlockState state = getBlockState();
            serverLevel.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_neoforge.shredder");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ShredderMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy);
        tag.putInt("progress", progress);
        tag.putInt("sound_cycle", soundCycle);
        tag.putBoolean("processing", processing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        energy = Math.max(0, Math.min(MAX_ENERGY, tag.getInt("energy")));
        progress = Math.max(0, Math.min(PROCESSING_TIME, tag.getInt("progress")));
        soundCycle = tag.getInt("sound_cycle");
        processing = tag.getBoolean("processing");
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
}
