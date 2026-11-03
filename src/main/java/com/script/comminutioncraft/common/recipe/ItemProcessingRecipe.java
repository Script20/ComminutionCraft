package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * One item in, a stack out — the shape of every machine recipe here (granulating, assembling, washing). JSON:
 * <pre>
 * { "type": "comminutioncraft:...", "ingredient": "#c:raw_materials/iron", "result": { "id": "...", "count": 2 } }
 * </pre>
 * Time, FE and fluids come from the machine, not the recipe, so every recipe runs at the same flat rate per machine.
 */
public abstract class ItemProcessingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStackTemplate result;

    protected ItemProcessingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    protected static <R extends ItemProcessingRecipe> MapCodec<R> codec(BiFunction<Ingredient, ItemStackTemplate, R> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemProcessingRecipe::getIngredient),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(ItemProcessingRecipe::getResult)
        ).apply(instance, factory));
    }

    protected static <R extends ItemProcessingRecipe> StreamCodec<RegistryFriendlyByteBuf, R> streamCodec(MapCodec<R> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec.codec());
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
