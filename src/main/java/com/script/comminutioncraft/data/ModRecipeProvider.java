package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.recipe.GranulatingRecipe;
import com.script.comminutioncraft.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class ModRecipeProvider extends RecipeProvider {
    private static final int SMELTING_TIME = 200;
    private static final int BLASTING_TIME = 100;

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            // raw ore -> 2 granulated ore
            output.accept(key("granulating/raw_" + metal.name()),
                    new GranulatingRecipe(tag(metal.rawTag()), new ItemStackTemplate(metal.granulated(), 2)),
                    null);

            // granulated ore -> ingot (vanilla furnace, Coal Smelter, blast furnace)
            Ingredient granulated = Ingredient.of(metal.granulated());
            ItemStackTemplate ingot = new ItemStackTemplate(metal.ingot().builtInRegistryHolder(), 1);
            output.accept(key("smelting/" + metal.name() + "_ingot_from_granulated"),
                    new SmeltingRecipe(RecipeBuilder.createCraftingCommonInfo(true), cookingInfo(),
                            granulated, ingot, metal.experience(), SMELTING_TIME),
                    null);
            output.accept(key("blasting/" + metal.name() + "_ingot_from_granulated"),
                    new BlastingRecipe(RecipeBuilder.createCraftingCommonInfo(true), cookingInfo(),
                            granulated, ingot, metal.experience(), BLASTING_TIME),
                    null);
        }

        shaped(RecipeCategory.MISC, ModItems.COAL_GRANULATOR.get())
                .pattern("FIF")
                .pattern("CXC")
                .pattern("CCC")
                .define('F', Items.FLINT)
                .define('I', tag(ProcessedMetal.commonItemTag("ingots/iron")))
                .define('C', tag(ProcessedMetal.commonItemTag("cobblestones")))
                .define('X', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output, key("coal_granulator"));

        shaped(RecipeCategory.MISC, ModItems.COAL_SMELTER.get())
                .pattern("BBB")
                .pattern("BXB")
                .pattern("CCC")
                .define('B', Items.BRICK)
                .define('C', tag(ProcessedMetal.commonItemTag("cobblestones")))
                .define('X', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output, key("coal_smelter"));
    }

    private static AbstractCookingRecipe.CookingBookInfo cookingInfo() {
        return new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, "");
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, ComminutionCraft.id(path));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "ComminutionCraft Recipes";
        }
    }
}
