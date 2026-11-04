package com.script.comminutioncraft;

import com.mojang.logging.LogUtils;
import com.script.comminutioncraft.common.recipe.RecipeSync;
import net.neoforged.neoforge.common.NeoForge;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.CableEnergyHandler;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.machine.ReactorPortBlockEntity;
import com.script.comminutioncraft.common.machine.OrePurifierBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaMachineBlockEntity;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.cable.PipeFluidHandler;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.FluidMachineBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import com.script.comminutioncraft.gametest.ModGameTests;
import com.script.comminutioncraft.common.registry.ModCreativeTabs;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModFeatures;
import com.script.comminutioncraft.common.registry.ModFluids;
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
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
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
        modEventBus.addListener(SwitchWrenchModePayload::register);

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

        // Tier 3 power chain: water generator (filters in, water out), electrolyser / plasma generator (FE in, fluids
        // in and out by face), MHD generator (plasma in, FE out); then tank and pipes
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get(),
                AtmosphericWaterGeneratorBlockEntity::getFluidHandler);
        for (var type : java.util.List.of(ModBlockEntities.ELECTROLYSER.get(), ModBlockEntities.PLASMA_GENERATOR.get(),
                ModBlockEntities.MHD_GENERATOR.get(), ModBlockEntities.ISOTOPE_SEPARATOR.get(), ModBlockEntities.TRITIUM_BREEDER.get())) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (machine, side) -> machine.getEnergyHandler());
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, FluidMachineBlockEntity::getFluidHandler);
        }
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_TANK.get(), FluidTankBlockEntity::getFluidHandler);
        event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(ConduitBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new PipeFluidHandler(level, pos, side, (FluidPipeBlock) state.getBlock()),
                ModBlocks.FLUID_PIPE.get(), ModBlocks.PLASMA_CABLE.get());
        event.registerBlock(Capabilities.Energy.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(EnergyCableBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new CableEnergyHandler(level, pos, side, ((EnergyCableBlock) state.getBlock()).getTransferRate()),
                ModBlocks.ENERGY_CABLE.get(), ModBlocks.FUSION_CABLE.get());

        // Tier 4 Fusion Reactor: everything goes through its ports; the controller itself only answers "no side"
        // queries (Jade) and takes pellets from hoppers.
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.FUSION_REACTOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FUSION_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FUSION_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getEnergy() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FUSION_ENERGY_BATTERY.get(), EnergyBatteryBlockEntity::getEnergyHandler);
        // Ore Purifier: items and FE like any machine, oxygen in through Input faces
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ORE_PURIFIER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ORE_PURIFIER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ORE_PURIFIER.get(), OrePurifierBlockEntity::getFluidHandler);

        // Tier 3 machines: FE and items like tier 2 (the granulator and smelter need nothing else)
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PLASMA_GRANULATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PLASMA_SMELTER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PLASMA_GRANULATOR.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PLASMA_SMELTER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PLASMA_ENERGY_BATTERY.get(), EnergyBatteryBlockEntity::getEnergyHandler);
        // Circuit Assembler (plasma in through Input faces) and Ore Washer (water in through Input faces)
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CIRCUIT_ASSEMBLER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ORE_WASHER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CIRCUIT_ASSEMBLER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ORE_WASHER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CIRCUIT_ASSEMBLER.get(), PlasmaMachineBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ORE_WASHER.get(), OreWasherBlockEntity::getFluidHandler);
        // Tier 4 Pellet Press: casings in / pellets out, FE, deuterium + tritium in through Input faces
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PELLET_PRESS.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PELLET_PRESS.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.PELLET_PRESS.get(), PelletPressBlockEntity::getFluidHandler);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
