package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class HBMAnvilRecipes {
    private static final Predicate<ItemStack> BRICK_MATERIAL = stack -> stack.is(Items.BRICK) || stack.is(Items.NETHER_BRICK);

    private static final List<Recipe> RECIPES = List.of(
            new Recipe(OperationType.CRAFTING, OverlayType.CONSTRUCTION, Component.translatable("anvil_recipe.hbm_neoforge.iron_flat"), BRICK_MATERIAL, 3, stack -> stack.is(Items.IRON_INGOT), 3, () -> new ItemStack(Items.BRICK), () -> new ItemStack(Items.IRON_INGOT), () -> new ItemStack(ModItems.STAMP_IRON_FLAT.get())),
            stampVariant("anvil_recipe.hbm_neoforge.iron_plate", ModItems.STAMP_IRON_FLAT, ModItems.IRON_PLATE_STAMP),
            stampVariant("anvil_recipe.hbm_neoforge.iron_wire", ModItems.STAMP_IRON_FLAT, ModItems.STAMP_IRON_WIRE),
            stampVariant("anvil_recipe.hbm_neoforge.iron_circuit", ModItems.STAMP_IRON_FLAT, ModItems.STAMP_IRON_CIRCUIT),
            new Recipe(OperationType.CRAFTING, OverlayType.CONSTRUCTION, Component.translatable("anvil_recipe.hbm_neoforge.steel_flat"), BRICK_MATERIAL, 3, stack -> stack.is(ModItems.INGOT_STEEL.get()), 3, () -> new ItemStack(Items.BRICK), () -> new ItemStack(ModItems.INGOT_STEEL.get()), () -> new ItemStack(ModItems.STAMP_STEEL_FLAT.get())),
            stampVariant("anvil_recipe.hbm_neoforge.steel_plate", ModItems.STAMP_STEEL_FLAT, ModItems.STAMP_STEEL_PLATE),
            stampVariant("anvil_recipe.hbm_neoforge.steel_wire", ModItems.STAMP_STEEL_FLAT, ModItems.STAMP_STEEL_WIRE),
            stampVariant("anvil_recipe.hbm_neoforge.steel_circuit", ModItems.STAMP_STEEL_FLAT, ModItems.STAMP_STEEL_CIRCUIT),
            new Recipe(OperationType.CRAFTING, OverlayType.CONSTRUCTION, Component.translatable("anvil_recipe.hbm_neoforge.iron_pipe"), stack -> stack.is(ModItems.PLATE_IRON.get()), 3, stack -> true, 0, () -> new ItemStack(ModItems.PLATE_IRON.get()), () -> ItemStack.EMPTY, () -> new ItemStack(ModItems.IRON_PIPE.get()))
    );
    private static final Recipe GUNMETAL_RECIPE = new Recipe(OperationType.CRAFTING, OverlayType.CONSTRUCTION, Component.translatable("anvil_recipe.hbm_neoforge.gunmetal"), stack -> stack.is(ModItems.INGOT_COPPER.get()), 1, stack -> stack.is(ModItems.INGOT_ALUMINIUM.get()), 1, () -> new ItemStack(ModItems.INGOT_COPPER.get()), () -> new ItemStack(ModItems.INGOT_ALUMINIUM.get()), () -> new ItemStack(ModItems.INGOT_GUNMETAL.get()));

    private HBMAnvilRecipes() {
    }

    public static List<Recipe> all() {
        return java.util.stream.Stream.concat(RECIPES.stream(), java.util.stream.Stream.of(GUNMETAL_RECIPE)).toList();
    }

    public static int size() {
        return RECIPES.size();
    }

    public static Recipe get(int index) {
        return RECIPES.get(Math.floorMod(index, RECIPES.size()));
    }

    public static void updateOutput(ItemStackHandler inventory, int selectedRecipe) {
        Recipe recipe = findTopSlotRecipe(inventory);
        if (recipe == null) {
            recipe = get(selectedRecipe);
        }
        ItemStack result = recipe.matches(inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY), inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY)) ? recipe.result().get() : ItemStack.EMPTY;
        inventory.setStackInSlot(HBMAnvilBlockEntity.SLOT_OUTPUT, result);
    }

    public static ItemStack craft(ItemStackHandler inventory, int selectedRecipe) {
        Recipe recipe = findTopSlotRecipe(inventory);
        if (recipe == null) {
            recipe = get(selectedRecipe);
        }
        ItemStack primary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY);
        if (!recipe.matches(primary, secondary)) {
            updateOutput(inventory, selectedRecipe);
            return ItemStack.EMPTY;
        }
        ItemStack result = recipe.result().get().copy();
        primary.shrink(recipe.primaryCount());
        if (recipe.secondaryCount() > 0) {
            secondary.shrink(recipe.secondaryCount());
        }
        inventory.setStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY, primary.isEmpty() ? ItemStack.EMPTY : primary);
        inventory.setStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY, secondary.isEmpty() ? ItemStack.EMPTY : secondary);
        updateOutput(inventory, selectedRecipe);
        return result;
    }

    public static ItemStack craftFromPlayerInventory(Inventory inventory, int selectedRecipe) {
        Recipe recipe = get(selectedRecipe);
        if (!hasInPlayerInventory(inventory, recipe.primary(), recipe.primaryCount()) || !hasInPlayerInventory(inventory, recipe.secondary(), recipe.secondaryCount())) {
            return ItemStack.EMPTY;
        }
        removeFromPlayerInventory(inventory, recipe.primary(), recipe.primaryCount());
        removeFromPlayerInventory(inventory, recipe.secondary(), recipe.secondaryCount());
        inventory.setChanged();
        return recipe.result().get().copy();
    }


    private static Recipe findTopSlotRecipe(ItemStackHandler inventory) {
        ItemStack primary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY);
        return GUNMETAL_RECIPE.matches(primary, secondary) ? GUNMETAL_RECIPE : null;
    }

    private static boolean hasInPlayerInventory(Inventory inventory, Predicate<ItemStack> predicate, int count) {
        if (count <= 0) {
            return true;
        }
        int found = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (predicate.test(stack)) {
                found += stack.getCount();
                if (found >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeFromPlayerInventory(Inventory inventory, Predicate<ItemStack> predicate, int count) {
        int remaining = count;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!predicate.test(stack)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static Recipe stampVariant(String name, Supplier<? extends net.minecraft.world.item.Item> input, Supplier<? extends net.minecraft.world.item.Item> output) {
        return new Recipe(OperationType.REFORGING, OverlayType.SMITHING, Component.translatable(name), stack -> stack.is(input.get()), 1, stack -> true, 0, () -> new ItemStack(input.get()), () -> ItemStack.EMPTY, () -> new ItemStack(output.get()));
    }


    public enum OperationType {
        CRAFTING,
        REFORGING
    }

    public enum OverlayType {
        NONE,
        CONSTRUCTION,
        RECYCLING,
        SMITHING
    }

    public record Recipe(OperationType operationType, OverlayType overlayType, Component name, Predicate<ItemStack> primary, int primaryCount, Predicate<ItemStack> secondary, int secondaryCount, Supplier<ItemStack> primaryDisplay, Supplier<ItemStack> secondaryDisplay, Supplier<ItemStack> result) {
        public boolean matches(ItemStack primaryStack, ItemStack secondaryStack) {
            boolean primaryMatches = primaryStack.getCount() >= primaryCount && primary.test(primaryStack);
            boolean secondaryMatches = secondaryCount == 0 || secondaryStack.getCount() >= secondaryCount && secondary.test(secondaryStack);
            return primaryMatches && secondaryMatches;
        }

        public ItemStack displayResult() {
            return result.get();
        }
    }
}
