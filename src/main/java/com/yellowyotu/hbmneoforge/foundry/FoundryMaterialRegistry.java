package com.yellowyotu.hbmneoforge.foundry;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FoundryMaterialRegistry {
    public static final int QUANTUM = 1;
    public static final int NUGGET = 8;
    public static final int WIRE = 9;
    public static final int BOLT = 9;
    public static final int BILLET = 48;
    public static final int INGOT = 72;
    public static final int PLATE = 72;
    public static final int DENSE_WIRE = 72;
    public static final int CAST_PLATE = 216;
    public static final int SHELL = 288;
    public static final int PIPE = 216;
    public static final int BLOCK = 648;
    public static final int LIGHT_BARREL = 216;
    public static final int HEAVY_BARREL = 432;
    public static final int LIGHT_RECEIVER = 288;
    public static final int HEAVY_RECEIVER = 648;
    public static final int MECHANISM = 288;
    public static final int STOCK = 288;
    public static final int GRIP = 144;

    private static final Set<String> SMALL_MOLDS = Set.of(
            "mold_nugget", "mold_billet", "mold_ingot", "mold_plate", "mold_wire", "mold_blade",
            "mold_blades", "mold_stamp", "mold_shell", "mold_pipe", "mold_c9", "mold_c50",
            "mold_plate_cast", "mold_wire_dense", "mold_barrel_light", "mold_barrel_heavy",
            "mold_receiver_light", "mold_receiver_heavy", "mold_mechanism", "mold_stock", "mold_grip");
    private static final Set<String> LARGE_MOLDS = Set.of(
            "mold_ingots", "mold_plates", "mold_block", "mold_pipes", "mold_plates_cast", "mold_wires_dense");

    private static final Map<String, Integer> COLORS = Map.ofEntries(
            Map.entry("iron", 0xFFA259), Map.entry("gold", 0xE8D754), Map.entry("steel", 0x4A4A4A),
            Map.entry("titanium", 0xA99E79), Map.entry("copper", 0xC18336), Map.entry("tungsten", 0x977474),
            Map.entry("aluminium", 0xD0B8EB), Map.entry("lead", 0x646470), Map.entry("beryllium", 0xAE9572),
            Map.entry("cobalt", 0x8F72AE), Map.entry("boron", 0xAD72AE), Map.entry("uranium", 0x9AA196),
            Map.entry("u233", 0x9AA196), Map.entry("u235", 0x9AA196), Map.entry("u238", 0x9AA196),
            Map.entry("plutonium", 0x78817E), Map.entry("plutonium_239", 0x78817E),
            Map.entry("plutonium_240", 0x78817E), Map.entry("plutonium_241", 0x78817E),
            Map.entry("thorium", 0xBF825F), Map.entry("th232", 0xBF825F), Map.entry("red_copper", 0xE44C0F),
            Map.entry("mingrade", 0xE44C0F), Map.entry("dura_steel", 0x42665C), Map.entry("dura", 0x42665C),
            Map.entry("gunmetal", 0xF9C62C), Map.entry("slag", 0x6C6562), Map.entry("rubber", 0x303030));

    private FoundryMaterialRegistry() {
    }

    public static MaterialAmount fromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return null;
        }
        String path = id.getPath();
        if (path.equals("iron_ingot") || path.equals("gold_ingot") || path.equals("copper_ingot")) {
            return new MaterialAmount(path.substring(0, path.length() - 6), INGOT);
        }
        if (path.equals("iron_nugget") || path.equals("gold_nugget")) {
            return new MaterialAmount(path.substring(0, path.length() - 7), NUGGET);
        }
        if (path.equals("iron_block") || path.equals("gold_block") || path.equals("copper_block")) {
            return new MaterialAmount(path.substring(0, path.length() - 6), BLOCK);
        }
        if (path.equals("redstone")) {
            return new MaterialAmount("redstone", INGOT);
        }
        return prefixed(path);
    }

    private static MaterialAmount prefixed(String path) {
        String[][] prefixes = {
                {"ingot_", String.valueOf(INGOT)}, {"nugget_", String.valueOf(NUGGET)},
                {"billet_", String.valueOf(BILLET)}, {"powder_", String.valueOf(INGOT)},
                {"dust_", String.valueOf(INGOT)}, {"plate_", String.valueOf(PLATE)},
                {"wire_dense_", String.valueOf(DENSE_WIRE)}, {"wire_", String.valueOf(WIRE)},
                {"bolt_", String.valueOf(BOLT)}, {"pipe_", String.valueOf(PIPE)}, {"shell_", String.valueOf(SHELL)}
        };
        if (path.equals("plate_cast")) {
            return new MaterialAmount("iron", CAST_PLATE);
        }
        if (path.startsWith("plate_") && path.endsWith("_cast")) {
            return new MaterialAmount(normalize(path.substring(6, path.length() - 5)), CAST_PLATE);
        }
        if (path.equals("iron_pipe")) {
            return new MaterialAmount("iron", PIPE);
        }
        for (String[] entry : prefixes) {
            if (path.startsWith(entry[0]) && path.length() > entry[0].length()) {
                return new MaterialAmount(normalize(path.substring(entry[0].length())), Integer.parseInt(entry[1]));
            }
        }
        return null;
    }

    public static boolean isMold(ItemStack stack, boolean large) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !id.getNamespace().equals(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID)) {
            return false;
        }
        return (large ? LARGE_MOLDS : SMALL_MOLDS).contains(id.getPath());
    }

    public static int getMoldCost(ItemStack mold, boolean large) {
        if (!isMold(mold, large)) {
            return 0;
        }
        ResourceLocation moldId = BuiltInRegistries.ITEM.getKey(mold.getItem());
        if (moldId == null) {
            return 0;
        }
        return switch (moldId.getPath()) {
            case "mold_nugget" -> NUGGET;
            case "mold_billet" -> BILLET;
            case "mold_ingot" -> INGOT;
            case "mold_plate" -> PLATE;
            case "mold_wire" -> WIRE * 8;
            case "mold_blade" -> INGOT * 3;
            case "mold_blades", "mold_stamp" -> INGOT * 4;
            case "mold_shell" -> SHELL;
            case "mold_pipe" -> PIPE;
            case "mold_c9" -> PLATE / 4;
            case "mold_c50" -> PLATE / 2;
            case "mold_plate_cast" -> CAST_PLATE;
            case "mold_wire_dense" -> DENSE_WIRE;
            case "mold_barrel_light" -> LIGHT_BARREL;
            case "mold_barrel_heavy" -> HEAVY_BARREL;
            case "mold_receiver_light" -> LIGHT_RECEIVER;
            case "mold_receiver_heavy" -> HEAVY_RECEIVER;
            case "mold_mechanism" -> MECHANISM;
            case "mold_stock" -> STOCK;
            case "mold_grip" -> GRIP;
            case "mold_ingots" -> INGOT * 9;
            case "mold_plates" -> PLATE * 9;
            case "mold_block" -> BLOCK;
            case "mold_pipes" -> BLOCK * 3;
            case "mold_plates_cast" -> CAST_PLATE * 3;
            case "mold_wires_dense" -> DENSE_WIRE * 9;
            default -> 0;
        };
    }

    public static MoldResult getMoldResult(ItemStack mold, String material) {
        if (mold.isEmpty() || material == null || material.isBlank()) {
            return null;
        }
        ResourceLocation moldId = BuiltInRegistries.ITEM.getKey(mold.getItem());
        if (moldId == null) {
            return null;
        }
        String mat = normalize(material);
        return switch (moldId.getPath()) {
            case "mold_nugget" -> shape(mat, NUGGET, 1, "nugget");
            case "mold_billet" -> shape(mat, BILLET, 1, "billet");
            case "mold_ingot" -> shape(mat, INGOT, 1, "ingot");
            case "mold_plate" -> shape(mat, PLATE, 1, "plate");
            case "mold_wire" -> shape(mat, WIRE * 8, 8, "wire");
            case "mold_blade" -> multi(mat, INGOT * 3, Map.of("titanium", "blade_titanium", "tungsten", "blade_tungsten"));
            case "mold_blades" -> multi(mat, INGOT * 4, Map.of("steel", "blades_steel", "titanium", "blades_titanium"));
            case "mold_stamp" -> multi(mat, INGOT * 4, Map.of(
                    "stone", "stamp_stone_flat", "iron", "stamp_iron_flat", "steel", "stamp_steel_flat",
                    "titanium", "stamp_titanium_flat", "obsidian", "stamp_obsidian_flat"));
            case "mold_shell" -> shape(mat, SHELL, 1, "shell");
            case "mold_pipe" -> shape(mat, PIPE, 1, "pipe");
            case "mold_ingots" -> shape(mat, INGOT * 9, 9, "ingot");
            case "mold_plates" -> shape(mat, PLATE * 9, 9, "plate");
            case "mold_block" -> shape(mat, BLOCK, 1, "block");
            case "mold_pipes" -> mat.equals("steel") ? direct(mat, BLOCK * 3, "pipes_steel", 1) : null;
            case "mold_plates_cast" -> shape(mat, CAST_PLATE * 3, 3, "cast_plate");
            case "mold_c9" -> multi(mat, PLATE / 4, Map.of("gunmetal", "casing_small", "weaponsteel", "casing_small_steel"));
            case "mold_c50" -> multi(mat, PLATE / 2, Map.of("gunmetal", "casing_large", "weaponsteel", "casing_large_steel"));
            case "mold_plate_cast" -> shape(mat, CAST_PLATE, 1, "cast_plate");
            case "mold_wire_dense" -> shape(mat, DENSE_WIRE, 1, "dense_wire");
            case "mold_wires_dense" -> shape(mat, DENSE_WIRE * 9, 9, "dense_wire");
            case "mold_barrel_light" -> shape(mat, LIGHT_BARREL, 1, "barrel_light");
            case "mold_barrel_heavy" -> shape(mat, HEAVY_BARREL, 1, "barrel_heavy");
            case "mold_receiver_light" -> shape(mat, LIGHT_RECEIVER, 1, "receiver_light");
            case "mold_receiver_heavy" -> shape(mat, HEAVY_RECEIVER, 1, "receiver_heavy");
            case "mold_mechanism" -> shape(mat, MECHANISM, 1, "mechanism");
            case "mold_stock" -> shape(mat, STOCK, 1, "stock");
            case "mold_grip" -> shape(mat, GRIP, 1, "grip");
            default -> null;
        };
    }

    private static MoldResult shape(String material, int cost, int count, String shape) {
        String path = switch (shape) {
            case "ingot" -> vanillaOrMod(material, "ingot");
            case "nugget" -> vanillaOrMod(material, "nugget");
            case "block" -> vanillaOrMod(material, "block");
            case "plate" -> "plate_" + itemMaterialName(material);
            case "cast_plate" -> material.equals("iron") ? "plate_cast" : "plate_" + itemMaterialName(material) + "_cast";
            case "wire" -> "wire_" + itemMaterialName(material);
            case "dense_wire" -> "wire_dense_" + itemMaterialName(material);
            case "pipe" -> material.equals("iron") ? "iron_pipe" : "pipe_" + itemMaterialName(material);
            case "shell" -> "shell_" + itemMaterialName(material);
            case "barrel_light" -> "barrel_light_" + itemMaterialName(material);
            case "barrel_heavy" -> "barrel_heavy_" + itemMaterialName(material);
            case "receiver_light" -> "receiver_light_" + itemMaterialName(material);
            case "receiver_heavy" -> "receiver_heavy_" + itemMaterialName(material);
            case "mechanism" -> "mechanism_" + itemMaterialName(material);
            case "stock" -> "stock_" + itemMaterialName(material);
            case "grip" -> "grip_" + itemMaterialName(material);
            default -> "";
        };
        return direct(material, cost, path, count);
    }

    private static MoldResult multi(String material, int cost, Map<String, String> outputs) {
        String path = outputs.get(material);
        return path == null ? null : direct(material, cost, path, 1);
    }

    private static MoldResult direct(String material, int cost, String path, int count) {
        Item item = findItem(path);
        return item == null ? null : new MoldResult(material, cost, new ItemStack(item, count));
    }

    private static Item findItem(String path) {
        ResourceLocation mod = ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, path);
        Item item = BuiltInRegistries.ITEM.get(mod);
        if (item != null && item != Items.AIR) {
            return item;
        }
        ResourceLocation vanilla = ResourceLocation.withDefaultNamespace(path);
        item = BuiltInRegistries.ITEM.get(vanilla);
        return item == null || item == Items.AIR ? null : item;
    }

    private static String vanillaOrMod(String material, String shape) {
        if (material.equals("iron") || material.equals("gold") || material.equals("copper")) {
            return material + "_" + shape;
        }
        String itemMaterial = itemMaterialName(material);
        return shape + "_" + itemMaterial;
    }

    private static String itemMaterialName(String material) {
        return switch (normalize(material)) {
            case "mingrade" -> "red_copper";
            case "dura" -> "dura_steel";
            default -> normalize(material);
        };
    }

    public static int color(String material) {
        return COLORS.getOrDefault(normalize(material), 0xFF4A00);
    }

    public static String normalize(String material) {
        return material == null ? "" : material.toLowerCase(Locale.ROOT);
    }

    public record MaterialAmount(String material, int amount) {
    }

    public record MoldResult(String material, int cost, ItemStack output) {
    }
}
