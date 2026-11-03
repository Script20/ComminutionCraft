package com.script.comminutioncraft;

import com.mojang.logging.LogUtils;
import com.script.comminutioncraft.common.recipe.RecipeSync;
import net.neoforged.neoforge.common.NeoForge;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.CableEnergyHandler;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.gametest.ModGameTests;
import com.script.comminutioncraft.common.registry.ModCreativeTabs;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModFeatures;
import com.script.comminutioncraft.common.registry.ModItems;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
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
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModGameTests.TEST_FUNCTIONS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(ModGameTests::registerTests);

        NeoForge.EVENT_BUS.addListener(RecipeSync::sendToClients);
    }

    /**
     * Items: hoppers, pipes and other mods' item transport. Tier 1 works like a vanilla furnace
     * (top = input, sides = fuel, bottom = output); tier 2 is bottom = output, every other side = input;
     * the generator takes fuel from any side.
     * Energy: tier-2 machines accept FE and the generator provides it on every side; batteries take FE on their
     * input faces and give it on their output faces (none on None faces); cables on every side except ones cut
     * with the wrench.
     */
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_GRANULATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_SMELTER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ELECTRIC_GRANULATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ELECTRIC_SMELTER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ELECTRIC_GENERATOR.get(), WorldlyContainerWrapper::new);

        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ELECTRIC_GRANULATOR.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ELECTRIC_SMELTER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ELECTRIC_GENERATOR.get(), (generator, side) -> generator.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ENERGY_BATTERY.get(), EnergyBatteryBlockEntity::getEnergyHandler);
        event.registerBlock(Capabilities.Energy.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(EnergyCableBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new CableEnergyHandler(level, pos, side),
                ModBlocks.ENERGY_CABLE.get());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
