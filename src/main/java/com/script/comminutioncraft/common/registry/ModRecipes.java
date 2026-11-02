package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ComminutionCraft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ComminutionCraft.MODID);

    /** "comminutioncraft:granulating" — used by the Coal Granulator (and future granulators). */
    public static final DeferredHolder<RecipeType<?>, RecipeType<GranulatingRecipe>> GRANULATING_TYPE =
            RECIPE_TYPES.register("granulating", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GranulatingRecipe>> GRANULATING_SERIALIZER =
            RECIPE_SERIALIZERS.register("granulating",
                    () -> new RecipeSerializer<>(GranulatingRecipe.CODEC, GranulatingRecipe.STREAM_CODEC));

    private ModRecipes() {}
}
