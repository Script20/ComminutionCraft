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
 * Ore Purifier: Washed ore -> Purified ore, and with {@code chance} also a by-product (the metal real ores of this
 * kind carry, e.g. nickel with iron). JSON:
 * <pre>
 * { "type": "comminutioncraft:purifying", "ingredient": "comminutioncraft:washed_iron",
 *   "result": { "id": "comminutioncraft:purified_iron" },
 *   "byproduct": { "id": "comminutioncraft:granulated_nickel" }, "chance": 0.2 }
 * </pre>
 * The oxygen and FE come from the machine.
 */
public class PurifyingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<PurifyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(PurifyingRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(PurifyingRecipe::getResult),
            ItemStackTemplate.CODEC.fieldOf("byproduct").forGetter(PurifyingRecipe::getByproduct),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 0.2F).forGetter(PurifyingRecipe::getChance)
    ).apply(instance, PurifyingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PurifyingRecipe> STREAM_CODEC = streamCodec(CODEC);

    private final ItemStackTemplate byproduct;
    private final float chance;

    public PurifyingRecipe(Ingredient ingredient, ItemStackTemplate result, ItemStackTemplate byproduct, float chance) {
        super(ingredient, result);
        this.byproduct = byproduct;
        this.chance = chance;
    }

    public ItemStackTemplate getByproduct() {
        return byproduct;
    }

    /** Chance (0-1) per operation of also getting the by-product. */
    public float getChance() {
        return chance;
    }

    @Override
    public RecipeSerializer<PurifyingRecipe> getSerializer() {
        return ModRecipes.PURIFYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PurifyingRecipe> getType() {
        return ModRecipes.PURIFYING_TYPE.get();
    }
}
