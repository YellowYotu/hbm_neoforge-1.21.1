package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.CrucibleBlock;
import com.yellowyotu.hbmneoforge.foundry.CrucibleRecipeRegistry;
import com.yellowyotu.hbmneoforge.foundry.FoundryMaterialRegistry;
import com.yellowyotu.hbmneoforge.heat.HeatSource;
import com.yellowyotu.hbmneoforge.menu.CrucibleMenu;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CrucibleBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_HEAT = 100_000;
    public static final int PROCESS_HEAT = 20_000;
    public static final int RECIPE_CAPACITY = FoundryMaterialRegistry.BLOCK * 16;
    public static final int WASTE_CAPACITY = FoundryMaterialRegistry.BLOCK * 16;
    private static final double DIFFUSION = 0.25D;
    private static final int POUR_RATE = FoundryMaterialRegistry.NUGGET * 3;

    private final ItemStackHandler items = new ItemStackHandler(10) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot > 0 && isItemSmeltable(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 0 : 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private final LinkedHashMap<String, Integer> recipeStack = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> wasteStack = new LinkedHashMap<>();
    private int heat;
    private int progress;
    private String recipe = "null";
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? heat : progress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                heat = value;
            } else {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        crucible.pullHeat();
        if (level.getGameTime() % 5L == 0L) {
            crucible.collectDroppedItems();
        }
        crucible.damageEntitiesInMoltenMetal();
        if (!crucible.trySmelt()) {
            crucible.progress = 0;
        }
        crucible.tryRecipe();
        crucible.pourStacks(state);
        crucible.setChangedAndSync();
    }

    private void pullHeat() {
        if (level == null || heat >= MAX_HEAT) {
            return;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        if (below instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            diff = Math.min(diff, MAX_HEAT - heat);
            if (diff > 0) {
                diff = (int) Math.ceil(diff * DIFFUSION);
                int extracted = source.extractHeat(diff);
                heat = Math.min(MAX_HEAT, heat + extracted);
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    private void collectDroppedItems() {
        if (level == null) {
            return;
        }
        AABB box = new AABB(worldPosition.getX() - 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() - 0.5D,
                worldPosition.getX() + 1.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 1.5D);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (!entity.isAlive()) {
                continue;
            }
            ItemStack stack = entity.getItem();
            if (!isItemSmeltable(stack)) {
                continue;
            }
            for (int slot = 1; slot < 10 && !stack.isEmpty(); slot++) {
                if (!items.getStackInSlot(slot).isEmpty()) {
                    continue;
                }
                items.setStackInSlot(slot, stack.copyWithCount(1));
                stack.shrink(1);
                if (stack.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(stack);
                }
                entity.setPickUpDelay(60);
            }
        }
    }

    private void damageEntitiesInMoltenMetal() {
        if (level == null) {
            return;
        }
        int total = getRecipeAmount() + getWasteAmount();
        if (total <= 0) {
            return;
        }
        double liquidLevel = ((double) total / (RECIPE_CAPACITY + WASTE_CAPACITY)) * 0.875D;
        AABB box = new AABB(worldPosition.getX() - 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() - 0.5D,
                worldPosition.getX() + 1.5D, worldPosition.getY() + 0.5D + liquidLevel, worldPosition.getZ() + 1.5D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            entity.hurt(level.damageSources().lava(), 5.0F);
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 100));
        }
    }

    private boolean trySmelt() {
        if (heat < MAX_HEAT / 2) {
            return false;
        }
        int slot = getFirstSmeltableSlot();
        if (slot < 1) {
            return false;
        }
        int delta = (int) ((heat - MAX_HEAT / 2) * 0.05D);
        if (delta <= 0) {
            return true;
        }
        progress += delta;
        heat -= delta;
        if (progress >= PROCESS_HEAT) {
            progress = 0;
            FoundryMaterialRegistry.MaterialAmount material = FoundryMaterialRegistry.fromItem(items.getStackInSlot(slot));
            if (material != null) {
                CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
                boolean recipeMaterial = loaded != null && (loaded.input().containsKey(material.material()) || loaded.output().containsKey(material.material()));
                if (loaded == null || recipeMaterial) {
                    add(recipeStack, material.material(), material.amount());
                } else {
                    add(wasteStack, material.material(), material.amount());
                }
                items.extractItem(slot, 1, false);
            }
        }
        return true;
    }

    private void tryRecipe() {
        if (level == null) {
            return;
        }
        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        if (loaded == null || level.getGameTime() % loaded.frequency() != 0L) {
            return;
        }
        for (Map.Entry<String, Integer> input : loaded.input().entrySet()) {
            if (recipeStack.getOrDefault(input.getKey(), 0) < input.getValue()) {
                return;
            }
        }
        for (Map.Entry<String, Integer> input : loaded.input().entrySet()) {
            subtract(recipeStack, input.getKey(), input.getValue());
        }
        for (Map.Entry<String, Integer> output : loaded.output().entrySet()) {
            add(recipeStack, output.getKey(), output.getValue());
        }
    }

    private int getFirstSmeltableSlot() {
        for (int slot = 1; slot < 10; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty() && isItemSmeltable(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public boolean isItemSmeltable(ItemStack stack) {
        FoundryMaterialRegistry.MaterialAmount material = FoundryMaterialRegistry.fromItem(stack);
        if (material == null) {
            return false;
        }
        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        int recipeAmount = getRecipeAmount();
        int wasteAmount = getWasteAmount();
        if (loaded == null) {
            return recipeAmount + material.amount() <= RECIPE_CAPACITY;
        }
        int required = loaded.input().getOrDefault(material.material(), 0);
        if (loaded.output().containsKey(material.material())) {
            return recipeAmount + material.amount() <= RECIPE_CAPACITY;
        }
        if (required == 0) {
            return wasteAmount + material.amount() <= WASTE_CAPACITY;
        }
        int maximum = required * RECIPE_CAPACITY / loaded.inputAmount();
        int stored = recipeStack.getOrDefault(material.material(), 0);
        return stored + material.amount() <= maximum && recipeAmount + material.amount() <= RECIPE_CAPACITY;
    }

    private void pourStacks(BlockState state) {
        if (level == null || !state.hasProperty(CrucibleBlock.FACING)) {
            return;
        }
        Direction front = state.getValue(CrucibleBlock.FACING);
        pourOne(wasteStack, front.getOpposite(), false);

        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        if (loaded == null) {
            pourOne(recipeStack, front, false);
        } else {
            pourOne(recipeStack, front, true);
        }
    }

    private void pourOne(LinkedHashMap<String, Integer> stack, Direction direction, boolean outputsOnly) {
        if (stack.isEmpty() || level == null) {
            return;
        }

        BlockPos mouth = worldPosition.relative(direction, 2);
        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        Iterator<Map.Entry<String, Integer>> iterator = stack.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (outputsOnly && loaded != null && !loaded.output().containsKey(entry.getKey())) {
                continue;
            }

            PourTarget target = findPourTarget(mouth, entry.getKey());
            if (target == null) {
                continue;
            }

            int request = Math.min(POUR_RATE, entry.getValue());
            int accepted;
            if (target.entity instanceof FoundryCastingBlockEntity casting) {
                accepted = casting.insertPour(entry.getKey(), request);
            } else {
                accepted = target.entity.insertMolten(entry.getKey(), request);
            }
            if (accepted <= 0) {
                continue;
            }

            entry.setValue(entry.getValue() - accepted);
            if (entry.getValue() <= 0) {
                iterator.remove();
            }
            return;
        }
    }

    private PourTarget findPourTarget(BlockPos mouth, String incomingMaterial) {
        if (level == null) {
            return null;
        }
        for (int distance = 0; distance <= 6; distance++) {
            BlockPos targetPos = mouth.below(distance);
            BlockEntity entity = level.getBlockEntity(targetPos);
            if (entity instanceof FoundryCastingBlockEntity casting) {
                return casting.canReceivePour(Direction.UP, incomingMaterial)
                        ? new PourTarget(casting, targetPos)
                        : null;
            }
            if (entity instanceof FoundryBaseBlockEntity foundry) {
                return foundry.canReceiveFrom(Direction.UP, incomingMaterial)
                        ? new PourTarget(foundry, targetPos)
                        : null;
            }
            if (!level.getBlockState(targetPos).isAir()) {
                return null;
            }
        }
        return null;
    }

    public boolean canAcceptPour(String material) {
        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        if (loaded == null) {
            return getWasteAmount() < WASTE_CAPACITY;
        }
        int required = loaded.input().getOrDefault(material, 0);
        if (required <= 0) {
            return false;
        }
        int maximum = required * RECIPE_CAPACITY / loaded.inputAmount();
        return recipeStack.getOrDefault(material, 0) < maximum && getRecipeAmount() < RECIPE_CAPACITY;
    }

    public int pourIntoCrucible(String material, int amount) {
        if (amount <= 0) {
            return 0;
        }
        CrucibleRecipeRegistry.Recipe loaded = getLoadedRecipe();
        if (loaded == null) {
            int accepted = Math.min(amount, WASTE_CAPACITY - getWasteAmount());
            add(wasteStack, material, accepted);
            return accepted;
        }
        int required = loaded.input().getOrDefault(material, 0);
        if (required <= 0) {
            return 0;
        }
        int maximum = required * RECIPE_CAPACITY / loaded.inputAmount();
        int accepted = Math.min(amount, Math.min(maximum - recipeStack.getOrDefault(material, 0), RECIPE_CAPACITY - getRecipeAmount()));
        if (accepted > 0) {
            add(recipeStack, material, accepted);
        }
        return Math.max(accepted, 0);
    }

    public void cycleRecipe(int delta) {
        java.util.List<CrucibleRecipeRegistry.Recipe> recipes = CrucibleRecipeRegistry.recipes();
        int index = -1;
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).name().equals(recipe)) {
                index = i;
                break;
            }
        }
        index += delta;
        if (index < -1) {
            index = recipes.size() - 1;
        }
        if (index >= recipes.size()) {
            index = -1;
        }
        recipe = index == -1 ? "null" : recipes.get(index).name();
        setChangedAndSync();
    }

    public void selectRecipe(int index) {
        java.util.List<CrucibleRecipeRegistry.Recipe> recipes = CrucibleRecipeRegistry.recipes();
        recipe = index >= 0 && index < recipes.size() ? recipes.get(index).name() : "null";
        setChangedAndSync();
    }

    public String getRecipeName() {
        return recipe;
    }

    public CrucibleRecipeRegistry.Recipe getSelectedRecipe() {
        return getLoadedRecipe();
    }

    private CrucibleRecipeRegistry.Recipe getLoadedRecipe() {
        return CrucibleRecipeRegistry.get(recipe);
    }

    private static void add(Map<String, Integer> stack, String material, int amount) {
        if (amount > 0) {
            stack.merge(material, amount, Integer::sum);
        }
    }

    private static void subtract(Map<String, Integer> stack, String material, int amount) {
        int left = stack.getOrDefault(material, 0) - amount;
        if (left <= 0) {
            stack.remove(material);
        } else {
            stack.put(material, left);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    public int getHeat() {
        return heat;
    }

    public int getProgress() {
        return progress;
    }

    public int getRecipeAmount() {
        return recipeStack.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getWasteAmount() {
        return wasteStack.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getMoltenAmount() {
        return getRecipeAmount() + getWasteAmount();
    }

    public Map<String, Integer> getRecipeStack() {
        return Map.copyOf(recipeStack);
    }

    public Map<String, Integer> getWasteStack() {
        return Map.copyOf(wasteStack);
    }

    public int getPrimaryMoltenColor() {
        if (!recipeStack.isEmpty()) {
            return FoundryMaterialRegistry.color(recipeStack.keySet().iterator().next());
        }
        if (!wasteStack.isEmpty()) {
            return FoundryMaterialRegistry.color(wasteStack.keySet().iterator().next());
        }
        return 0xFF4A00;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 1; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
            }
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
        return Component.translatable("block.hbm_neoforge.machine_crucible");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CrucibleMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Heat", heat);
        tag.putInt("Progress", progress);
        tag.putString("Recipe", recipe);
        tag.put("RecipeStack", saveStack(recipeStack));
        tag.put("WasteStack", saveStack(wasteStack));
    }

    private static CompoundTag saveStack(Map<String, Integer> stack) {
        CompoundTag tag = new CompoundTag();
        stack.forEach(tag::putInt);
        return tag;
    }

    private static void loadStack(CompoundTag tag, Map<String, Integer> stack) {
        stack.clear();
        for (String key : tag.getAllKeys()) {
            int amount = tag.getInt(key);
            if (amount > 0) {
                stack.put(key, amount);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        heat = tag.getInt("Heat");
        progress = tag.getInt("Progress");
        recipe = tag.contains("Recipe") ? tag.getString("Recipe") : "null";
        if (tag.contains("RecipeStack")) {
            loadStack(tag.getCompound("RecipeStack"), recipeStack);
        }
        if (tag.contains("WasteStack")) {
            loadStack(tag.getCompound("WasteStack"), wasteStack);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), registries);
        }
    }

    private record PourTarget(FoundryBaseBlockEntity entity, BlockPos pos) {
    }
}
