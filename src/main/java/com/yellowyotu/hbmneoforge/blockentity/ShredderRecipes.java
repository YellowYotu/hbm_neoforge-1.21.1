package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class ShredderRecipes {

    public record Recipe(ItemStack input, ItemStack output) {
    }

    private static final List<Recipe> RECIPES = createRecipes();

    private ShredderRecipes() {
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (Recipe recipe : RECIPES) {
            if (input.is(recipe.input().getItem())) {
                return recipe.output().copy();
            }
        }

        if (input.is(ItemTags.LOGS)) {
            return new ItemStack(ModItems.POWDER_SAWDUST.get(), 4);
        }
        if (input.is(ItemTags.PLANKS)) {
            return new ItemStack(ModItems.POWDER_SAWDUST.get());
        }
        if (input.is(ItemTags.SAPLINGS)) {
            return new ItemStack(Items.STICK);
        }
        if (input.is(ItemTags.WOOL)) {
            return new ItemStack(Items.STRING, 4);
        }

        return new ItemStack(ModItems.SCRAP.get());
    }

    public static boolean canShred(ItemStack input) {
        return !input.isEmpty() && !input.is(ModItems.BLADES_STEEL.get()) && !input.is(ModItems.BLADES_TITANIUM.get()) && !input.is(ModItems.BATTERY_PACK.get());
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        addMaterial(recipes, ModItems.INGOT_STEEL.get(), ModItems.POWDER_STEEL.get(), 1);
        addMaterial(recipes, ModItems.PLATE_STEEL.get(), ModItems.POWDER_STEEL.get(), 1);
        addMaterial(recipes, ModItems.INGOT_TITANIUM.get(), ModItems.POWDER_TITANIUM.get(), 1);
        addMaterial(recipes, ModItems.PLATE_TITANIUM.get(), ModItems.POWDER_TITANIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_COPPER.get(), ModItems.POWDER_COPPER.get(), 1);
        addMaterial(recipes, ModItems.PLATE_COPPER.get(), ModItems.POWDER_COPPER.get(), 1);
        addMaterial(recipes, ModItems.PLATE_IRON.get(), ModItems.POWDER_IRON.get(), 1);
        addMaterial(recipes, ModItems.INGOT_TUNGSTEN.get(), ModItems.POWDER_TUNGSTEN.get(), 1);
        addMaterial(recipes, ModItems.INGOT_ALUMINIUM.get(), ModItems.POWDER_ALUMINIUM.get(), 1);
        addMaterial(recipes, ModItems.PLATE_ALUMINIUM.get(), ModItems.POWDER_ALUMINIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_BERYLLIUM.get(), ModItems.POWDER_BERYLLIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_LEAD.get(), ModItems.POWDER_LEAD.get(), 1);
        addMaterial(recipes, ModItems.PLATE_LEAD.get(), ModItems.POWDER_LEAD.get(), 1);
        addMaterial(recipes, ModItems.INGOT_COBALT.get(), ModItems.POWDER_COBALT.get(), 1);
        addMaterial(recipes, ModItems.INGOT_BORON.get(), ModItems.POWDER_BORON.get(), 1);
        addMaterial(recipes, ModItems.INGOT_URANIUM.get(), ModItems.POWDER_URANIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_U233.get(), ModItems.POWDER_URANIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_U235.get(), ModItems.POWDER_URANIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_U238.get(), ModItems.POWDER_URANIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_PLUTONIUM.get(), ModItems.POWDER_PLUTONIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_PLUTONIUM_239.get(), ModItems.POWDER_PLUTONIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_PLUTONIUM_240.get(), ModItems.POWDER_PLUTONIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_PLUTONIUM_241.get(), ModItems.POWDER_PLUTONIUM.get(), 1);
        addMaterial(recipes, ModItems.INGOT_RED_COPPER.get(), ModItems.POWDER_RED_COPPER.get(), 1);
        addMaterial(recipes, ModItems.INGOT_DURA_STEEL.get(), ModItems.POWDER_DURA_STEEL.get(), 1);
        addMaterial(recipes, ModItems.INGOT_GUNMETAL.get(), ModItems.POWDER_GUNMETAL.get(), 1);
        addMaterial(recipes, ModItems.INGOT_GRAPHITE.get(), ModItems.POWDER_COAL.get(), 1);
        addMaterial(recipes, ModItems.INGOT_POLYMER.get(), ModItems.POWDER_POLYMER.get(), 1);
        addMaterial(recipes, ModItems.PLATE_POLYMER.get(), ModItems.POWDER_POLYMER.get(), 1);
        addMaterial(recipes, ModItems.INGOT_BAKELITE.get(), ModItems.POWDER_BAKELITE.get(), 1);

        addMaterial(recipes, ModItems.ORE_URANIUM.get(), ModItems.POWDER_URANIUM.get(), 2);
        addMaterial(recipes, ModItems.ORE_TITANIUM.get(), ModItems.POWDER_TITANIUM.get(), 2);
        addMaterial(recipes, ModItems.ORE_TUNGSTEN.get(), ModItems.POWDER_TUNGSTEN.get(), 2);
        addMaterial(recipes, ModItems.ORE_ALUMINIUM.get(), ModItems.POWDER_ALUMINIUM.get(), 2);
        addMaterial(recipes, ModItems.ORE_BERYLLIUM.get(), ModItems.POWDER_BERYLLIUM.get(), 2);
        addMaterial(recipes, ModItems.ORE_LEAD.get(), ModItems.POWDER_LEAD.get(), 2);
        addMaterial(recipes, ModItems.ORE_COBALT.get(), ModItems.POWDER_COBALT.get(), 2);
        addMaterial(recipes, ModItems.ORE_RARE_EARTH.get(), ModItems.POWDER_DESH_MIX.get(), 1);
        addMaterial(recipes, ModItems.RARE_EARTH_ORE_CHUNK.get(), ModItems.POWDER_DESH_MIX.get(), 1);

        addMaterial(recipes, ModItems.BLOCK_LEAD.get(), ModItems.POWDER_LEAD.get(), 9);
        addMaterial(recipes, ModItems.BLOCK_BORON.get(), ModItems.POWDER_BORON.get(), 9);
        addMaterial(recipes, ModItems.BLOCK_COBALT.get(), ModItems.POWDER_COBALT.get(), 9);
        addMaterial(recipes, ModItems.BLOCK_RED_COPPER.get(), ModItems.POWDER_RED_COPPER.get(), 9);
        addMaterial(recipes, ModItems.FRAGMENT_COBALT.get(), ModItems.POWDER_COBALT_TINY.get(), 1);
        addMaterial(recipes, ModItems.NUGGET_COBALT.get(), ModItems.POWDER_COBALT_TINY.get(), 1);
        addMaterial(recipes, ModItems.COPPER_COIL.get(), ModItems.POWDER_RED_COPPER.get(), 1);
        addMaterial(recipes, ModItems.RING_COIL.get(), ModItems.POWDER_RED_COPPER.get(), 2);

        addMaterial(recipes, Items.IRON_INGOT, ModItems.POWDER_IRON.get(), 1);
        addMaterial(recipes, Items.GOLD_INGOT, ModItems.POWDER_GOLD.get(), 1);
        addMaterial(recipes, Items.COPPER_INGOT, ModItems.POWDER_COPPER.get(), 1);
        addMaterial(recipes, Items.COAL, ModItems.POWDER_COAL.get(), 1);
        addMaterial(recipes, Items.CHARCOAL, ModItems.POWDER_COAL.get(), 1);
        addMaterial(recipes, Items.QUARTZ, ModItems.POWDER_QUARTZ.get(), 1);
        addMaterial(recipes, Blocks.QUARTZ_BLOCK.asItem(), ModItems.POWDER_QUARTZ.get(), 4);
        addMaterial(recipes, Blocks.CHISELED_QUARTZ_BLOCK.asItem(), ModItems.POWDER_QUARTZ.get(), 4);
        addMaterial(recipes, Blocks.QUARTZ_PILLAR.asItem(), ModItems.POWDER_QUARTZ.get(), 4);
        addMaterial(recipes, Blocks.SMOOTH_QUARTZ.asItem(), ModItems.POWDER_QUARTZ.get(), 4);
        addMaterial(recipes, Blocks.QUARTZ_STAIRS.asItem(), ModItems.POWDER_QUARTZ.get(), 3);
        addMaterial(recipes, Blocks.QUARTZ_SLAB.asItem(), ModItems.POWDER_QUARTZ.get(), 2);
        addMaterial(recipes, Blocks.NETHER_QUARTZ_ORE.asItem(), ModItems.POWDER_QUARTZ.get(), 2);
        addMaterial(recipes, Blocks.GLOWSTONE.asItem(), Items.GLOWSTONE_DUST, 4);
        addMaterial(recipes, Blocks.PACKED_ICE.asItem(), ModItems.POWDER_ICE.get(), 1);
        addMaterial(recipes, Blocks.OBSIDIAN.asItem(), ModItems.GRAVEL_OBSIDIAN.get(), 1);
        addMaterial(recipes, Blocks.STONE.asItem(), Blocks.GRAVEL.asItem(), 1);
        addMaterial(recipes, Blocks.COBBLESTONE.asItem(), Blocks.GRAVEL.asItem(), 1);
        addMaterial(recipes, Blocks.STONE_BRICKS.asItem(), Blocks.GRAVEL.asItem(), 1);
        addMaterial(recipes, Blocks.GRAVEL.asItem(), Blocks.SAND.asItem(), 1);
        addMaterial(recipes, Blocks.BRICKS.asItem(), Items.CLAY_BALL, 4);
        addMaterial(recipes, Blocks.BRICK_STAIRS.asItem(), Items.CLAY_BALL, 3);
        addMaterial(recipes, Items.FLOWER_POT, Items.CLAY_BALL, 3);
        addMaterial(recipes, Items.BRICK, Items.CLAY_BALL, 1);
        addMaterial(recipes, Blocks.SANDSTONE.asItem(), Blocks.SAND.asItem(), 4);
        addMaterial(recipes, Blocks.CHISELED_SANDSTONE.asItem(), Blocks.SAND.asItem(), 4);
        addMaterial(recipes, Blocks.CUT_SANDSTONE.asItem(), Blocks.SAND.asItem(), 4);
        addMaterial(recipes, Blocks.SMOOTH_SANDSTONE.asItem(), Blocks.SAND.asItem(), 4);
        addMaterial(recipes, Blocks.SANDSTONE_STAIRS.asItem(), Blocks.SAND.asItem(), 6);
        addMaterial(recipes, Blocks.CLAY.asItem(), Items.CLAY_BALL, 4);
        addMaterial(recipes, Blocks.TERRACOTTA.asItem(), Items.CLAY_BALL, 4);
        addTerracottaRecipes(recipes);
        addMaterial(recipes, Blocks.TNT.asItem(), Items.GUNPOWDER, 5);
        addMaterial(recipes, Blocks.DIRT.asItem(), ModItems.DUST.get(), 1);
        addMaterial(recipes, Blocks.SAND.asItem(), ModItems.DUST.get(), 2);
        addMaterial(recipes, Items.SUGAR_CANE, Items.SUGAR, 3);
        addMaterial(recipes, Items.APPLE, Items.SUGAR, 1);
        addMaterial(recipes, Items.CARROT, Items.SUGAR, 1);
        addMaterial(recipes, Blocks.ANVIL.asItem(), ModItems.POWDER_IRON.get(), 31);
        addMaterial(recipes, Blocks.CHIPPED_ANVIL.asItem(), ModItems.POWDER_IRON.get(), 31);
        addMaterial(recipes, Blocks.DAMAGED_ANVIL.asItem(), ModItems.POWDER_IRON.get(), 31);
        addSkullRecipes(recipes);
        addConcreteRecipes(recipes);

        addMaterial(recipes, ModItems.SCRAP.get(), ModItems.DUST.get(), 1);
        addMaterial(recipes, ModItems.DUST.get(), ModItems.DUST.get(), 1);
        addPowderRecycling(recipes);

        addMaterial(recipes, Items.OAK_LOG, ModItems.POWDER_SAWDUST.get(), 4);
        addMaterial(recipes, Items.OAK_PLANKS, ModItems.POWDER_SAWDUST.get(), 1);
        addMaterial(recipes, Items.OAK_SAPLING, Items.STICK, 1);
        addMaterial(recipes, Items.WHITE_WOOL, Items.STRING, 4);

        return List.copyOf(recipes);
    }

    private static void addConcreteRecipes(List<Recipe> recipes) {
        addMaterials(recipes, Blocks.GRAVEL.asItem(), 1,
                ModItems.CONCRETE.get(), ModItems.CONCRETE_WHITE.get(), ModItems.CONCRETE_ORANGE.get(), ModItems.CONCRETE_MAGENTA.get(), ModItems.CONCRETE_LIGHT_BLUE.get(), ModItems.CONCRETE_YELLOW.get(), ModItems.CONCRETE_LIME.get(), ModItems.CONCRETE_PINK.get(), ModItems.CONCRETE_GRAY.get(), ModItems.CONCRETE_SILVER.get(), ModItems.CONCRETE_CYAN.get(), ModItems.CONCRETE_PURPLE.get(), ModItems.CONCRETE_BLUE.get(), ModItems.CONCRETE_BROWN.get(), ModItems.CONCRETE_GREEN.get(), ModItems.CONCRETE_RED.get(), ModItems.CONCRETE_BLACK.get(), ModItems.CONCRETE_REBAR.get(), ModItems.CONCRETE_ASBESTOS.get(), ModItems.CONCRETE_SUPER.get(), ModItems.CONCRETE_TILE.get(), ModItems.CONCRETE_COLORED_EXT_HAZARD.get(), ModItems.BRICK_CONCRETE.get(), ModItems.BRICK_CONCRETE_CRACKED.get(), ModItems.BRICK_CONCRETE_MOSSY.get(), ModItems.BRICK_CONCRETE_BROKEN.get(), ModItems.BRICK_CONCRETE_MARKED.get());
    }

    private static void addTerracottaRecipes(List<Recipe> recipes) {
        addMaterials(recipes, Items.CLAY_BALL, 4, Blocks.WHITE_TERRACOTTA.asItem(), Blocks.ORANGE_TERRACOTTA.asItem(), Blocks.MAGENTA_TERRACOTTA.asItem(), Blocks.LIGHT_BLUE_TERRACOTTA.asItem(), Blocks.YELLOW_TERRACOTTA.asItem(), Blocks.LIME_TERRACOTTA.asItem(), Blocks.PINK_TERRACOTTA.asItem(), Blocks.GRAY_TERRACOTTA.asItem(), Blocks.LIGHT_GRAY_TERRACOTTA.asItem(), Blocks.CYAN_TERRACOTTA.asItem(), Blocks.PURPLE_TERRACOTTA.asItem(), Blocks.BLUE_TERRACOTTA.asItem(), Blocks.BROWN_TERRACOTTA.asItem(), Blocks.GREEN_TERRACOTTA.asItem(), Blocks.RED_TERRACOTTA.asItem(), Blocks.BLACK_TERRACOTTA.asItem());
    }

    private static void addSkullRecipes(List<Recipe> recipes) {
        addMaterials(recipes, ModItems.BIOMASS.get(), 4, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.PIGLIN_HEAD);
    }

    private static void addPowderRecycling(List<Recipe> recipes) {
        addMaterials(recipes, ModItems.DUST.get(), 1, ModItems.POWDER_IRON.get(), ModItems.POWDER_STEEL.get(), ModItems.POWDER_COPPER.get(), ModItems.POWDER_TITANIUM.get(), ModItems.POWDER_TUNGSTEN.get(), ModItems.POWDER_ALUMINIUM.get(), ModItems.POWDER_BERYLLIUM.get(), ModItems.POWDER_LEAD.get(), ModItems.POWDER_COBALT.get(), ModItems.POWDER_BORON.get(), ModItems.POWDER_URANIUM.get(), ModItems.POWDER_PLUTONIUM.get(), ModItems.POWDER_RED_COPPER.get(), ModItems.POWDER_DURA_STEEL.get(), ModItems.POWDER_GUNMETAL.get(), ModItems.POWDER_COAL.get(), ModItems.POWDER_GOLD.get(), ModItems.POWDER_POLYMER.get(), ModItems.POWDER_BAKELITE.get(), ModItems.POWDER_QUARTZ.get(), ModItems.POWDER_ICE.get(), ModItems.POWDER_SAWDUST.get(), ModItems.POWDER_DESH_MIX.get(), ModItems.POWDER_COBALT_TINY.get());
    }

    private static void addMaterials(List<Recipe> recipes, Item output, int count, Item... inputs) {
        for (Item input : inputs) {
            addMaterial(recipes, input, output, count);
        }
    }

    private static void addMaterial(List<Recipe> recipes, Item input, Item output, int count) {
        recipes.add(new Recipe(new ItemStack(input), new ItemStack(output, count)));
    }
}
