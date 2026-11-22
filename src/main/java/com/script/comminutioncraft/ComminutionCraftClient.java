package com.script.comminutioncraft;

import com.script.comminutioncraft.client.ClientConfig;
import com.script.comminutioncraft.client.ClientRecipes;
import com.script.comminutioncraft.client.ModKeyMappings;
import com.script.comminutioncraft.client.ModTooltips;
import com.script.comminutioncraft.client.gui.ConfigSectionScreen;
import com.script.comminutioncraft.client.registry.ClientRegistries;
import com.script.comminutioncraft.client.sound.MachineSounds;
import com.script.comminutioncraft.common.data.ModDataGenerators;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only entrypoint. Only loaded on the physical client (and for the data run). */
@Mod(value = ComminutionCraft.MODID, dist = Dist.CLIENT)
public class ComminutionCraftClient {
    public ComminutionCraftClient(IEventBus modEventBus, ModContainer modContainer) {
        // Screens, renderers, fluid models, key mappings (see ClientRegistries)
        ClientRegistries.register(modEventBus);
        modEventBus.addListener(ModDataGenerators::gatherData);

        // Mods menu > Comminution Craft > Config: NeoForge's generated screen for our config spec
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ConfigurationScreen(container, parent, ConfigSectionScreen::new));
        // per-player settings (machine sound volume)
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Game events
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
        NeoForge.EVENT_BUS.addListener(ModKeyMappings::onClientTick);
        NeoForge.EVENT_BUS.addListener(ModTooltips::onTooltip);
        NeoForge.EVENT_BUS.addListener(MachineSounds::onClientTick); // the machines' working sounds
    }
}
