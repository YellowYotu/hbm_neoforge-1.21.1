package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModMenus;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.menu.StorageCrateMenu;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class StorageCrateBlockItem extends BlockItem {
    private static final String INVENTORY_TAG = "CrateInventory";
    private final int rows;

    public StorageCrateBlockItem(Block block, Properties properties, int rows) {
        super(block, properties);
        this.rows = rows;
    }

    public static boolean hasStoredContents(ItemStack stack) {
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(INVENTORY_TAG)) {
            return false;
        }
        CompoundTag inventory = custom.getCompound(INVENTORY_TAG);
        return inventory.contains("Items") && !inventory.getList("Items", Tag.TAG_COMPOUND).isEmpty();
    }


    public static void storeContents(ItemStack stack, net.minecraft.world.Container container, HolderLookup.Provider registries) {
        CompoundTag inventory = new CompoundTag();
        NonNullList<ItemStack> stacks = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, container.getItem(i).copy());
        }
        ContainerHelper.saveAllItems(inventory, stacks, true, registries);
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        custom.put(INVENTORY_TAG, inventory);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        stack.set(DataComponents.MAX_STACK_SIZE, container.isEmpty() ? 64 : 1);
    }

    public static void loadContents(ItemStack stack, net.minecraft.world.Container container, HolderLookup.Provider registries) {
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(INVENTORY_TAG)) {
            return;
        }
        NonNullList<ItemStack> stacks = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(custom.getCompound(INVENTORY_TAG), stacks, registries);
        for (int i = 0; i < stacks.size(); i++) {
            container.setItem(i, stacks.get(i));
        }
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BoundCrateContainer container = new BoundCrateContainer(rows * 9, stack, level.registryAccess());
            Component title = Component.translatable(rows == 6 ? "container.hbm_neoforge.crate_steel" : "container.hbm_neoforge.crate_iron");
            level.playSound(null, player.blockPosition(), ModSounds.CRATE_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, p) -> new StorageCrateMenu(id, inventory, container, rows),
                    title), buffer -> {
                buffer.writeVarInt(rows);
                buffer.writeBoolean(true);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static final class BoundCrateContainer extends SimpleContainer {
        private final ItemStack boundStack;
        private final HolderLookup.Provider registries;
        private boolean loading;

        public BoundCrateContainer(int size, ItemStack boundStack, HolderLookup.Provider registries) {
            super(size);
            this.boundStack = boundStack;
            this.registries = registries;
            load();
        }

        private void load() {
            loading = true;
            try {
                CompoundTag custom = boundStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (!custom.contains(INVENTORY_TAG)) {
                    return;
                }
                CompoundTag inventory = custom.getCompound(INVENTORY_TAG);
                NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
                ContainerHelper.loadAllItems(inventory, stacks, registries);
                for (int i = 0; i < stacks.size(); i++) {
                    super.setItem(i, stacks.get(i));
                }
            } finally {
                loading = false;
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof StorageCrateBlockItem) || !StorageCrateBlockItem.hasStoredContents(stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (loading) {
                return;
            }
            CompoundTag inventory = new CompoundTag();
            NonNullList<ItemStack> stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < stacks.size(); i++) {
                stacks.set(i, getItem(i));
            }
            ContainerHelper.saveAllItems(inventory, stacks, true, registries);
            CompoundTag custom = boundStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            custom.put(INVENTORY_TAG, inventory);
            boundStack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
            boundStack.set(DataComponents.MAX_STACK_SIZE, isEmpty() ? 64 : 1);
        }
    }
}
