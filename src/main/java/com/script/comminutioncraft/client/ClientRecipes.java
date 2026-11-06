package com.script.comminutioncraft.client;

import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/** Recipes the server synced to this client (see {@code RecipeSync}); used by the JEI plugin. */
public final class ClientRecipes {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    public static List<RecipeHolder<GranulatingRecipe>> granulating() {
        return List.copyOf(recipes.byType(ModRecipes.GRANULATING_TYPE.get()));
    }

    public static List<RecipeHolder<AssemblingRecipe>> assembling() {
        return List.copyOf(recipes.byType(ModRecipes.ASSEMBLING_TYPE.get()));
    }

    public static List<RecipeHolder<WashingRecipe>> washing() {
        return List.copyOf(recipes.byType(ModRecipes.WASHING_TYPE.get()));
    }

    public static List<RecipeHolder<PressingRecipe>> pressing() {
        return List.copyOf(recipes.byType(ModRecipes.PRESSING_TYPE.get()));
    }

    public static List<RecipeHolder<PurifyingRecipe>> purifying() {
        return List.copyOf(recipes.byType(ModRecipes.PURIFYING_TYPE.get()));
    }

    public static List<RecipeHolder<com.script.comminutioncraft.common.recipe.FlotationRecipe>> flotation() {
        return List.copyOf(recipes.byType(ModRecipes.FLOTATION_TYPE.get()));
    }

    private ClientRecipes() {}
}
