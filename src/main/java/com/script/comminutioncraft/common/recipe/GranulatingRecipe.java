package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Granulators: raw ore -> 2 granulated ore, and Washed ore -> 1 granulated ore. JSON looks like:
 * <pre>
 * {
 *   "type": "comminutioncraft:granulating",
 *   "ingredient": "#c:raw_materials/iron",
 *   "result": { "id": "comminutioncraft:granulated_iron", "count": 2 },
 *   "min_tier": 1
 * }
 * </pre>
 * {@code min_tier} (default 1) is the lowest granulator tier that runs it: 1 = Coal, 2 = Electric, 3 = Plasma.
 * Washed ore recipes are tier 2, so the Coal Granulator can't use them.
 */
public class GranulatingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<GranulatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(GranulatingRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(GranulatingRecipe::getResult),
            Codec.INT.optionalFieldOf("min_tier", 1).forGetter(GranulatingRecipe::getMinTier)
    ).apply(instance, GranulatingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GranulatingRecipe> STREAM_CODEC = streamCodec(CODEC);

    private final int minTier;

    public GranulatingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this(ingredient, result, 1);
    }

    public GranulatingRecipe(Ingredient ingredient, ItemStackTemplate result, int minTier) {
        super(ingredient, result);
        this.minTier = minTier;
    }

    /** The lowest granulator tier that runs this recipe (1 = Coal, 2 = Electric, 3 = Plasma). */
    public int getMinTier() {
        return minTier;
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
