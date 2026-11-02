package com.script.comminutioncraft.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Registered from the client entrypoint; runs with the "data" run config (./gradlew runData). */
public final class ModDataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        // Worldgen first: it adds our features to the lookup the other providers see.
        event.createDatapackRegistryObjects(ModWorldGen.BUILDER);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLanguageProvider::new);
    }

    private ModDataGenerators() {}
}
