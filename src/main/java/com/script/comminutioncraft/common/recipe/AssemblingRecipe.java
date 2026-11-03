package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Circuit Assembler: one item in (e.g. an Electronic Circuit), a stack out; the plasma and FE come from the machine. */
public class AssemblingRecipe extends ItemProcessingRecipe {
    public static final MapCodec<AssemblingRecipe> CODEC = codec(AssemblingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblingRecipe> STREAM_CODEC = streamCodec(CODEC);

    public AssemblingRecipe(Ingredient ingredient, ItemStackTemplate result) {
        super(ingredient, result);
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
