package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.item.tool.PoweredTool;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * A shaped recipe that upgrades a drill or chainsaw to the next tier: the result keeps the FE the old tool held
 * (up to its own capacity). Same JSON as {@code minecraft:crafting_shaped}, type {@code comminutioncraft:tool_upgrade}.
 */
public class ToolUpgradeRecipe extends ShapedRecipe {
    public static final MapCodec<ToolUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.upgraded)
    ).apply(i, ToolUpgradeRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, o -> o.pattern,
            ItemStackTemplate.STREAM_CODEC, o -> o.upgraded,
            ToolUpgradeRecipe::new);

    private final ItemStackTemplate upgraded;

    public ToolUpgradeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern,
                             ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.upgraded = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);
        for (int i = 0; i < input.size(); i++) {
            ItemStack old = input.getItem(i);
            if (old.getItem() instanceof PoweredTool && result.getItem() instanceof PoweredTool tool) {
                int energy = Math.min(PoweredTool.energy(old), tool.tier().capacity());
                if (energy > 0) {
                    result.set(ModDataComponents.STORED_ENERGY.get(), energy);
                }
                break;
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) ModRecipes.TOOL_UPGRADE_SERIALIZER.get();
    }
}
