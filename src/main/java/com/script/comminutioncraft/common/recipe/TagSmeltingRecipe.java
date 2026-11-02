package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * A normal furnace recipe (type minecraft:smelting, so furnaces, our smelters and JEI all use it)
 * whose result is an item tag. JSON type {@code comminutioncraft:tag_smelting}; same fields as vanilla
 * smelting, except {@code "result": {"tag": "c:ingots/tin", "count": 1}}.
 */
public class TagSmeltingRecipe extends SmeltingRecipe {
    public static final MapCodec<TagSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CookingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
            TagResult.CODEC.fieldOf("result").forGetter(TagSmeltingRecipe::tagResult),
            Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(TagSmeltingRecipe::experience),
            Codec.INT.fieldOf("cookingtime").orElse(200).forGetter(TagSmeltingRecipe::cookingTime)
    ).apply(i, TagSmeltingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CookingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, SingleItemRecipe::input,
            TagResult.STREAM_CODEC, TagSmeltingRecipe::tagResult,
            ByteBufCodecs.FLOAT, TagSmeltingRecipe::experience,
            ByteBufCodecs.INT, TagSmeltingRecipe::cookingTime,
            TagSmeltingRecipe::new);

    private final TagResult tagResult;

    public TagSmeltingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient,
                             TagResult result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, TagResult.PLACEHOLDER, experience, cookingTime);
        this.tagResult = result;
    }

    public TagResult tagResult() {
        return tagResult;
    }

    /** Used by vanilla for the recipe book display and by {@link #assemble}. */
    @Override
    protected ItemStackTemplate result() {
        return tagResult.resolve();
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result().create();
    }

    // Vanilla types this as RecipeSerializer<SmeltingRecipe>; ours only ever encodes this class, so the cast is safe.
    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<SmeltingRecipe> getSerializer() {
        return (RecipeSerializer<SmeltingRecipe>) (RecipeSerializer<?>) ModRecipes.TAG_SMELTING_SERIALIZER.get();
    }
}
