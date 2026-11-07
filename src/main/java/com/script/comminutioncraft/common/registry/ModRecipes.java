package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.recipe.TagBlastingRecipe;
import com.script.comminutioncraft.common.recipe.TagSmeltingRecipe;
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

    /** "comminutioncraft:assembling" — Circuit Assembler. */
    public static final DeferredHolder<RecipeType<?>, RecipeType<AssemblingRecipe>> ASSEMBLING_TYPE =
            RECIPE_TYPES.register("assembling", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AssemblingRecipe>> ASSEMBLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("assembling",
                    () -> new RecipeSerializer<>(AssemblingRecipe.CODEC, AssemblingRecipe.STREAM_CODEC));

    /** "comminutioncraft:washing" — Ore Washer (the 3x path). */
    public static final DeferredHolder<RecipeType<?>, RecipeType<WashingRecipe>> WASHING_TYPE =
            RECIPE_TYPES.register("washing", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WashingRecipe>> WASHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("washing",
                    () -> new RecipeSerializer<>(WashingRecipe.CODEC, WashingRecipe.STREAM_CODEC));

    /** "comminutioncraft:flotation" — Froth Flotation Cell (tier 5, the 4x step). */
    public static final DeferredHolder<RecipeType<?>, RecipeType<com.script.comminutioncraft.common.recipe.FlotationRecipe>> FLOTATION_TYPE =
            RECIPE_TYPES.register("flotation", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.script.comminutioncraft.common.recipe.FlotationRecipe>> FLOTATION_SERIALIZER =
            RECIPE_SERIALIZERS.register("flotation", () -> new RecipeSerializer<>(com.script.comminutioncraft.common.recipe.FlotationRecipe.CODEC,
                    com.script.comminutioncraft.common.recipe.FlotationRecipe.STREAM_CODEC));

    /** "comminutioncraft:singularity_processing" — Singularity Processor (tier 6, the 5x step). */
    public static final DeferredHolder<RecipeType<?>, RecipeType<com.script.comminutioncraft.common.recipe.SingularityRecipe>> SINGULARITY_TYPE =
            RECIPE_TYPES.register("singularity_processing", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<com.script.comminutioncraft.common.recipe.SingularityRecipe>> SINGULARITY_SERIALIZER =
            RECIPE_SERIALIZERS.register("singularity_processing", () -> new RecipeSerializer<>(com.script.comminutioncraft.common.recipe.SingularityRecipe.CODEC,
                    com.script.comminutioncraft.common.recipe.SingularityRecipe.STREAM_CODEC));

    /** "comminutioncraft:pressing" — Pellet Press (tier 4). */
    public static final DeferredHolder<RecipeType<?>, RecipeType<PressingRecipe>> PRESSING_TYPE =
            RECIPE_TYPES.register("pressing", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressingRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing",
                    () -> new RecipeSerializer<>(PressingRecipe.CODEC, PressingRecipe.STREAM_CODEC));

    /** "comminutioncraft:purifying" — Ore Purifier (tier 4): washed -> purified ore, maybe a by-product. */
    public static final DeferredHolder<RecipeType<?>, RecipeType<PurifyingRecipe>> PURIFYING_TYPE =
            RECIPE_TYPES.register("purifying", RecipeType::simple);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PurifyingRecipe>> PURIFYING_SERIALIZER =
            RECIPE_SERIALIZERS.register("purifying",
                    () -> new RecipeSerializer<>(PurifyingRecipe.CODEC, PurifyingRecipe.STREAM_CODEC));

    /** Smelting / blasting whose result is an item tag, for other mods' metals (see TagResult). */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TagSmeltingRecipe>> TAG_SMELTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("tag_smelting",
                    () -> new RecipeSerializer<>(TagSmeltingRecipe.MAP_CODEC, TagSmeltingRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TagBlastingRecipe>> TAG_BLASTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("tag_blasting",
                    () -> new RecipeSerializer<>(TagBlastingRecipe.MAP_CODEC, TagBlastingRecipe.STREAM_CODEC));

    private ModRecipes() {}
}
