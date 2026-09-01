package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.block.FluidPipeBlock;
import com.yellowyotu.hbmneoforge.fluid.FluidNetworkUtil;
import com.yellowyotu.hbmneoforge.fluid.FluidNode;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class FluidPipeBlockEntity extends BlockEntity implements FluidNode, net.minecraft.world.MenuProvider {
    public static final ModelProperty<BlockState> DISGUISE_STATE = new ModelProperty<>();
    private NTMFluidType filter;
    private boolean enabled = true;
    private BlockState disguiseState;

    private final net.minecraft.world.inventory.ContainerData data = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> 0;
                case 1, 2 -> filter == null ? -1 : filter.ordinal();
                case 3 -> enabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public net.minecraft.world.inventory.ContainerData getData() {
        return data;
    }

    public NTMFluidType getFilter() {
        return filter;
    }

    public void setFilter(NTMFluidType filter) {
        if (this.filter == filter) {
            return;
        }
        this.filter = filter;
        updateFilteredState();
        setChangedAndSync();
        FluidPipeBlock.refreshConnections(level, worldPosition);
    }

    public void clearFilter() {
        setFilter(null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
        if (level != null && getBlockState().hasProperty(FluidPipeBlock.ACTIVE)) {
            level.setBlock(worldPosition, getBlockState().setValue(FluidPipeBlock.ACTIVE, enabled), 3);
        }
        setChangedAndSync();
        FluidPipeBlock.refreshConnections(level, worldPosition);
    }

    public int getTransferRate() {
        return getBlockState().getBlock() instanceof FluidPipeBlock block ? block.getTransferRate() : 1_000;
    }

    public boolean canCarry(NTMFluidType type) {
        return enabled && type != null && filter == type;
    }

    public boolean canConnectTo(FluidPipeBlockEntity other) {
        return other != null && enabled && other.enabled && filter == other.filter;
    }

    private void updateFilteredState() {
        if (level != null && getBlockState().hasProperty(FluidPipeBlock.FILTERED)) {
            level.setBlock(worldPosition, getBlockState().setValue(FluidPipeBlock.FILTERED, filter != null), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPipeBlockEntity pipe) {
        // NTM FluidNet is network-driven; ducts do not behave like one-block tanks.
    }

    public BlockState getDisguiseState() {
        return disguiseState;
    }

    public void setDisguiseState(BlockState disguiseState) {
        this.disguiseState = disguiseState;
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
        }
        setChangedAndSync();
    }

    @Override
    public ModelData getModelData() {
        if (disguiseState == null) {
            return ModelData.EMPTY;
        }
        return ModelData.builder().with(DISGUISE_STATE, disguiseState).build();
    }

    @Override
    public NTMFluidType getFluidType() {
        return filter;
    }

    @Override
    public int getFluidAmount() {
        return 0;
    }

    @Override
    public int getFluidCapacity() {
        return 0;
    }

    @Override
    public boolean accepts(NTMFluidType fluid) {
        return canCarry(fluid);
    }

    @Override
    public int fill(NTMFluidType fluid, int requested) {
        if (!canCarry(fluid) || level == null) {
            return 0;
        }
        return FluidNetworkUtil.fillNetwork(level, worldPosition, fluid, Math.min(requested, getTransferRate()), null);
    }

    @Override
    public int drain(NTMFluidType fluid, int requested) {
        if (!canCarry(fluid) || level == null) {
            return 0;
        }
        return FluidNetworkUtil.drainNetwork(level, worldPosition, fluid, Math.min(requested, getTransferRate()));
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.hbm_neoforge.fluid_pipe");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
        return new com.yellowyotu.hbmneoforge.menu.FluidPipeMenu(id, inventory, this);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (filter != null) {
            tag.putString("filter", filter.id());
        }
        if (disguiseState != null) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(disguiseState.getBlock());
            tag.putString("disguise", key.toString());
        }
        tag.putBoolean("enabled", enabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        filter = tag.contains("filter") ? NTMFluidType.byId(tag.getString("filter")) : null;
        disguiseState = null;
        if (tag.contains("disguise")) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString("disguise"));
            if (key != null && BuiltInRegistries.BLOCK.containsKey(key)) {
                disguiseState = BuiltInRegistries.BLOCK.get(key).defaultBlockState();
            }
        }
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
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
