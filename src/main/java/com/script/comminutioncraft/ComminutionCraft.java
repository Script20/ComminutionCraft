package com.script.comminutioncraft;

import com.mojang.logging.LogUtils;
import com.script.comminutioncraft.registry.ModBlockEntities;
import com.script.comminutioncraft.registry.ModBlocks;
import com.script.comminutioncraft.registry.ModCreativeTabs;
import com.script.comminutioncraft.registry.ModItems;
import com.script.comminutioncraft.registry.ModMenus;
import com.script.comminutioncraft.registry.ModRecipes;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.slf4j.Logger;

@Mod(ComminutionCraft.MODID)
public class ComminutionCraft {
    public static final String MODID = "comminutioncraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ComminutionCraft(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
    }

    /**
     * Lets hoppers, pipes and other mods' item transport insert/extract from our machines.
     * Faces work like a vanilla furnace: top = input, sides = fuel, bottom = output.
     */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_GRANULATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_SMELTER.get(), WorldlyContainerWrapper::new);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
