package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.item.ItemFluidIdentifierMulti;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class FluidIdentifierMenu extends AbstractContainerMenu {
    private final Player player;
    private final InteractionHand hand;
    private final ContainerData data;

    public FluidIdentifierMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, InteractionHand.values()[Math.floorMod(buffer.readByte(), InteractionHand.values().length)]);
    }

    public FluidIdentifierMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ModMenus.FLUID_IDENTIFIER.get(), id);
        this.player = inventory.player;
        this.hand = hand;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                ItemStack stack = getIdentifierStack();
                NTMFluidType primary = ItemFluidIdentifierMulti.getType(stack, true);
                NTMFluidType secondary = ItemFluidIdentifierMulti.getType(stack, false);
                return switch (index) {
                    case 0 -> primary == null ? -1 : primary.ordinal();
                    case 1 -> secondary == null ? -1 : secondary.ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        addDataSlots(data);
    }

    private ItemStack getIdentifierStack() {
        ItemStack stack = player.getItemInHand(hand);
        return stack.is(ModItems.FLUID_IDENTIFIER.get()) ? stack : ItemStack.EMPTY;
    }

    public int primaryOrdinal() {
        return data.get(0);
    }

    public int secondaryOrdinal() {
        return data.get(1);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack stack = getIdentifierStack();
        if (stack.isEmpty()) {
            return false;
        }
        if (id == 0) {
            ItemFluidIdentifierMulti.swapTypes(stack);
            return true;
        }
        if (id >= 1000 && id < 1000 + NTMFluidType.values().length) {
            ItemFluidIdentifierMulti.setType(stack, NTMFluidType.values()[id - 1000], true);
            return true;
        }
        if (id >= 2000 && id < 2000 + NTMFluidType.values().length) {
            ItemFluidIdentifierMulti.setType(stack, NTMFluidType.values()[id - 2000], false);
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player && !getIdentifierStack().isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
