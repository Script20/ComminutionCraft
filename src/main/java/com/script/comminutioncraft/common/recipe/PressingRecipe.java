package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Pellet Press: a casing item in (an iron nugget), a fuel pellet out; the deuterium, tritium and FE come from the machine. */
public class PressingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<PressingRecipe> CODEC = codec(PressingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PressingRecipe> STREAM_CODEC = streamCodec(CODEC);

    public PressingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<PressingRecipe> getSerializer() {
        return ModRecipes.PRESSING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PressingRecipe> getType() {
        return ModRecipes.PRESSING_TYPE.get();
    }
}
