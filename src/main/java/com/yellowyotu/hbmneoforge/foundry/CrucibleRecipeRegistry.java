package com.yellowyotu.hbmneoforge.foundry;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CrucibleRecipeRegistry {
    private static final int N = FoundryMaterialRegistry.NUGGET;
    private static final int I = FoundryMaterialRegistry.INGOT;
    private static final List<Recipe> RECIPES = List.of(
            recipe("crucible.steel", "Steel", 20, in("iron", N * 2, "carbon", N * 3, "flux", N), out("steel", N * 2), "ingot_steel"),
            recipe("crucible.redcopper", "Red Copper", 2, in("copper", N, "redstone", N), out("mingrade", N * 2), "ingot_red_copper"),
            recipe("crucible.hss", "High-Speed Steel", 9, in("steel", N * 5, "tungsten", N * 3, "cobalt", N), out("dura_steel", N * 9), "ingot_dura_steel"));
    private static final Map<String, Recipe> BY_NAME;

    static {
        Map<String, Recipe> map = new LinkedHashMap<>();
        for (Recipe recipe : RECIPES) {
            map.put(recipe.name(), recipe);
        }
        BY_NAME = Map.copyOf(map);
    }

    private CrucibleRecipeRegistry() {
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    public static Recipe get(String name) {
        return BY_NAME.get(name);
    }

    public static String displayMaterial(String material) {
        String normalized = FoundryMaterialRegistry.normalize(material);
        if (normalized.equals("mingrade")) {
            return "Red Copper";
        }
        if (normalized.equals("dura_steel") || normalized.equals("dura")) {
            return "High-Speed Steel";
        }
        StringBuilder result = new StringBuilder();
        for (String part : normalized.split("_")) {
            if (part.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private static Recipe recipe(String name, String title, int frequency, Map<String, Integer> input,
                                 Map<String, Integer> output, String iconPath) {
        return new Recipe(name, title, frequency, input, output, iconPath);
    }

    private static Map<String, Integer> in(Object... values) {
        return map(values);
    }

    private static Map<String, Integer> out(Object... values) {
        return map(values);
    }

    private static Map<String, Integer> map(Object... values) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], (Integer) values[i + 1]);
        }
        return Map.copyOf(map);
    }

    public record Recipe(String name, String title, int frequency, Map<String, Integer> input,
                         Map<String, Integer> output, String iconPath) {
        public int inputAmount() {
            return input.values().stream().mapToInt(Integer::intValue).sum();
        }

        public ItemStack icon() {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                    HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, iconPath));
            if (item == null || item == Items.AIR) {
                item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(iconPath));
            }
            return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }

        public List<Component> tooltip() {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(title));
            lines.add(Component.literal("Input:"));
            for (Map.Entry<String, Integer> entry : input.entrySet()) {
                lines.add(Component.literal("  " + displayMaterial(entry.getKey()) + ": " + formatAmount(entry.getValue())));
            }
            lines.add(Component.literal("Output:"));
            for (Map.Entry<String, Integer> entry : output.entrySet()) {
                lines.add(Component.literal("  " + displayMaterial(entry.getKey()) + ": " + formatAmount(entry.getValue())));
            }
            return lines;
        }

        private static String formatAmount(int amount) {
            if (amount % FoundryMaterialRegistry.INGOT == 0) {
                int ingots = amount / FoundryMaterialRegistry.INGOT;
                return ingots + (ingots == 1 ? " Ingot" : " Ingots");
            }
            if (amount % FoundryMaterialRegistry.NUGGET == 0) {
                int nuggets = amount / FoundryMaterialRegistry.NUGGET;
                return nuggets + (nuggets == 1 ? " Nugget" : " Nuggets");
            }
            return amount + " q";
        }
    }
}
