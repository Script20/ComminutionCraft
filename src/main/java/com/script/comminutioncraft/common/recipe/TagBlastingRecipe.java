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
import net.minecraft.world.item.crafting.BlastingRecipe;

/** Blast furnace twin of {@link TagSmeltingRecipe} (type minecraft:blasting, JSON type comminutioncraft:tag_blasting). */
public class TagBlastingRecipe extends BlastingRecipe {
    public static final MapCodec<TagBlastingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CookingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleItemRecipe::input),
            TagResult.CODEC.fieldOf("result").forGetter(TagBlastingRecipe::tagResult),
            Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(TagBlastingRecipe::experience),
            Codec.INT.fieldOf("cookingtime").orElse(100).forGetter(TagBlastingRecipe::cookingTime)
    ).apply(i, TagBlastingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagBlastingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CookingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            Ingredient.CONTENTS_STREAM_CODEC, SingleItemRecipe::input,
            TagResult.STREAM_CODEC, TagBlastingRecipe::tagResult,
            ByteBufCodecs.FLOAT, TagBlastingRecipe::experience,
            ByteBufCodecs.INT, TagBlastingRecipe::cookingTime,
            TagBlastingRecipe::new);

    private final TagResult tagResult;

    public TagBlastingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient,
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

    // Vanilla types this as RecipeSerializer<BlastingRecipe>; ours only ever encodes this class, so the cast is safe.
    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        return (RecipeSerializer<BlastingRecipe>) (RecipeSerializer<?>) ModRecipes.TAG_BLASTING_SERIALIZER.get();
    }
}
