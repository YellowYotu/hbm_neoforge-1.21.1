package com.yellowyotu.hbmneoforge.menu;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.HBMAnvilRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class HBMAnvilMenu extends AbstractContainerMenu {
    private static final int ANVIL_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_START = 3;
    private static final int PLAYER_INVENTORY_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final ContainerLevelAccess access;
    private final HBMAnvilBlockEntity anvil;
    private int selectedRecipe;

    public HBMAnvilMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, getClientAnvil(playerInventory, buffer.readBlockPos()));
    }

    public HBMAnvilMenu(int containerId, Inventory playerInventory, HBMAnvilBlockEntity anvil) {
        super(ModMenus.HBM_ANVIL.get(), containerId);
        this.anvil = anvil;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), anvil.getBlockPos());
        this.selectedRecipe = anvil.getSelectedRecipe();

        ItemStackHandler inventory = anvil.getInventory();
        addSlot(new SlotItemHandler(inventory, HBMAnvilBlockEntity.SLOT_PRIMARY, 17, 27));
        addSlot(new SlotItemHandler(inventory, HBMAnvilBlockEntity.SLOT_SECONDARY, 53, 27));
        addSlot(new AnvilOutputSlot(inventory, HBMAnvilBlockEntity.SLOT_OUTPUT, 89, 27));
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return HBMAnvilMenu.this.anvil.getSelectedRecipe();
            }

            @Override
            public void set(int value) {
                HBMAnvilMenu.this.selectedRecipe = Math.floorMod(value, HBMAnvilRecipes.size());
            }
        });
    }

    private static HBMAnvilBlockEntity getClientAnvil(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof HBMAnvilBlockEntity anvil) {
            return anvil;
        }
        return new HBMAnvilBlockEntity(pos, ModBlocks.ANVIL_IRON.get().defaultBlockState());
    }

    public int getSelectedRecipe() {
        return selectedRecipe;
    }

    public HBMAnvilRecipes.Recipe getRecipe() {
        return HBMAnvilRecipes.get(selectedRecipe);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == HBMAnvilRecipes.size()) {
            ItemStack result = HBMAnvilRecipes.craftFromPlayerInventory(player.getInventory(), selectedRecipe);
            if (result.isEmpty()) {
                return false;
            }
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
            broadcastChanges();
            return true;
        }
        if (id < 0 || id >= HBMAnvilRecipes.size()) {
            return false;
        }
        anvil.setSelectedRecipe(id);
        selectedRecipe = id;
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ANVIL_IRON.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (slotIndex < ANVIL_SLOT_COUNT) {
            if (!moveItemStackTo(source, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            if (slotIndex == HBMAnvilBlockEntity.SLOT_OUTPUT) {
                slot.onTake(player, original);
            }
        } else if (isSteelUpgradeInput(source) || isBrickMaterial(source) || source.is(ModItems.PLATE_IRON.get()) || source.is(ModItems.INGOT_COPPER.get())) {
            if (!moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(net.minecraft.world.item.Items.IRON_INGOT) || source.is(ModItems.INGOT_STEEL.get()) || source.is(ModItems.INGOT_ALUMINIUM.get())) {
            if (!moveItemStackTo(source, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(source, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private static boolean isBrickMaterial(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.BRICK) || stack.is(net.minecraft.world.item.Items.NETHER_BRICK) || stack.is(ModBlocks.BRICK_CONCRETE.get().asItem()) || stack.is(ModBlocks.BRICK_CONCRETE_CRACKED.get().asItem()) || stack.is(ModBlocks.BRICK_CONCRETE_MOSSY.get().asItem()) || stack.is(ModBlocks.BRICK_CONCRETE_BROKEN.get().asItem()) || stack.is(ModBlocks.BRICK_CONCRETE_MARKED.get().asItem());
    }

    private static boolean isSteelUpgradeInput(ItemStack stack) {
        return stack.is(ModItems.STAMP_IRON_FLAT.get()) || stack.is(ModItems.STAMP_STEEL_FLAT.get());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    private final class AnvilOutputSlot extends SlotItemHandler {
        private AnvilOutputSlot(ItemStackHandler inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            anvil.craftSelected();
            super.onTake(player, stack);
        }
    }
}
