package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public final class MixerRecipes {
    public static final List<Recipe> RECIPES;
    private static final Map<NTMFluidType, List<Recipe>> BY_OUTPUT;

    static {
        List<Recipe> recipes = new ArrayList<>();

        add(recipes, recipe(NTMFluidType.COOLANT, 2_000, 50).input1(NTMFluidType.WATER, 1_800).solid(ModItems.NITER.get(), 1));
        add(recipes, recipe(NTMFluidType.CRYOGEL, 2_000, 50).input1(NTMFluidType.COOLANT, 1_800).solid(ModItems.POWDER_ICE.get(), 1));
        add(recipes, recipe(NTMFluidType.FRACKSOL, 1_000, 20).input1(NTMFluidType.SULFURIC_ACID, 900).input2(NTMFluidType.PETROLEUM, 100));
        add(recipes, recipe(NTMFluidType.FRACKSOL, 1_000, 20).input1(NTMFluidType.WATER, 1_000).input2(NTMFluidType.PETROLEUM, 100).solid(ModItems.SULFUR.get(), 1));
        add(recipes, recipe(NTMFluidType.ENDERJUICE, 100, 100).input1(NTMFluidType.XPJUICE, 500).solid(Items.DIAMOND, 1));
        add(recipes, recipe(NTMFluidType.SALIENT, 1_000, 20).input1(NTMFluidType.SEEDSLURRY, 500).input2(NTMFluidType.BLOOD, 500));
        add(recipes, recipe(NTMFluidType.COLLOID, 500, 20).input1(NTMFluidType.WATER, 500).solid(ModItems.DUST.get(), 1));
        add(recipes, recipe(NTMFluidType.PHOSGENE, 1_000, 20).input1(NTMFluidType.UNSATURATEDS, 500).input2(NTMFluidType.CHLORINE, 500));
        add(recipes, recipe(NTMFluidType.MUSTARDGAS, 1_000, 20).input1(NTMFluidType.REFORMGAS, 750).input2(NTMFluidType.CHLORINE, 250).solid(ModItems.SULFUR.get(), 1));
        add(recipes, recipe(NTMFluidType.EGG, 1_000, 50).input1(NTMFluidType.RADIOSOLVENT, 500).solid(Items.EGG, 1));
        add(recipes, recipe(NTMFluidType.FISHOIL, 100, 50).solid(Ingredient.of(ItemTags.FISHES), 1, new ItemStack(Items.COD)));
        add(recipes, recipe(NTMFluidType.SUNFLOWEROIL, 100, 50).solid(Blocks.SUNFLOWER.asItem(), 1));

        add(recipes, recipe(NTMFluidType.SOLVENT, 1_000, 50).input1(NTMFluidType.NAPHTHA, 500).input2(NTMFluidType.AROMATICS, 500));
        add(recipes, recipe(NTMFluidType.SOLVENT, 1_000, 50).input1(NTMFluidType.NAPHTHA_CRACK, 500).input2(NTMFluidType.AROMATICS, 500));
        add(recipes, recipe(NTMFluidType.SOLVENT, 1_000, 50).input1(NTMFluidType.NAPHTHA_DS, 500).input2(NTMFluidType.AROMATICS, 500));
        add(recipes, recipe(NTMFluidType.SOLVENT, 1_000, 50).input1(NTMFluidType.NAPHTHA_COKER, 500).input2(NTMFluidType.AROMATICS, 500));
        add(recipes, recipe(NTMFluidType.SULFURIC_ACID, 500, 50).input1(NTMFluidType.PEROXIDE, 800).solid(ModItems.SULFUR.get(), 1));
        add(recipes, recipe(NTMFluidType.NITRIC_ACID, 1_000, 50).input1(NTMFluidType.SULFURIC_ACID, 500).solid(ModItems.NITER.get(), 1));
        add(recipes, recipe(NTMFluidType.RADIOSOLVENT, 1_000, 50).input1(NTMFluidType.REFORMGAS, 750).input2(NTMFluidType.CHLORINE, 250));

        add(recipes, recipe(NTMFluidType.PETROIL, 1_000, 30).input1(NTMFluidType.RECLAIMED, 800).input2(NTMFluidType.LUBRICANT, 200));
        add(recipes, recipe(NTMFluidType.LUBRICANT, 1_000, 20).input1(NTMFluidType.HEATINGOIL, 500).input2(NTMFluidType.UNSATURATEDS, 500));
        add(recipes, recipe(NTMFluidType.LUBRICANT, 1_000, 20).input1(NTMFluidType.FISHOIL, 800).input2(NTMFluidType.ETHANOL, 200));
        add(recipes, recipe(NTMFluidType.LUBRICANT, 1_000, 20).input1(NTMFluidType.SUNFLOWEROIL, 800).input2(NTMFluidType.ETHANOL, 200));
        add(recipes, recipe(NTMFluidType.BIOFUEL, 250, 20).input1(NTMFluidType.FISHOIL, 500).input2(NTMFluidType.WOODOIL, 500));
        add(recipes, recipe(NTMFluidType.BIOFUEL, 200, 20).input1(NTMFluidType.SUNFLOWEROIL, 500).input2(NTMFluidType.WOODOIL, 500));
        add(recipes, recipe(NTMFluidType.NITROGLYCERIN, 1_000, 20).input1(NTMFluidType.PETROLEUM, 1_000).input2(NTMFluidType.NITRIC_ACID, 1_000));
        add(recipes, recipe(NTMFluidType.NITROGLYCERIN, 1_000, 20).input1(NTMFluidType.FISHOIL, 500).input2(NTMFluidType.NITRIC_ACID, 500));

        add(recipes, recipe(NTMFluidType.THORIUM_SALT, 1_000, 30).input1(NTMFluidType.CHLORINE, 1_000).solid(ModItems.POWDER_THORIUM.get(), 1));
        add(recipes, recipe(NTMFluidType.SYNGAS, 1_000, 50).input1(NTMFluidType.COALOIL, 500).input2(NTMFluidType.STEAM, 500));
        add(recipes, recipe(NTMFluidType.OXYHYDROGEN, 1_000, 50).input1(NTMFluidType.HYDROGEN, 500).input2(NTMFluidType.AIR, 2_000));
        add(recipes, recipe(NTMFluidType.OXYHYDROGEN, 1_000, 50).input1(NTMFluidType.HYDROGEN, 500).input2(NTMFluidType.OXYGEN, 500));

        add(recipes, recipe(NTMFluidType.DIESEL_REFORM, 1_000, 50).input1(NTMFluidType.DIESEL, 900).input2(NTMFluidType.REFORMATE, 100));
        add(recipes, recipe(NTMFluidType.DIESEL_CRACK_REFORM, 1_000, 50).input1(NTMFluidType.DIESEL_CRACK, 900).input2(NTMFluidType.REFORMATE, 100));
        add(recipes, recipe(NTMFluidType.KEROSENE_REFORM, 1_000, 50).input1(NTMFluidType.KEROSENE, 900).input2(NTMFluidType.REFORMATE, 100));
        add(recipes, recipe(NTMFluidType.PERFLUOROMETHYL, 1_000, 20).input1(NTMFluidType.PETROLEUM, 1_000).input2(NTMFluidType.UNSATURATEDS, 500).solid(ModItems.FLUORITE.get(), 1));

        RECIPES = Collections.unmodifiableList(recipes);
        EnumMap<NTMFluidType, List<Recipe>> byOutput = new EnumMap<>(NTMFluidType.class);
        for (Recipe recipe : RECIPES) {
            byOutput.computeIfAbsent(recipe.outputType(), ignored -> new ArrayList<>()).add(recipe);
        }
        byOutput.replaceAll((type, list) -> Collections.unmodifiableList(list));
        BY_OUTPUT = Collections.unmodifiableMap(byOutput);
    }

    private MixerRecipes() {
    }

    private static void add(List<Recipe> recipes, RecipeBuilder builder) {
        recipes.add(builder.build());
    }


    public static boolean isValidSolidInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream().map(Recipe::solidInput).filter(java.util.Objects::nonNull).anyMatch(input -> input.ingredient().test(stack));
    }

    public static List<Recipe> getRecipes(NTMFluidType output) {
        return output == null ? List.of() : BY_OUTPUT.getOrDefault(output, List.of());
    }

    private static RecipeBuilder recipe(NTMFluidType output, int outputAmount, int processTime) {
        return new RecipeBuilder(output, outputAmount, processTime);
    }

    public record FluidIngredient(NTMFluidType type, int amount) {
    }

    public record SolidIngredient(Ingredient ingredient, int count, ItemStack displayStack) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && stack.getCount() >= count && ingredient.test(stack);
        }
    }

    public record Recipe(NTMFluidType outputType, int outputAmount, int processTime, FluidIngredient input1, FluidIngredient input2, SolidIngredient solidInput) {
    }

    private static final class RecipeBuilder {
        private final NTMFluidType outputType;
        private final int outputAmount;
        private final int processTime;
        private FluidIngredient input1;
        private FluidIngredient input2;
        private SolidIngredient solidInput;

        private RecipeBuilder(NTMFluidType outputType, int outputAmount, int processTime) {
            this.outputType = outputType;
            this.outputAmount = outputAmount;
            this.processTime = processTime;
        }

        private RecipeBuilder input1(NTMFluidType type, int amount) {
            input1 = new FluidIngredient(type, amount);
            return this;
        }

        private RecipeBuilder input2(NTMFluidType type, int amount) {
            input2 = new FluidIngredient(type, amount);
            return this;
        }

        private RecipeBuilder solid(net.minecraft.world.level.ItemLike item, int count) {
            return solid(Ingredient.of(item), count, new ItemStack(item, count));
        }

        private RecipeBuilder solid(Ingredient ingredient, int count, ItemStack displayStack) {
            solidInput = new SolidIngredient(ingredient, count, displayStack);
            return this;
        }

        private Recipe build() {
            return new Recipe(outputType, outputAmount, processTime, input1, input2, solidInput);
        }
    }
}
