package com.script.comminutioncraft;

import com.script.comminutioncraft.common.compat.patchouli.GuideBook;
import com.script.comminutioncraft.common.config.ComminutionConfig;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.item.tool.AreaBreaker;
import com.script.comminutioncraft.common.item.tool.OreHammer;
import com.script.comminutioncraft.common.recipe.RecipeSync;
import com.script.comminutioncraft.common.registry.ModRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ComminutionCraft.MODID)
public class ComminutionCraft {
    public static final String MODID = "comminutioncraft";

    public ComminutionCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Blocks, items, block entities, capabilities... (see ModRegistries / ModCapabilities)
        ModRegistries.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ComminutionConfig.SPEC);
        // Balance numbers: per world, synced to players (see MachineConfig)
        modContainer.registerConfig(ModConfig.Type.SERVER, MachineConfig.SPEC);

        // Game events
        NeoForge.EVENT_BUS.addListener(GuideBook::onPlayerLoggedIn); // the guide book on first join
        NeoForge.EVENT_BUS.addListener(RecipeSync::sendToClients);
        NeoForge.EVENT_BUS.addListener(OreHammer::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(AreaBreaker::onBlockDrops);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
