package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AssemblyMachineRecipes {
    public record Ingredient(Supplier<ItemStack> stack, int count) {
        public ItemStack displayStack() {
            ItemStack result = stack.get().copy();
            result.setCount(count);
            return result;
        }
    }

    public record Recipe(String id, Supplier<ItemStack> result, int duration, int energyPerTick, List<Ingredient> ingredients) {
        public ItemStack resultStack() {
            return result.get().copy();
        }
    }

    public static final List<Recipe> RECIPES = List.of(
            new Recipe("machine_shredder", () -> new ItemStack(ModBlocks.SHREDDER.get()), 100, 100, List.of(
                    ingredient(() -> new ItemStack(ModItems.PLATE_STEEL.get()), 8),
                    ingredient(() -> new ItemStack(ModItems.PLATE_COPPER.get()), 4),
                    ingredient(() -> new ItemStack(ModItems.MOTOR.get()), 2)
            )),
            new Recipe("sliding_seal_door", () -> new ItemStack(ModBlocks.SLIDING_SEAL_DOOR.get()), 400, 25, List.of(
                    ingredient(() -> new ItemStack(ModItems.PLATE_STEEL.get()), 12),
                    ingredient(() -> new ItemStack(ModItems.PLATE_POLYMER.get()), 4),
                    ingredient(() -> new ItemStack(ModItems.MOTOR.get()), 2),
                    ingredient(() -> new ItemStack(ModItems.BOLT_DURA_STEEL.get()), 4),
                    ingredient(() -> new ItemStack(Items.BONE_MEAL), 2)
            )),
            new Recipe("machine_soldering_station", () -> new ItemStack(ModBlocks.SOLDERING_STATION.get()), 300, 250, List.of(
                    ingredient(() -> new ItemStack(ModItems.PLATE_STEEL.get()), 2),
                    ingredient(() -> new ItemStack(ModItems.COPPER_COIL.get()), 4),
                    ingredient(() -> new ItemStack(ModItems.BOLT_TUNGSTEN.get()), 4),
                    ingredient(() -> new ItemStack(ModItems.CIRCUIT_VACUUM_TUBE.get()), 2)
            )),
            new Recipe("hazmat_cloth", () -> new ItemStack(ModItems.HAZMAT_CLOTH.get(), 4), 50, 100, List.of(
                    ingredient(() -> new ItemStack(ModItems.INGOT_LEAD.get()), 4),
                    ingredient(() -> new ItemStack(Items.STRING), 8)
            ))
    );

    private static Ingredient ingredient(Supplier<ItemStack> stack, int count) {
        return new Ingredient(stack, count);
    }

    private AssemblyMachineRecipes() {
    }

    public static Recipe get(int index) {
        return RECIPES.get(Math.floorMod(index, RECIPES.size()));
    }
}
