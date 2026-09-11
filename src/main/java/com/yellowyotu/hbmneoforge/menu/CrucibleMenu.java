package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.CrucibleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class CrucibleMenu extends AbstractContainerMenu {
    private final CrucibleBlockEntity crucible;
    private final ContainerData data;

    public CrucibleMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, getCrucible(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public CrucibleMenu(int id, Inventory inventory, CrucibleBlockEntity crucible, ContainerData data) {
        super(ModMenus.CRUCIBLE.get(), id);
        this.crucible = crucible;
        this.data = data;
        addDataSlots(data);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(crucible.getItems(), 1 + col + row * 3, 107 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 132 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 190));
        }
    }

    private static CrucibleBlockEntity getCrucible(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            return crucible;
        }
        return new CrucibleBlockEntity(pos, ModBlocks.CRUCIBLE.get().defaultBlockState());
    }

    public int getHeat() {
        return data.get(0);
    }

    public int getProgress() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 9, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }


    public java.util.Map<String, Integer> getRecipeStack() {
        return crucible.getRecipeStack();
    }

    public java.util.Map<String, Integer> getWasteStack() {
        return crucible.getWasteStack();
    }

    public String getRecipeName() {
        return crucible.getRecipeName();
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 99) {
            if (!player.level().isClientSide()) {
                crucible.selectRecipe(-1);
            }
            return true;
        }
        if (id >= 100 && id < 100 + com.yellowyotu.hbmneoforge.foundry.CrucibleRecipeRegistry.recipes().size()) {
            if (!player.level().isClientSide()) {
                crucible.selectRecipe(id - 100);
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean stillValid(Player player) {
        return crucible.getLevel() != null
                && crucible.getLevel().getBlockState(crucible.getBlockPos()).is(ModBlocks.CRUCIBLE.get())
                && player.distanceToSqr(crucible.getBlockPos().getX() + 0.5D, crucible.getBlockPos().getY() + 0.5D, crucible.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }
}
