package com.script.comminutioncraft.client.registry;

import com.script.comminutioncraft.client.ModKeyMappings;
import com.script.comminutioncraft.client.render.FennecFoxRenderer;
import com.script.comminutioncraft.client.render.WoodRenderers;
import net.neoforged.bus.api.IEventBus;

/** Everything the client registers on the mod event bus: screens, renderers, fluid models, key mappings. */
public final class ClientRegistries {
    private ClientRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModScreens::register);
        modEventBus.addListener(ModRenderers::registerRenderers);
        modEventBus.addListener(ModRenderers::registerFluidModels);
        modEventBus.addListener(ModRenderers::registerItemRenderers);
        modEventBus.addListener(ModRenderers::registerGuiRenderers);
        modEventBus.addListener(FennecFoxRenderer::registerLayers);
        WoodRenderers.register(modEventBus); // purpleheart boats and signs
        modEventBus.addListener(ModKeyMappings::register);
    }
}
