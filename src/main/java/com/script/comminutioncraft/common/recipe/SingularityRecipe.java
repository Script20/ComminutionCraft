package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * Singularity Processor (tier 6, the 5x step): raw ore or an ore block straight to ingots, the result given as an
 * item tag so other mods' metals work ({@code c:ingots/<metal>}, its first item), plus rare by-products, each rolled on
 * its own chance per operation. JSON:
 * <pre>
 * { "type": "comminutioncraft:singularity_processing", "ingredient": "#c:raw_materials/iron",
 *   "result": { "tag": "c:ingots/iron", "count": 5 },
 *   "byproducts": [ { "item": { "id": "minecraft:diamond" }, "chance": 0.03 }, ... ] }
 * </pre>
 * FE and the nearby black hole come from the machine.
 */
public class SingularityRecipe extends ItemProcessingRecipe {
    /** A by-product and its chance (0-1) per operation. */
    public record Byproduct(ItemStackTemplate item, float chance) {
        public static final Codec<Byproduct> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStackTemplate.CODEC.fieldOf("item").forGetter(Byproduct::item),
                Codec.floatRange(0, 1).fieldOf("chance").forGetter(Byproduct::chance)
        ).apply(i, Byproduct::new));
    }

    public static final MapCodec<SingularityRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SingularityRecipe::getIngredient),
            TagResult.CODEC.fieldOf("result").forGetter(SingularityRecipe::getTagResult),
            Byproduct.CODEC.listOf().optionalFieldOf("byproducts", List.of()).forGetter(SingularityRecipe::getByproducts)
    ).apply(instance, SingularityRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SingularityRecipe> STREAM_CODEC = streamCodec(CODEC);

    private final TagResult tagResult;
    private final List<Byproduct> byproducts;

    public SingularityRecipe(Ingredient ingredient, TagResult result, List<Byproduct> byproducts) {
        super(ingredient, TagResult.PLACEHOLDER);
        this.tagResult = result;
        this.byproducts = List.copyOf(byproducts);
    }

    public TagResult getTagResult() {
        return tagResult;
    }

    public List<Byproduct> getByproducts() {
        return byproducts;
    }

    @Override
    public ItemStackTemplate getResult() {
        return tagResult.resolve();
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return tagResult.resolve().create();
    }

    @Override
    public RecipeSerializer<SingularityRecipe> getSerializer() {
        return ModRecipes.SINGULARITY_SERIALIZER.get();
    }

    @Override
    public RecipeType<SingularityRecipe> getType() {
        return ModRecipes.SINGULARITY_TYPE.get();
    }
}
