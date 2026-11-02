package com.script.comminutioncraft;

import com.script.comminutioncraft.client.CoalGranulatorScreen;
import com.script.comminutioncraft.client.CoalSmelterScreen;
import com.script.comminutioncraft.data.ModDataGenerators;
import com.script.comminutioncraft.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only entrypoint. Only loaded on the physical client (and for the data run). */
@Mod(value = ComminutionCraft.MODID, dist = Dist.CLIENT)
public class ComminutionCraftClient {
    public ComminutionCraftClient(IEventBus modEventBus) {
        modEventBus.addListener(ComminutionCraftClient::registerScreens);
        modEventBus.addListener(ModDataGenerators::gatherData);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COAL_GRANULATOR.get(), CoalGranulatorScreen::new);
        event.register(ModMenus.COAL_SMELTER.get(), CoalSmelterScreen::new);
    }
}
