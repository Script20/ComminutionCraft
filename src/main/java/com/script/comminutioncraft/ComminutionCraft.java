package com.script.comminutioncraft;

import com.script.comminutioncraft.common.block.ModWoods;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.CableEnergyHandler;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.cable.PipeFluidHandler;
import com.script.comminutioncraft.common.compat.patchouli.GuideBook;
import com.script.comminutioncraft.common.config.ComminutionConfig;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.item.tool.AreaBreaker;
import com.script.comminutioncraft.common.item.tool.OreHammer;
import com.script.comminutioncraft.common.item.tool.PoweredTool;
import com.script.comminutioncraft.common.item.tool.ToolTier;
import com.script.comminutioncraft.common.machine.AcceleratorPortBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeChestBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.FluidMachineBlockEntity;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.FrothFlotationCellBlockEntity;
import com.script.comminutioncraft.common.machine.HorizonPortBlockEntity;
import com.script.comminutioncraft.common.machine.OrePurifierBlockEntity;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaMachineBlockEntity;
import com.script.comminutioncraft.common.machine.ReactorPortBlockEntity;
import com.script.comminutioncraft.common.network.SetGhostItemPayload;
import com.script.comminutioncraft.common.network.SwitchDrillModePayload;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import com.script.comminutioncraft.common.recipe.RecipeSync;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModCreativeTabs;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModDataMaps;
import com.script.comminutioncraft.common.registry.ModEntities;
import com.script.comminutioncraft.common.registry.ModFeatures;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
import com.script.comminutioncraft.common.registry.ModSounds;
import com.script.comminutioncraft.common.registry.ModTriggers;
import com.script.comminutioncraft.gametest.ModGameTests;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@Mod(ComminutionCraft.MODID)
public class ComminutionCraft {
    public static final String MODID = "comminutioncraft";

    public ComminutionCraft(IEventBus modEventBus, ModContainer modContainer) {
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModTriggers.TRIGGERS.register(modEventBus);
        // the guide book (Patchouli, optional): who has been given one, and the first-join gift
        GuideBook.ATTACHMENTS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(GuideBook::onPlayerLoggedIn);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON,
                ComminutionConfig.SPEC);
        // Balance numbers: per world, synced to players (see MachineConfig)
        modContainer.registerConfig(ModConfig.Type.SERVER,
                MachineConfig.SPEC);

        ModGameTests.TEST_FUNCTIONS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(ModDataMaps::register);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(SwitchWrenchModePayload::register);
        modEventBus.addListener(SwitchDrillModePayload::register);
        modEventBus.addListener(SetGhostItemPayload::register);
        ModWoods.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(RecipeSync::sendToClients);
        NeoForge.EVENT_BUS.addListener(OreHammer::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(AreaBreaker::onBlockDrops);
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
        // Drills and chainsaws (tiers 2-6) hold FE, so an Energy Battery's charge slot (or another mod's charger) fills them
        for (DeferredItem<? extends PoweredTool> tool : Stream.concat(ModItems.DRILLS.stream(), ModItems.CHAINSAWS.stream()).toList()) {
            ToolTier tier = tool.get().tier();
            if (tier.powered()) {
                event.registerItem(Capabilities.Energy.ITEM, (stack, access) -> new ItemAccessEnergyHandler(access,
                        ModDataComponents.STORED_ENERGY.get(), tier.capacity(), tier.capacity()), tool.get());
            }
        }

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
        for (var type : List.of(ModBlockEntities.ELECTROLYSER.get(), ModBlockEntities.PLASMA_GENERATOR.get(),
                ModBlockEntities.MHD_GENERATOR.get(), ModBlockEntities.ISOTOPE_SEPARATOR.get(), ModBlockEntities.TRITIUM_BREEDER.get(),
                ModBlockEntities.ANNIHILATION_GENERATOR.get())) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (machine, side) -> machine.getEnergyHandler());
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, FluidMachineBlockEntity::getFluidHandler);
        }
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_TANK.get(), FluidTankBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CONTAINMENT_CELL.get(), FluidTankBlockEntity::getFluidHandler);
        event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(ConduitBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new PipeFluidHandler(level, pos, side, (FluidPipeBlock) state.getBlock()),
                ModBlocks.FLUID_PIPE.get(), ModBlocks.PLASMA_CABLE.get(), ModBlocks.MAGNETIC_CONDUIT.get());
        event.registerBlock(Capabilities.Energy.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(EnergyCableBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new CableEnergyHandler(level, pos, side, ((EnergyCableBlock) state.getBlock()).getTransferRate()),
                ModBlocks.ENERGY_CABLE.get(), ModBlocks.FUSION_CABLE.get(), ModBlocks.ANTIMATTER_CABLE.get(), ModBlocks.SINGULARITY_CABLE.get());

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
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ANTIMATTER_ENERGY_BATTERY.get(), EnergyBatteryBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.SINGULARITY_ENERGY_BATTERY.get(), EnergyBatteryBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.SINGULARITY_PROCESSOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.SINGULARITY_PROCESSOR.get(), (machine, side) -> machine.getEnergyHandler());
        // Tier 5 Particle Accelerator: like the reactor, through its ports (the controller answers only "no side")
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ACCELERATOR_PORT.get(), AcceleratorPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ACCELERATOR_PORT.get(), AcceleratorPortBlockEntity::getEnergyHandler);
        // Tier 6 Event Horizon Reactor: through its ports (the controller answers "no side" and takes items from hoppers)
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getEnergy() : null);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.PARTICLE_ACCELERATOR.get(),
                (accelerator, side) -> side == null ? accelerator.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PARTICLE_ACCELERATOR.get(),
                (accelerator, side) -> side == null ? accelerator.getEnergy() : null);
        // Creative blocks: endless FE / fluid out of every face
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CREATIVE_ENERGY_BATTERY.get(),
                (battery, side) -> CreativeEnergyBatteryBlockEntity.ENDLESS);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CREATIVE_FLUID_TANK.get(), CreativeFluidTankBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CREATIVE_CHEST.get(), CreativeChestBlockEntity::getItemHandler);
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
        // Tier 5 suite and the Froth Flotation Cell
        for (var type : List.of(ModBlockEntities.ANTIMATTER_GRANULATOR.get(), ModBlockEntities.ANTIMATTER_SMELTER.get())) {
            event.registerBlockEntity(Capabilities.Item.BLOCK, type, WorldlyContainerWrapper::new);
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (machine, side) -> machine.getEnergyHandler());
        }
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ANTIMATTER_WASHER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ANTIMATTER_WASHER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ANTIMATTER_WASHER.get(), OreWasherBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ANTIMATTER_PURIFIER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ANTIMATTER_PURIFIER.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ANTIMATTER_PURIFIER.get(), OrePurifierBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.FROTH_FLOTATION_CELL.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FROTH_FLOTATION_CELL.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FROTH_FLOTATION_CELL.get(),
                FrothFlotationCellBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.PELLET_PRESS.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PELLET_PRESS.get(), (machine, side) -> machine.getEnergyHandler());
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.PELLET_PRESS.get(), PelletPressBlockEntity::getFluidHandler);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
