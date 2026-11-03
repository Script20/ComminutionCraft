package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Ore Washer: raw ore or an ore block -> 3 granulated ore; the water, plasma and FE come from the machine. */
public class WashingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<WashingRecipe> CODEC = codec(WashingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, WashingRecipe> STREAM_CODEC = streamCodec(CODEC);

    public WashingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<WashingRecipe> getSerializer() {
        return ModRecipes.WASHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<WashingRecipe> getType() {
        return ModRecipes.WASHING_TYPE.get();
    }
}
