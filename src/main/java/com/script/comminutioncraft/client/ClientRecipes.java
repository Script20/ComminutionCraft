package com.script.comminutioncraft.client;

import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
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

    private ClientRecipes() {}
}
