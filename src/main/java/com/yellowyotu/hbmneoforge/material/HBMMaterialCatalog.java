package com.yellowyotu.hbmneoforge.material;

import java.util.*;

import static com.yellowyotu.hbmneoforge.material.MaterialKind.*;
import static com.yellowyotu.hbmneoforge.material.MaterialShape.*;

public final class HBMMaterialCatalog {
    private static final LinkedHashMap<String, HBMMaterialDefinition> MATERIALS = new LinkedHashMap<>();
    private static final Set<String> DEDICATED_FRAGMENTS = Set.of(
            "neodymium", "cobalt", "niobium", "cerium", "lanthanium", "actinium", "meteorite", "boron", "coltan");

    static {
        add("wood", "Wood", 0x896727, NON_SMELTABLE, STOCK, GRIP);
        add("ivory", "Ivory", 0xEDEBCA, NON_SMELTABLE, GRIP);
        add("carbon", "Carbon", 0x404040, ADDITIVE, WIRE, BLOCK);
        add("coal", "Coal", 0x404040, NON_SMELTABLE, FRAGMENT);
        add("lignite", "Lignite", 0x472913, NON_SMELTABLE, FRAGMENT);
        add("diamond", "Diamond", 0x8CF4E2, NON_SMELTABLE, FRAGMENT);
        add("iron", "Iron", 0xFFA259, SMELTABLE, FRAGMENT, DUST, PIPE, CAST_PLATE, WELDED_PLATE, BLOCK);
        add("gold", "Gold", 0xE8D754, SMELTABLE, FRAGMENT, WIRE, NUGGET, DUST, DENSE_WIRE, CAST_PLATE, BLOCK);
        add("redstone", "Redstone", 0xFF1000, SMELTABLE, FRAGMENT, INGOT);
        add("bauxite", "Bauxite", 0xE2560F, NON_SMELTABLE, FRAGMENT);
        add("cryolite", "Cryolite", 0x8B701A, NON_SMELTABLE, FRAGMENT);
        add("uranium", "Uranium", 0x9AA196, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("u233", "Uranium-233", 0x9AA196, SMELTABLE, NUGGET, BILLET, DUST, BLOCK);
        add("u235", "Uranium-235", 0x9AA196, SMELTABLE, NUGGET, BILLET, DUST, BLOCK);
        add("u238", "Uranium-238", 0x9AA196, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("thorium", "Thorium-232", 0xBF825F, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("plutonium", "Plutonium", 0x78817E, SMELTABLE, NUGGET, BILLET, DUST, BLOCK);
        add("neptunium", "Neptunium-237", 0x647064, SMELTABLE, NUGGET, BILLET, DUST, BLOCK);
        add("polonium", "Polonium-210", 0x715E4A, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("technetium", "Technetium-99", 0xCADFDF, SMELTABLE, FRAGMENT, NUGGET, BILLET, BLOCK);
        add("radium", "Radium-226", 0xE9FAF6, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("actinium", "Actinium-227", 0x958989, SMELTABLE, NUGGET, BILLET);
        add("schrabidium", "Schrabidium", 0x32FFFF, SMELTABLE, NUGGET, WIRE, BILLET, DUST, DENSE_WIRE, PLATE, CAST_PLATE, BLOCK);
        add("titanium", "Titanium", 0xA99E79, SMELTABLE, FRAGMENT, DUST, PLATE, DENSE_WIRE, CAST_PLATE, WELDED_PLATE, SHELL, BLOCK);
        add("copper", "Copper", 0xC18336, SMELTABLE, FRAGMENT, WIRE, DUST, PLATE, DENSE_WIRE, CAST_PLATE, WELDED_PLATE, SHELL, PIPE, BLOCK);
        add("tungsten", "Tungsten", 0x977474, SMELTABLE, FRAGMENT, WIRE, BOLT, DUST, DENSE_WIRE, CAST_PLATE, WELDED_PLATE, BLOCK);
        add("aluminium", "Aluminium", 0xD0B8EB, SMELTABLE, FRAGMENT, WIRE, DUST, PLATE, CAST_PLATE, WELDED_PLATE, SHELL, PIPE, BLOCK);
        add("lead", "Lead", 0x646470, SMELTABLE, FRAGMENT, NUGGET, WIRE, BOLT, DUST, PLATE, CAST_PLATE, PIPE, BLOCK);
        add("bismuth", "Bismuth", 0xB200FF, SMELTABLE, FRAGMENT, NUGGET, BILLET, DUST, BLOCK);
        add("tantalium", "Tantalium", 0xA89B74, SMELTABLE, FRAGMENT, NUGGET, DUST, BLOCK);
        add("neodymium", "Neodymium", 0x8F8F5F, SMELTABLE, FRAGMENT, NUGGET, TINY_POWDER, INGOT, DUST, DENSE_WIRE, BLOCK);
        add("niobium", "Niobium", 0xD576B1, SMELTABLE, FRAGMENT, NUGGET, TINY_POWDER, DUST, DENSE_WIRE, BLOCK);
        add("beryllium", "Beryllium", 0xAE9572, SMELTABLE, FRAGMENT, NUGGET, DUST, BLOCK);
        add("emerald", "Emerald", 0x17DD62, NON_SMELTABLE, FRAGMENT, DUST, GEM, BLOCK);
        add("cobalt", "Cobalt", 0x8F72AE, SMELTABLE, FRAGMENT, NUGGET, TINY_POWDER, BILLET, DUST, BLOCK);
        add("boron", "Boron", 0xAD72AE, SMELTABLE, FRAGMENT, TINY_POWDER, DUST, BLOCK);
        add("borax", "Borax", 0xFFECC6, SMELTABLE, FRAGMENT, INGOT, DUST);
        add("lanthanium", "Lanthanium", 0xA1B9B9, SMELTABLE, FRAGMENT, BLOCK);
        add("zirconium", "Zirconium", 0xADA688, SMELTABLE, FRAGMENT, NUGGET, WIRE, TINY_POWDER, BILLET, DUST, CAST_PLATE, WELDED_PLATE, BLOCK);
        add("sodium", "Sodium", 0x7E9493, SMELTABLE, FRAGMENT, INGOT, DUST);
        add("strontium", "Strontium", 0xCAC193, SMELTABLE, FRAGMENT, INGOT, DUST);
        add("lithium", "Lithium", 0xD6D6D6, SMELTABLE, FRAGMENT, DUST, BLOCK);
        add("sulfur", "Sulfur", 0xF1DF68, NON_SMELTABLE, FRAGMENT, DUST, BLOCK);
        add("fluorite", "Fluorite", 0xE1DBD4, NON_SMELTABLE, FRAGMENT, DUST, BLOCK);
        add("silicon", "Silicon", 0x878B9E, SMELTABLE, FRAGMENT, NUGGET, BILLET);
        add("steel", "Steel", 0x4A4A4A, SMELTABLE, TINY_POWDER, BOLT, WIRE, DUST, PLATE, CAST_PLATE, WELDED_PLATE, SHELL, PIPE, BLOCK, LIGHT_BARREL, HEAVY_BARREL, LIGHT_RECEIVER, GRIP);
        add("red_copper", "Red Copper", 0xE44C0F, SMELTABLE, WIRE, DUST, DENSE_WIRE, BLOCK);
        add("dura_steel", "Dura Steel", 0x42665C, SMELTABLE, BOLT, DUST, PLATE, CAST_PLATE, PIPE, BLOCK, LIGHT_BARREL, HEAVY_BARREL, LIGHT_RECEIVER, HEAVY_RECEIVER, GRIP);
        add("flux", "Flux", 0xDECCAD, ADDITIVE, DUST);
        add("slag", "Slag", 0x6C6562, SMELTABLE, INGOT, BLOCK);
        add("rubber", "Rubber", 0x4B4A3F, NON_SMELTABLE, PIPE, GRIP);
        // Dedicated CE fragment items which are not material autogen shapes.
        add("cerium", "Cerium", 0xD6D0B8, SMELTABLE, FRAGMENT);
        add("meteorite", "Meteorite", 0x575757, NON_SMELTABLE, FRAGMENT);
        add("coltan", "Coltan", 0x5D4332, NON_SMELTABLE, FRAGMENT);
    }

    private HBMMaterialCatalog() {}

    private static void add(String id, String name, int color, MaterialKind kind, MaterialShape... shapes) {
        MATERIALS.put(id, new HBMMaterialDefinition(id, name, color, kind,
                shapes.length == 0 ? Set.of() : EnumSet.copyOf(List.of(shapes))));
    }

    public static List<HBMMaterialDefinition> all() { return List.copyOf(MATERIALS.values()); }
    public static Optional<HBMMaterialDefinition> get(String id) { return Optional.ofNullable(MATERIALS.get(id)); }
    public static Set<String> dedicatedFragmentMaterials() { return DEDICATED_FRAGMENTS; }
    public static List<MaterialForm> forms() {
        return MATERIALS.values().stream().flatMap(m -> m.shapes().stream()
                .filter(s -> s != FRAGMENT || DEDICATED_FRAGMENTS.contains(m.id()))
                .map(s -> new MaterialForm(m, s))).toList();
    }
}
