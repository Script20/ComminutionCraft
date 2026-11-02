package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * One item in, a stack out. JSON looks like:
 * <pre>
 * {
 *   "type": "comminutioncraft:granulating",
 *   "ingredient": "#c:raw_materials/iron",
 *   "result": { "id": "comminutioncraft:granulated_iron", "count": 2 }
 * }
 * </pre>
 * Processing time and fuel use come from the machine, not the recipe, so every
 * recipe runs at the same flat rate in a given tier.
 */
public class GranulatingRecipe implements Recipe<SingleRecipeInput> {
    public static final MapCodec<GranulatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(GranulatingRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(GranulatingRecipe::getResult)
    ).apply(instance, GranulatingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GranulatingRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    public GranulatingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStackTemplate getResult() {
        return result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<GranulatingRecipe> getSerializer() {
        return ModRecipes.GRANULATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<GranulatingRecipe> getType() {
        return ModRecipes.GRANULATING_TYPE.get();
    }

    // Our machines have no recipe book, so keep these recipes out of it.
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
