package com.yellowyotu.hbmneoforge.blockentity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class HBMAnvilRecipes {
    private static final List<LoadedRecipe> LOADED_RECIPES = loadRecipes();
    private static final List<Recipe> RECIPES = LOADED_RECIPES.stream()
            .filter(LoadedRecipe::selectable)
            .map(LoadedRecipe::recipe)
            .toList();
    private static final List<Recipe> SLOT_RECIPES = LOADED_RECIPES.stream()
            .filter(recipe -> !recipe.selectable())
            .map(LoadedRecipe::recipe)
            .toList();

    private HBMAnvilRecipes() {
    }

    public static List<Recipe> all() {
        return LOADED_RECIPES.stream()
                .map(LoadedRecipe::recipe)
                .toList();
    }

    public static int size() {
        return RECIPES.size();
    }

    public static Recipe get(int index) {
        return RECIPES.get(Math.floorMod(index, RECIPES.size()));
    }

    public static void updateOutput(ItemStackHandler inventory, int selectedRecipe, int anvilTier) {
        Recipe recipe = findTopSlotRecipe(inventory);

        if (recipe == null) {
            recipe = get(selectedRecipe);
        }

        if (recipe.tier() > anvilTier) {
            inventory.setStackInSlot(HBMAnvilBlockEntity.SLOT_OUTPUT, ItemStack.EMPTY);
            return;
        }

        ItemStack primary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY);

        ItemStack result = recipe.matches(primary, secondary)
                ? recipe.result().get().copy()
                : ItemStack.EMPTY;

        inventory.setStackInSlot(HBMAnvilBlockEntity.SLOT_OUTPUT, result);
    }

    public static ItemStack craft(ItemStackHandler inventory, int selectedRecipe, int anvilTier) {
        Recipe recipe = findTopSlotRecipe(inventory);

        if (recipe == null) {
            recipe = get(selectedRecipe);
        }

        if (recipe.tier() > anvilTier) {
            return ItemStack.EMPTY;
        }

        ItemStack primary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY);

        if (!recipe.matches(primary, secondary)) {
            updateOutput(inventory, selectedRecipe, anvilTier);
            return ItemStack.EMPTY;
        }

        ItemStack result = recipe.result().get().copy();

        primary.shrink(recipe.primaryCount());

        if (recipe.secondaryCount() > 0) {
            secondary.shrink(recipe.secondaryCount());
        }

        inventory.setStackInSlot(
                HBMAnvilBlockEntity.SLOT_PRIMARY,
                primary.isEmpty() ? ItemStack.EMPTY : primary);

        inventory.setStackInSlot(
                HBMAnvilBlockEntity.SLOT_SECONDARY,
                secondary.isEmpty() ? ItemStack.EMPTY : secondary);

        updateOutput(inventory, selectedRecipe, anvilTier);

        return result;
    }

    public static ItemStack craftFromPlayerInventory(Inventory inventory, int selectedRecipe, int anvilTier) {
        Recipe recipe = get(selectedRecipe);

        if (recipe.tier() > anvilTier) {
            return ItemStack.EMPTY;
        }

        if (!hasInPlayerInventory(inventory, recipe.primary(), recipe.primaryCount())) {
            return ItemStack.EMPTY;
        }

        if (!hasInPlayerInventory(inventory, recipe.secondary(), recipe.secondaryCount())) {
            return ItemStack.EMPTY;
        }

        for (Input input : recipe.extraInputs()) {
            if (!hasInPlayerInventory(inventory, input.predicate(), input.count())) {
                return ItemStack.EMPTY;
            }
        }

        removeFromPlayerInventory(inventory, recipe.primary(), recipe.primaryCount());
        removeFromPlayerInventory(inventory, recipe.secondary(), recipe.secondaryCount());

        for (Input input : recipe.extraInputs()) {
            removeFromPlayerInventory(inventory, input.predicate(), input.count());
        }

        inventory.setChanged();

        return recipe.result().get().copy();
    }

    private static Recipe findTopSlotRecipe(ItemStackHandler inventory) {
        ItemStack primary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(HBMAnvilBlockEntity.SLOT_SECONDARY);

        for (Recipe recipe : SLOT_RECIPES) {
            if (recipe.matches(primary, secondary)) {
                return recipe;
            }
        }

        return null;
    }

    private static boolean hasInPlayerInventory(
            Inventory inventory,
            Predicate<ItemStack> predicate,
            int count) {

        if (count <= 0) {
            return true;
        }

        int found = 0;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);

            if (!predicate.test(stack)) {
                continue;
            }

            found += stack.getCount();

            if (found >= count) {
                return true;
            }
        }

        return false;
    }

    private static void removeFromPlayerInventory(
            Inventory inventory,
            Predicate<ItemStack> predicate,
            int count) {

        if (count <= 0) {
            return;
        }

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

    private static List<LoadedRecipe> loadRecipes() {
        List<LoadedRecipe> recipes = new ArrayList<>();
        JsonArray index = MachineRecipeJsonLoader.readIndex("anvil");

        for (int i = 0; i < index.size(); i++) {
            String id = index.get(i).getAsString();
            JsonObject json = MachineRecipeJsonLoader.readRecipe("anvil", id);

            OperationType operationType = OperationType.valueOf(
                    json.get("operation").getAsString().toUpperCase());

            OverlayType overlayType = OverlayType.valueOf(
                    json.get("overlay").getAsString().toUpperCase());

            Component name = Component.translatable(
                    json.get("name").getAsString());

            Input primary = readInput(json.getAsJsonObject("primary"));
            Input secondary = readInput(json.getAsJsonObject("secondary"));

            ItemStack resultStack = MachineRecipeJsonLoader.readStack(
                    json.getAsJsonObject("result"));

            boolean selectable = !json.has("selectable")
                    || json.get("selectable").getAsBoolean();

            int tier = json.has("tier")
                    ? json.get("tier").getAsInt()
                    : 1;

            List<Input> extraInputs = new ArrayList<>();

            if (json.has("extra_inputs")) {
                JsonArray extraJson = json.getAsJsonArray("extra_inputs");

                for (int extraIndex = 0; extraIndex < extraJson.size(); extraIndex++) {
                    extraInputs.add(
                            readInput(extraJson.get(extraIndex).getAsJsonObject()));
                }
            }

            Recipe recipe = new Recipe(
                    operationType,
                    overlayType,
                    name,
                    tier,
                    primary.predicate(),
                    primary.count(),
                    secondary.predicate(),
                    secondary.count(),
                    primary.display(),
                    secondary.display(),
                    List.copyOf(extraInputs),
                    () -> resultStack.copy());

            recipes.add(new LoadedRecipe(recipe, selectable));
        }

        return List.copyOf(recipes);
    }

    private static Input readInput(JsonObject json) {
        int count = json.has("count")
                ? json.get("count").getAsInt()
                : 0;

        if (json.has("any") && json.get("any").getAsBoolean()) {
            return new Input(
                    stack -> true,
                    count,
                    () -> ItemStack.EMPTY);
        }

        JsonArray itemsJson = json.getAsJsonArray("items");
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < itemsJson.size(); i++) {
            items.add(
                    MachineRecipeJsonLoader.getItem(
                            itemsJson.get(i).getAsString()));
        }

        Predicate<ItemStack> predicate = stack ->
                items.stream().anyMatch(stack::is);

        Item displayItem = items.getFirst();

        return new Input(
                predicate,
                count,
                () -> new ItemStack(displayItem));
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

    public record Recipe(
            OperationType operationType,
            OverlayType overlayType,
            Component name,
            int tier,
            Predicate<ItemStack> primary,
            int primaryCount,
            Predicate<ItemStack> secondary,
            int secondaryCount,
            Supplier<ItemStack> primaryDisplay,
            Supplier<ItemStack> secondaryDisplay,
            List<Input> extraInputs,
            Supplier<ItemStack> result) {

        public boolean matches(ItemStack primaryStack, ItemStack secondaryStack) {
            if (!extraInputs.isEmpty()) {
                return false;
            }

            boolean primaryMatches =
                    primaryStack.getCount() >= primaryCount
                            && primary.test(primaryStack);

            boolean secondaryMatches =
                    secondaryCount == 0
                            || secondaryStack.getCount() >= secondaryCount
                            && secondary.test(secondaryStack);

            return primaryMatches && secondaryMatches;
        }

        public ItemStack displayResult() {
            return result.get().copy();
        }
    }

    public record Input(
            Predicate<ItemStack> predicate,
            int count,
            Supplier<ItemStack> display) {
    }

    private record LoadedRecipe(
            Recipe recipe,
            boolean selectable) {
    }
}