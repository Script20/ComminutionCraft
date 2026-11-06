package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Froth Flotation Cell (tier 5, the 4x step): raw ore or an ore block -> 4 Ore Concentrate; water, oxygen and FE come from the machine. */
public class FlotationRecipe extends ItemProcessingRecipe {
    public static final MapCodec<FlotationRecipe> CODEC = codec(FlotationRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, FlotationRecipe> STREAM_CODEC = streamCodec(CODEC);

    public FlotationRecipe(Ingredient ingredient, ItemStackTemplate result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<FlotationRecipe> getSerializer() {
        return ModRecipes.FLOTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<FlotationRecipe> getType() {
        return ModRecipes.FLOTATION_TYPE.get();
    }
}
