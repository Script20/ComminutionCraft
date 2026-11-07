package com.script.comminutioncraft.common.recipe;

import com.script.comminutioncraft.common.registry.ModRecipes;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * Since 1.21.2 clients don't get recipes by default. Recipe viewers (JEI) need ours, so ask NeoForge to send
 * our machine recipe types along with whatever else gets synced; the client keeps them in {@code ClientRecipes}.
 */
public final class RecipeSync {
    public static void sendToClients(OnDatapackSyncEvent event) {
        event.sendRecipes(ModRecipes.GRANULATING_TYPE.get(), ModRecipes.ASSEMBLING_TYPE.get(), ModRecipes.WASHING_TYPE.get(),
                ModRecipes.PRESSING_TYPE.get(), ModRecipes.PURIFYING_TYPE.get(), ModRecipes.FLOTATION_TYPE.get(),
                ModRecipes.SINGULARITY_TYPE.get());
    }

    private RecipeSync() {}
}
