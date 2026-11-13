package com.script.comminutioncraft.common.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Registered from the client entrypoint; runs with the "data" run config (./gradlew runData). */
public final class ModDataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        // Worldgen first: it adds our features to the lookup the other providers see.
        event.createDatapackRegistryObjects(ModWorldGen.BUILDER);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModDamageTypeTagsProvider::new);
        event.createProvider(ModFluidTagsProvider::new);
        event.createProvider(ModDataMapProvider::new);
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModLanguageProvider::new);
        event.createProvider(ModSoundsProvider::new);
        event.createProvider((output, registries) -> new net.minecraft.data.advancements.AdvancementProvider(
                output, registries, java.util.List.of(new ModAdvancements())));
    }

    private ModDataGenerators() {}
}
