package com.script.comminutioncraft;

import com.script.comminutioncraft.client.gui.CoalGranulatorScreen;
import com.script.comminutioncraft.client.ClientRecipes;
import net.neoforged.neoforge.common.NeoForge;
import com.script.comminutioncraft.client.gui.CoalSmelterScreen;
import com.script.comminutioncraft.client.gui.ElectricGeneratorScreen;
import com.script.comminutioncraft.client.gui.ElectricMachineScreen;
import com.script.comminutioncraft.common.data.ModDataGenerators;
import com.script.comminutioncraft.common.registry.ModMenus;
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
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COAL_GRANULATOR.get(), CoalGranulatorScreen::new);
        event.register(ModMenus.COAL_SMELTER.get(), CoalSmelterScreen::new);
        event.register(ModMenus.ELECTRIC_GRANULATOR.get(), ElectricMachineScreen::granulator);
        event.register(ModMenus.ELECTRIC_SMELTER.get(), ElectricMachineScreen::smelter);
        event.register(ModMenus.ELECTRIC_GENERATOR.get(), ElectricGeneratorScreen::new);
    }
}
