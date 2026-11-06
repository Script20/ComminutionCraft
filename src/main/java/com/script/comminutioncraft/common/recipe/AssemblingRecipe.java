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
 * Circuit Assembler: one item in (e.g. an Electronic Circuit), a stack out; the plasma and FE come from the machine.
 * An optional {@code "antimatter": mB} makes the recipe also use that much antimatter from the assembler's antimatter
 * tank (the Antimatter Circuit).
 */
public class AssemblingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<AssemblingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemProcessingRecipe::getIngredient),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ItemProcessingRecipe::getResult),
            Codec.INT.optionalFieldOf("antimatter", 0).forGetter(AssemblingRecipe::getAntimatter)
    ).apply(instance, AssemblingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblingRecipe> STREAM_CODEC = streamCodec(CODEC);

    private final int antimatter;

    public AssemblingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        this(ingredient, result, 0);
    }

    public AssemblingRecipe(Ingredient ingredient, ItemStackTemplate result, int antimatter) {
        super(ingredient, result);
        this.antimatter = antimatter;
    }

    /** mB of antimatter this recipe uses (0 = none). */
    public int getAntimatter() {
        return antimatter;
    }

    @Override
    public RecipeSerializer<AssemblingRecipe> getSerializer() {
        return ModRecipes.ASSEMBLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<AssemblingRecipe> getType() {
        return ModRecipes.ASSEMBLING_TYPE.get();
    }
}
