package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.menu.MachinePressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MachinePressBlockEntity
        extends BlockEntity
        implements MenuProvider {

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;

    public static final int STORAGE_START = 4;
    public static final int INVENTORY_SIZE = 13;

    public static final int MAX_SPEED = 400;
    public static final int MAX_PRESS = 200;
    public static final int FUEL_PER_OPERATION = 200;

    private static final int PROGRESS_AT_MAX_SPEED = 8;
    private static final int DIRECTION_CHANGE_DELAY = 5;

    private int speed;
    private int burnTime;
    private int pressProgress;

    private boolean retracting;
    private int delay;

    /*
     * Клиентское предыдущее значение нужно для плавной анимации.
     */
    private int previousClientPressProgress;

    private final ItemStackHandler inventory =
            new ItemStackHandler(INVENTORY_SIZE) {

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return switch (slot) {
                        case SLOT_FUEL ->
                                stack.is(Items.COAL)
                                        || stack.is(Items.CHARCOAL);

                        case SLOT_STAMP -> stack.getItem() instanceof com.yellowyotu.hbmneoforge.item.ItemStamp;

                        case SLOT_OUTPUT -> false;

                        default -> true;
                    };
                }

                @Override
                protected void onContentsChanged(int slot) {
                    setChanged();
                }
            };

    /*
     * 0 — скорость;
     * 1 — запас топлива;
     * 2 — положение пресса;
     * 3 — возвращается ли головка.
     */
    private final ContainerData data = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> speed;
                case 1 -> burnTime;
                case 2 -> pressProgress;
                case 3 -> retracting ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> speed = value;
                case 1 -> burnTime = value;
                case 2 -> pressProgress = value;
                case 3 -> retracting = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MachinePressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_PRESS.get(), pos, state);
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            MachinePressBlockEntity press
    ) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        if (press.tryLoadFuel()) {
            changed = true;
        }

        boolean canProcess = press.canProcess();

        if ((canProcess || press.retracting)
                && press.burnTime >= FUEL_PER_OPERATION) {

            if (press.speed < MAX_SPEED) {
                press.speed++;
                changed = true;
            }
        } else if (press.speed > 0) {
            press.speed--;
            changed = true;
        }

        if (press.delay > 0) {
            press.delay--;
            changed = true;
        } else {
            int movement =
                    press.speed * PROGRESS_AT_MAX_SPEED / MAX_SPEED;

            if (press.retracting) {
                press.pressProgress -= movement;

                if (press.pressProgress <= 0) {
                    press.pressProgress = 0;
                    press.retracting = false;
                    press.delay = DIRECTION_CHANGE_DELAY;
                }

                changed = movement > 0;
            } else if (canProcess) {
                press.pressProgress += movement;

                if (press.pressProgress >= MAX_PRESS) {
                    press.pressProgress = MAX_PRESS;
                    press.completeOperation();
                    press.retracting = true;
                    press.delay = DIRECTION_CHANGE_DELAY;
                }

                changed = movement > 0;
            } else if (press.pressProgress > 0) {
                press.retracting = true;
                changed = true;
            }
        }

        if (changed) {
            press.setChanged();

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(
                        pos,
                        state,
                        state,
                        3
                );
            }
        }
    }

    private boolean tryLoadFuel() {
        if (burnTime >= FUEL_PER_OPERATION) {
            return false;
        }

        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);

        if (fuel.isEmpty()) {
            return false;
        }

        if (!fuel.is(Items.COAL) && !fuel.is(Items.CHARCOAL)) {
            return false;
        }

        burnTime += 1600;
        fuel.shrink(1);

        inventory.setStackInSlot(
                SLOT_FUEL,
                fuel.isEmpty() ? ItemStack.EMPTY : fuel
        );

        return true;
    }

    private boolean canProcess() {
        if (burnTime < FUEL_PER_OPERATION) {
            return false;
        }

        ItemStack stamp = inventory.getStackInSlot(SLOT_STAMP);
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);

        if (!(stamp.getItem() instanceof com.yellowyotu.hbmneoforge.item.ItemStamp)) {
            return false;
        }

        if (input.isEmpty()) {
            return false;
        }

        ItemStack result = getRecipeResult(input, stamp);

        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);

        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }

        return output.getCount() + result.getCount()
                <= output.getMaxStackSize();
    }

    private void completeOperation() {
        ItemStack input =
                inventory.getStackInSlot(SLOT_INPUT);

        ItemStack stamp =
                inventory.getStackInSlot(SLOT_STAMP);

        ItemStack result =
                getRecipeResult(input, stamp);

        if (result.isEmpty()) {
            return;
        }

        ItemStack output =
                inventory.getStackInSlot(SLOT_OUTPUT);

        if (output.isEmpty()) {
            inventory.setStackInSlot(
                    SLOT_OUTPUT,
                    result.copy()
            );
        } else {
            output.grow(result.getCount());

            inventory.setStackInSlot(
                    SLOT_OUTPUT,
                    output
            );
        }

        input.shrink(1);

        inventory.setStackInSlot(
                SLOT_INPUT,
                input.isEmpty()
                        ? ItemStack.EMPTY
                        : input
        );

        damageStamp();

        burnTime -= FUEL_PER_OPERATION;

        if (burnTime < 0) {
            burnTime = 0;
        }
    }

    private void damageStamp() {
        ItemStack stamp =
                inventory.getStackInSlot(SLOT_STAMP);

        if (stamp.isEmpty() || !stamp.isDamageableItem()) {
            return;
        }

        int newDamage =
                stamp.getDamageValue() + 1;

        if (newDamage >= stamp.getMaxDamage()) {
            inventory.setStackInSlot(
                    SLOT_STAMP,
                    ItemStack.EMPTY
            );
        } else {
            stamp.setDamageValue(newDamage);

            inventory.setStackInSlot(
                    SLOT_STAMP,
                    stamp
            );
        }
    }

    public static ItemStack getRecipeResult(ItemStack input, ItemStack stamp) {
        return MachinePressRecipes.getResult(input, stamp);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public int getPressProgress() {
        return pressProgress;
    }

    public float getInterpolatedPressProgress(float partialTick) {
        return previousClientPressProgress
                + (pressProgress - previousClientPressProgress)
                * partialTick;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(
                "container.hbm_neoforge.machine_press"
        );
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new MachinePressMenu(
                containerId,
                playerInventory,
                inventory,
                data,
                worldPosition
        );
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put(
                "inventory",
                inventory.serializeNBT(registries)
        );

        tag.putInt("speed", speed);
        tag.putInt("burn_time", burnTime);
        tag.putInt("press_progress", pressProgress);
        tag.putBoolean("retracting", retracting);
        tag.putInt("delay", delay);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (level != null && level.isClientSide()) {
            previousClientPressProgress = pressProgress;
        }

        if (tag.contains("inventory")) {
            inventory.deserializeNBT(
                    registries,
                    tag.getCompound("inventory")
            );
        }

        speed = tag.getInt("speed");
        burnTime = tag.getInt("burn_time");
        pressProgress = tag.getInt("press_progress");
        retracting = tag.getBoolean("retracting");
        delay = tag.getInt("delay");
    }

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}