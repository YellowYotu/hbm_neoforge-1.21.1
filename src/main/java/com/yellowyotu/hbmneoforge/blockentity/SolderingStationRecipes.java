package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.item.ItemSolderingFluidCell;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class SolderingStationRecipes {

    public record Ingredient(Supplier<ItemStack> stack, int count) {
        public ItemStack displayStack() {
            ItemStack result = stack.get().copy();
            result.setCount(count);
            return result;
        }
    }

    public record FluidRequirement(ItemSolderingFluidCell.FluidType type, int amount) {
    }

    public record Recipe(String id, Supplier<ItemStack> result, int duration, int energyPerTick, FluidRequirement fluid, List<Ingredient> toppings, List<Ingredient> pcb, List<Ingredient> solder) {
        public ItemStack resultStack() {
            return result.get().copy();
        }
    }

    public static final List<Recipe> RECIPES = createRecipes();

    private SolderingStationRecipes() {
    }

    public static Recipe find(ItemStackHandler inventory) {
        for (Recipe recipe : RECIPES) {
            if (matchesGroup(inventory, 0, 3, recipe.toppings()) && matchesGroup(inventory, 3, 2, recipe.pcb()) && matchesGroup(inventory, 5, 1, recipe.solder())) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isValidTopping(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.TOPPINGS);
    }

    public static boolean isValidPcb(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.PCB);
    }

    public static boolean isValidSolder(ItemStack stack) {
        return isValidForAnyGroup(stack, Group.SOLDER);
    }

    private static boolean isValidForAnyGroup(ItemStack stack, Group group) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Recipe recipe : RECIPES) {
            List<Ingredient> ingredients = switch (group) {
                case TOPPINGS -> recipe.toppings();
                case PCB -> recipe.pcb();
                case SOLDER -> recipe.solder();
            };
            for (Ingredient ingredient : ingredients) {
                if (ItemStack.isSameItemSameComponents(stack, ingredient.stack().get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesGroup(ItemStackHandler inventory, int firstSlot, int slotCount, List<Ingredient> ingredients) {
        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (int slot = firstSlot; slot < firstSlot + slotCount; slot++) {
            ItemStack input = inventory.getStackInSlot(slot);
            if (input.isEmpty()) {
                continue;
            }

            Ingredient match = null;
            for (Ingredient ingredient : remaining) {
                if (input.getCount() >= ingredient.count() && ItemStack.isSameItemSameComponents(input, ingredient.stack().get())) {
                    match = ingredient;
                    break;
                }
            }
            if (match == null) {
                return false;
            }
            remaining.remove(match);
        }
        return remaining.isEmpty();
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("analog_circuit", ModItems.CIRCUIT_ANALOG, 100, 100, null,
                List.of(ingredient(ModItems.CIRCUIT_VACUUM_TUBE, 3), ingredient(ModItems.CIRCUIT_CAPACITOR, 2)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 4)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 4))));

        recipes.add(recipe("basic_circuit", ModItems.CIRCUIT_BASIC, 200, 250, null,
                List.of(ingredient(ModItems.CIRCUIT_CHIP, 4)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 4)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 4))));

        recipes.add(recipe("advanced_circuit", ModItems.CIRCUIT_ADVANCED, 300, 1_000, fluid(ItemSolderingFluidCell.FluidType.SULFURIC_ACID, 1_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP, 16), ingredient(ModItems.CIRCUIT_CAPACITOR, 4)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 8), ingredient(ModItems.INGOT_RUBBER, 2)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 8))));

        recipes.add(recipe("capacitor_board", ModItems.CIRCUIT_CAPACITOR_BOARD, 200, 300, fluid(ItemSolderingFluidCell.FluidType.PEROXIDE, 250),
                List.of(ingredient(ModItems.CIRCUIT_CAPACITOR_TANTALIUM, 3)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 1)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 3))));

        recipes.add(recipe("bismoid_circuit", ModItems.CIRCUIT_BISMOID, 400, 10_000, fluid(ItemSolderingFluidCell.FluidType.SOLVENT, 1_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP_BISMOID, 4), ingredient(ModItems.CIRCUIT_CHIP, 16), ingredient(ModItems.CIRCUIT_CAPACITOR, 24)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 12), ingredient(ModItems.INGOT_POLYMER, 2)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 12))));

        recipes.add(recipe("quantum_circuit", ModItems.CIRCUIT_QUANTUM, 400, 100_000, fluid(ItemSolderingFluidCell.FluidType.HELIUM4, 1_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP_QUANTUM, 4), ingredient(ModItems.CIRCUIT_CHIP_BISMOID, 16), ingredient(ModItems.CIRCUIT_ATOMIC_CLOCK, 4)),
                List.of(ingredient(ModItems.CIRCUIT_PCB, 16), ingredient(ModItems.INGOT_POLYMER, 4)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 16))));

        recipes.add(recipe("controller", ModItems.CIRCUIT_CONTROLLER, 400, 15_000, fluid(ItemSolderingFluidCell.FluidType.PERFLUOROMETHYL, 1_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP, 32), ingredient(ModItems.CIRCUIT_CAPACITOR, 32), ingredient(ModItems.CIRCUIT_CAPACITOR_TANTALIUM, 16)),
                List.of(ingredient(ModItems.CIRCUIT_CONTROLLER_CHASSIS, 1), ingredient(ModItems.UPGRADE_SPEED_1, 1)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 16))));

        recipes.add(recipe("controller_advanced", ModItems.CIRCUIT_CONTROLLER_ADVANCED, 600, 25_000, fluid(ItemSolderingFluidCell.FluidType.PERFLUOROMETHYL, 4_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP_BISMOID, 16), ingredient(ModItems.CIRCUIT_CAPACITOR_TANTALIUM, 48), ingredient(ModItems.CIRCUIT_ATOMIC_CLOCK, 1)),
                List.of(ingredient(ModItems.CIRCUIT_CONTROLLER_CHASSIS, 1), ingredient(ModItems.UPGRADE_SPEED_3, 1)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 24))));

        recipes.add(recipe("controller_quantum", ModItems.CIRCUIT_CONTROLLER_QUANTUM, 600, 250_000, fluid(ItemSolderingFluidCell.FluidType.PERFLUOROMETHYL_COLD, 6_000),
                List.of(ingredient(ModItems.CIRCUIT_CHIP_QUANTUM, 16), ingredient(ModItems.CIRCUIT_CHIP_BISMOID, 48), ingredient(ModItems.CIRCUIT_ATOMIC_CLOCK, 8)),
                List.of(ingredient(ModItems.CIRCUIT_CONTROLLER_ADVANCED, 2), ingredient(ModItems.UPGRADE_OVERDRIVE_1, 1)),
                List.of(ingredient(ModItems.WIRE_FINE_LEAD, 32))));

        recipes.add(upgradeOne("upgrade_speed_1", ModItems.UPGRADE_SPEED_1, ModItems.POWDER_RED_COPPER));
        recipes.add(upgradeOne("upgrade_power_1", ModItems.UPGRADE_POWER_1, ModItems.POWDER_GOLD));
        recipes.add(upgradeTwo("upgrade_speed_2", ModItems.UPGRADE_SPEED_1, ModItems.UPGRADE_SPEED_2));
        recipes.add(upgradeTwo("upgrade_power_2", ModItems.UPGRADE_POWER_1, ModItems.UPGRADE_POWER_2));
        recipes.add(upgradeThree("upgrade_speed_3", ModItems.UPGRADE_SPEED_2, ModItems.UPGRADE_SPEED_3));
        recipes.add(upgradeThree("upgrade_power_3", ModItems.UPGRADE_POWER_2, ModItems.UPGRADE_POWER_3));

        return List.copyOf(recipes);
    }

    private static Recipe upgradeOne(String id, Supplier<? extends net.minecraft.world.item.Item> output, Supplier<? extends net.minecraft.world.item.Item> powder) {
        return recipe(id, output, 200, 1_000, null,
                List.of(ingredient(ModItems.CIRCUIT_VACUUM_TUBE, 4), ingredient(ModItems.CIRCUIT_CAPACITOR, 1)),
                List.of(ingredient(ModItems.UPGRADE_TEMPLATE, 1), ingredient(powder, 4)),
                List.of());
    }

    private static Recipe upgradeTwo(String id, Supplier<? extends net.minecraft.world.item.Item> lower, Supplier<? extends net.minecraft.world.item.Item> output) {
        return recipe(id, output, 300, 10_000, null,
                List.of(ingredient(ModItems.CIRCUIT_CHIP, 8), ingredient(ModItems.CIRCUIT_CAPACITOR, 4)),
                List.of(ingredient(lower, 1), ingredient(ModItems.INGOT_POLYMER, 4)),
                List.of());
    }

    private static Recipe upgradeThree(String id, Supplier<? extends net.minecraft.world.item.Item> lower, Supplier<? extends net.minecraft.world.item.Item> output) {
        return recipe(id, output, 400, 25_000, fluid(ItemSolderingFluidCell.FluidType.SOLVENT, 500),
                List.of(ingredient(ModItems.CIRCUIT_CHIP, 16), ingredient(ModItems.CIRCUIT_CAPACITOR, 16)),
                List.of(ingredient(lower, 1), ingredient(ModItems.INGOT_RUBBER, 4)),
                List.of());
    }

    private static Recipe recipe(String id, Supplier<? extends net.minecraft.world.item.Item> output, int duration, int energyPerTick, FluidRequirement fluid, List<Ingredient> toppings, List<Ingredient> pcb, List<Ingredient> solder) {
        return new Recipe(id, () -> new ItemStack(output.get()), duration, energyPerTick, fluid, toppings, pcb, solder);
    }

    private static Ingredient ingredient(Supplier<? extends net.minecraft.world.item.Item> item, int count) {
        return new Ingredient(() -> new ItemStack(item.get()), count);
    }

    private static FluidRequirement fluid(ItemSolderingFluidCell.FluidType type, int amount) {
        return new FluidRequirement(type, amount);
    }

    private enum Group {
        TOPPINGS,
        PCB,
        SOLDER
    }
}
