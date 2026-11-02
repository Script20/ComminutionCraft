package com.script.comminutioncraft.machine;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Recipes a machine finished since a player last emptied its output, turned into XP like a vanilla furnace.
 * Only cooking recipes carry XP; others are still recorded so the player unlocks them.
 */
public final class MachineExperience {
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);
    private static final String KEY = "recipes_used";

    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();

    public void recordRecipe(RecipeHolder<?> recipe) {
        recipesUsed.addTo(recipe.id(), 1);
    }

    /** Called when a player takes from the output slot: unlocks the recipes and drops their XP at the player. */
    public void award(ServerPlayer player, List<ItemStack> machineItems) {
        List<RecipeHolder<?>> recipes = pop(player.level(), player.position());
        player.awardRecipes(recipes);
        for (RecipeHolder<?> recipe : recipes) {
            player.triggerRecipeCrafted(recipe, machineItems);
        }
    }

    /** Drops XP for every recipe finished since the last pop (also used when the machine is broken). */
    public List<RecipeHolder<?>> pop(ServerLevel level, Vec3 position) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry : recipesUsed.reference2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                recipes.add(recipe);
                if (recipe.value() instanceof AbstractCookingRecipe cooking) {
                    awardExperience(level, position, entry.getIntValue(), cooking.experience());
                }
            });
        }
        recipesUsed.clear();
        return recipes;
    }

    private static void awardExperience(ServerLevel level, Vec3 position, int count, float perItem) {
        float total = count * perItem;
        int xp = Mth.floor(total);
        // Fractional XP becomes a chance at one more point, like a vanilla furnace.
        if (level.getRandom().nextFloat() < Mth.frac(total)) {
            xp++;
        }
        ExperienceOrb.award(level, position, xp);
    }

    public void load(ValueInput input) {
        recipesUsed.clear();
        recipesUsed.putAll(input.read(KEY, CODEC).orElse(Map.of()));
    }

    public void save(ValueOutput output) {
        output.store(KEY, CODEC, recipesUsed);
    }

    /** Implemented by machine block entities so {@link MachineOutputSlot} can pay out their XP. */
    public interface Holder {
        void awardUsedRecipesAndPopExperience(ServerPlayer player);
    }
}
