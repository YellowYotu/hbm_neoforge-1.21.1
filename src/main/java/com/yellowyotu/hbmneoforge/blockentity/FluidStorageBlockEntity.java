package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.FluidStorageBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifier;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import com.yellowyotu.hbmneoforge.item.ItemInfiniteFluid;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
import com.yellowyotu.hbmneoforge.menu.FluidStorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FluidStorageBlockEntity extends BlockEntity implements FluidNode, MenuProvider {
    public enum Mode { INPUT, BUFFER, OUTPUT, DISABLED }

    private NTMFluidType type;
    private int amount;
    private Mode mode = Mode.BUFFER;

    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return stack.getItem() instanceof ItemFluidIdentifierMulti || stack.getItem() instanceof ItemFluidIdentifier;
            }
            if (slot == 2) {
                return stack.getItem() instanceof ItemSolderingFluidCell || stack.getItem() instanceof ItemInfiniteFluid || stack.is(Items.WATER_BUCKET);
            }
            if (slot == 4) {
                return stack.is(ModItems.CELL_EMPTY.get()) || stack.is(Items.BUCKET);
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> amount;
                case 1 -> getFluidCapacity();
                case 2 -> type == null ? -1 : type.ordinal();
                case 3 -> mode.ordinal();
                case 4 -> getTransferRate();
                case 5 -> getBlockState().getBlock() instanceof FluidStorageBlock block ? block.getKind().ordinal() : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 2) {
                setFluidType(NTMFluidType.byOrdinalSafe(value));
            } else if (index == 3) {
                setMode(value);
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public FluidStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_STORAGE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getData() {
        return data;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean canReceive() {
        return mode == Mode.INPUT || mode == Mode.BUFFER;
    }

    public boolean canProvide() {
        return mode == Mode.OUTPUT || mode == Mode.BUFFER;
    }

    public void setMode(int ordinal) {
        mode = Mode.values()[Math.floorMod(ordinal, Mode.values().length)];
        setChangedAndSync();
    }

    public void cycleMode() {
        setMode(mode.ordinal() + 1);
    }

    public void setFluidType(NTMFluidType newType) {
        if (newType == type) {
            return;
        }
        if (amount > 0 && type != null && newType != type) {
            return;
        }
        type = newType;
        updateTankFluidModel();
        setChangedAndSync();
    }

    public int getTransferRate() {
        if (getBlockState().getBlock() instanceof FluidStorageBlock block) {
            return switch (block.getKind()) {
                case PLASTIC_BARREL, CORRODED_BARREL, STEEL_BARREL -> 1_000;
                case TANK -> 2_560;
            };
        }
        return 1_000;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidStorageBlockEntity storage) {
        storage.handleIdentifierSlot();
        storage.handleContainerSlots();

        if (!storage.canProvide() || storage.amount <= 0 || storage.type == null) {
            return;
        }

        int requested = Math.min(storage.getTransferRate(), storage.amount);
        int moved = 0;
        if (state.getBlock() instanceof com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock tank) {
            int remaining = requested;
            for (BlockPos portPos : tank.getExternalPortPositions(pos, state)) {
                if (remaining <= 0) {
                    break;
                }
                if (level.getBlockEntity(portPos) instanceof FluidPipeBlockEntity pipe && pipe.canCarry(storage.type)) {
                    int portMoved = FluidNetworkUtil.fillNetwork(level, portPos, storage.type, remaining, pos);
                    moved += portMoved;
                    remaining -= portMoved;
                }
            }
        } else {
            moved = FluidNetworkUtil.fillNetwork(level, pos, storage.type, requested, pos);
        }
        if (moved > 0) {
            storage.amount -= moved;
            storage.setChangedAndSync();
        }
    }

    private void handleIdentifierSlot() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        NTMFluidType selected = null;
        if (stack.getItem() instanceof ItemFluidIdentifierMulti) {
            selected = ItemFluidIdentifierMulti.getType(stack, true);
        } else if (stack.getItem() instanceof ItemFluidIdentifier identifier) {
            selected = identifier.getFluidType();
        }

        if (selected != null) {
            setFluidType(selected);
            if (inventory.getStackInSlot(1).isEmpty()) {
                inventory.setStackInSlot(1, stack.copy());
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
    }

    private void handleContainerSlots() {
        if (level == null || level.isClientSide()) {
            return;
        }

        ItemStack input = inventory.getStackInSlot(2);
        if (!input.isEmpty()) {
            if (input.getItem() instanceof ItemInfiniteFluid infinite) {
                NTMFluidType effective = infinite.getFluidType() != null ? infinite.getFluidType() : type;
                if (effective != null) {
                    fillInternal(effective, infinite.getRate());
                }
            } else if (inventory.getStackInSlot(3).isEmpty() && input.is(Items.WATER_BUCKET) && fillInternal(NTMFluidType.WATER, 1_000) == 1_000) {
                inventory.setStackInSlot(2, ItemStack.EMPTY);
                inventory.setStackInSlot(3, new ItemStack(Items.BUCKET));
            } else if (inventory.getStackInSlot(3).isEmpty() && input.getItem() instanceof ItemSolderingFluidCell cell) {
                NTMFluidType fluid = NTMFluidType.valueOf(cell.getFluidType().name());
                if (fillInternal(fluid, 1_000) == 1_000) {
                    inventory.setStackInSlot(2, ItemStack.EMPTY);
                    inventory.setStackInSlot(3, new ItemStack(ModItems.CELL_EMPTY.get()));
                }
            }
        }

        ItemStack empty = inventory.getStackInSlot(4);
        if (type != null && amount >= 1_000 && !empty.isEmpty() && inventory.getStackInSlot(5).isEmpty()) {
            ItemStack filled = makeFilledContainer(empty, type);
            if (!filled.isEmpty() && drainInternal(type, 1_000) == 1_000) {
                inventory.setStackInSlot(4, ItemStack.EMPTY);
                inventory.setStackInSlot(5, filled);
            }
        }
    }

    private static ItemStack makeFilledContainer(ItemStack empty, NTMFluidType type) {
        if (empty.is(Items.BUCKET) && type == NTMFluidType.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        if (!empty.is(ModItems.CELL_EMPTY.get())) {
            return ItemStack.EMPTY;
        }
        return switch (type) {
            case SULFURIC_ACID -> new ItemStack(ModItems.CELL_SULFURIC_ACID.get());
            case PEROXIDE -> new ItemStack(ModItems.CELL_PEROXIDE.get());
            case SOLVENT -> new ItemStack(ModItems.CELL_SOLVENT.get());
            case HELIUM4 -> new ItemStack(ModItems.CELL_HELIUM4.get());
            case PERFLUOROMETHYL -> new ItemStack(ModItems.CELL_PERFLUOROMETHYL.get());
            case PERFLUOROMETHYL_COLD -> new ItemStack(ModItems.CELL_PERFLUOROMETHYL_COLD.get());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public NTMFluidType getFluidType() {
        return type;
    }

    @Override
    public int getFluidAmount() {
        return amount;
    }

    @Override
    public int getFluidCapacity() {
        return getBlockState().getBlock() instanceof FluidStorageBlock block ? block.getCapacity() : 0;
    }

    @Override
    public boolean accepts(NTMFluidType fluid) {
        return canReceive() && fluid != null && type == fluid;
    }

    @Override
    public int fill(NTMFluidType fluid, int requested) {
        if (!canReceive()) {
            return 0;
        }
        return fillInternal(fluid, requested);
    }

    private int fillInternal(NTMFluidType fluid, int requested) {
        if (requested <= 0 || fluid == null || (type != null && type != fluid)) {
            return 0;
        }
        int accepted = Math.min(requested, getFluidCapacity() - amount);
        if (accepted <= 0) {
            return 0;
        }
        type = fluid;
        updateTankFluidModel();
        amount += accepted;
        setChangedAndSync();
        return accepted;
    }

    @Override
    public int drain(NTMFluidType fluid, int requested) {
        if (!canProvide()) {
            return 0;
        }
        return drainInternal(fluid, requested);
    }

    private int drainInternal(NTMFluidType fluid, int requested) {
        if (requested <= 0 || type != fluid) {
            return 0;
        }
        int drained = Math.min(requested, amount);
        amount -= drained;
        if (drained > 0) {
            setChangedAndSync();
        }
        return drained;
    }

    private void updateTankFluidModel() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock) || !state.hasProperty(com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock.FLUID_MODEL)) {
            return;
        }
        int model = type == null ? NTMFluidType.values().length : type.ordinal();
        if (state.getValue(com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock.FLUID_MODEL) != model) {
            level.setBlock(worldPosition, state.setValue(com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock.FLUID_MODEL, model), 3);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        if (getBlockState().getBlock() instanceof FluidStorageBlock block) {
            return getBlockState().getBlock().getName();
        }
        return Component.translatable("container.hbm_neoforge.fluid_storage");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FluidStorageMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (type != null) {
            tag.putString("fluid", type.id());
        }
        tag.putInt("amount", amount);
        tag.putInt("mode", mode.ordinal());
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        type = tag.contains("fluid") ? NTMFluidType.byId(tag.getString("fluid")) : null;
        amount = Math.max(0, Math.min(getFluidCapacity(), tag.getInt("amount")));
        mode = Mode.values()[Math.floorMod(tag.getInt("mode"), Mode.values().length)];
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
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
}
