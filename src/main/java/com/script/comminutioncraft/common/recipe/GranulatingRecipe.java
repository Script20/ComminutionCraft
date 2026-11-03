package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Granulators: raw ore -> 2 granulated ore. JSON looks like:
 * <pre>
 * {
 *   "type": "comminutioncraft:granulating",
 *   "ingredient": "#c:raw_materials/iron",
 *   "result": { "id": "comminutioncraft:granulated_iron", "count": 2 }
 * }
 * </pre>
 */
public class GranulatingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<GranulatingRecipe> CODEC = codec(GranulatingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GranulatingRecipe> STREAM_CODEC = streamCodec(CODEC);

    public GranulatingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        super(ingredient, result);
    }

    @Override
    public RecipeSerializer<GranulatingRecipe> getSerializer() {
        return ModRecipes.GRANULATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<GranulatingRecipe> getType() {
        return ModRecipes.GRANULATING_TYPE.get();
    }
}
