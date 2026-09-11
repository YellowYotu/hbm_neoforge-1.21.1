package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FluidPipeMenu extends AbstractContainerMenu {
    private final FluidPipeBlockEntity pipe;
    private final ContainerData data;
    public FluidPipeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { this(id, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof FluidPipeBlockEntity pipe ? pipe : null); }
    public FluidPipeMenu(int id, Inventory inventory, FluidPipeBlockEntity pipe) {
        super(ModMenus.FLUID_PIPE.get(), id);
        this.pipe = pipe;
        this.data = pipe == null ? new SimpleContainerData(4) : pipe.getData();
        addDataSlots(data);
    }
    public int amount() { return data.get(0); }
    public int fluidOrdinal() { return data.get(1); }
    public int filterOrdinal() { return data.get(2); }
    public boolean enabled() { return data.get(3) != 0; }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (pipe == null) { return false; }
        if (id == 0) { pipe.toggleEnabled(); return true; }
        int current = pipe.getFilter() == null ? -1 : pipe.getFilter().ordinal();
        if (id == 1) { pipe.setFilter(NTMFluidType.byOrdinalSafe(current + 1)); return true; }
        if (id == 2) { if (current <= 0) { pipe.clearFilter(); } else { pipe.setFilter(NTMFluidType.byOrdinalSafe(current - 1)); } return true; }
        if (id == 3) { pipe.clearFilter(); return true; }
        if (id >= 100 && id < 100 + NTMFluidType.values().length) { pipe.setFilter(NTMFluidType.values()[id - 100]); return true; }
        return false;
    }
    @Override public boolean stillValid(Player player) { return pipe != null && !pipe.isRemoved() && player.distanceToSqr(pipe.getBlockPos().getX()+.5, pipe.getBlockPos().getY()+.5, pipe.getBlockPos().getZ()+.5) <= 64; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
