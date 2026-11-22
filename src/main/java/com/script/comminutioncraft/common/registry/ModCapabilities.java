package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.CableEnergyHandler;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.cable.PipeFluidHandler;
import com.script.comminutioncraft.common.item.tool.PoweredTool;
import com.script.comminutioncraft.common.item.tool.ToolTier;
import com.script.comminutioncraft.common.machine.AcceleratorPortBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeChestBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
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
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

/**
 * Who exposes what to hoppers, pipes, cables and other mods.
 * Items: tier 1 works like a vanilla furnace (top = input, sides = fuel, bottom = output); FE machines follow their side
 * configuration (default bottom = output, every other side = input); the generator takes fuel from any side.
 * Energy: FE machines accept it; generators give it on every side; batteries take it on Input faces and give it on Output
 * faces (none on None faces); cables on every side except ones cut with the wrench.
 * Multiblocks go through their ports; their controllers only answer "no side" queries (Jade) and hoppers.
 */
public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        tools(event);
        tier1(event);
        tier2(event);
        tier3(event);
        tier4(event);
        tier5(event);
        tier6(event);
        factories(event);
        oreMiners(event);
        conduits(event);
        creative(event);
    }

    /** Drills and chainsaws (tiers 2-6) hold FE, so an Energy Battery's charge slot (or another mod's charger) fills them. */
    private static void tools(RegisterCapabilitiesEvent event) {
        for (DeferredItem<? extends PoweredTool> tool : Stream.concat(ModItems.DRILLS.stream(), ModItems.CHAINSAWS.stream()).toList()) {
            ToolTier tier = tool.get().tier();
            if (tier.powered()) {
                event.registerItem(Capabilities.Energy.ITEM, (stack, access) -> new ItemAccessEnergyHandler(access,
                        ModDataComponents.STORED_ENERGY.get(), tier.capacity(), tier.capacity()), tool.get());
            }
        }
    }

    private static void tier1(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_GRANULATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.COAL_SMELTER.get(), WorldlyContainerWrapper::new);
    }

    private static void tier2(RegisterCapabilitiesEvent event) {
        processing(event, ModBlockEntities.ELECTRIC_GRANULATOR.get(), ModBlockEntities.ELECTRIC_SMELTER.get());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ELECTRIC_GENERATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ELECTRIC_GENERATOR.get(), (generator, side) -> generator.getEnergyHandler());
        battery(event, ModBlockEntities.ENERGY_BATTERY.get());
    }

    /**
     * Plasma machines, the tier-3 power chain (water generator: filters in, water out; electrolyser / plasma generator:
     * FE in, fluids in and out by face; MHD generator: plasma in, FE out) and the tier-4 fluid machines that share it.
     */
    private static void tier3(RegisterCapabilitiesEvent event) {
        processing(event, ModBlockEntities.PLASMA_GRANULATOR.get(), ModBlockEntities.PLASMA_SMELTER.get());
        processing(event, ModBlockEntities.CIRCUIT_ASSEMBLER.get(), PlasmaMachineBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.ORE_WASHER.get(), OreWasherBlockEntity::getFluidHandler);
        battery(event, ModBlockEntities.PLASMA_ENERGY_BATTERY.get());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get(),
                AtmosphericWaterGeneratorBlockEntity::getFluidHandler);
        for (BlockEntityType<? extends FluidMachineBlockEntity> type : List.of(ModBlockEntities.ELECTROLYSER.get(),
                ModBlockEntities.PLASMA_GENERATOR.get(), ModBlockEntities.MHD_GENERATOR.get(), ModBlockEntities.ISOTOPE_SEPARATOR.get(),
                ModBlockEntities.TRITIUM_BREEDER.get(), ModBlockEntities.ANNIHILATION_GENERATOR.get())) {
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (machine, side) -> machine.getEnergyHandler());
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, FluidMachineBlockEntity::getFluidHandler);
        }
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_TANK.get(), FluidTankBlockEntity::getFluidHandler);
    }

    /** Fusion: the reactor through its ports; Ore Purifier (oxygen in through Input faces) and Pellet Press (D + T in). */
    private static void tier4(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.REACTOR_PORT.get(), ReactorPortBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.FUSION_REACTOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FUSION_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.FUSION_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getEnergy() : null);
        processing(event, ModBlockEntities.ORE_PURIFIER.get(), OrePurifierBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.PELLET_PRESS.get(), PelletPressBlockEntity::getFluidHandler);
        battery(event, ModBlockEntities.FUSION_ENERGY_BATTERY.get());
    }

    /** Antimatter: the accelerator through its ports, the containment cell, the suite and the Froth Flotation Cell. */
    private static void tier5(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.ACCELERATOR_PORT.get(), AcceleratorPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ACCELERATOR_PORT.get(), AcceleratorPortBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.PARTICLE_ACCELERATOR.get(),
                (accelerator, side) -> side == null ? accelerator.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.PARTICLE_ACCELERATOR.get(),
                (accelerator, side) -> side == null ? accelerator.getEnergy() : null);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CONTAINMENT_CELL.get(), FluidTankBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.ANTIMATTER_GRANULATOR.get(), ModBlockEntities.ANTIMATTER_SMELTER.get());
        processing(event, ModBlockEntities.ANTIMATTER_WASHER.get(), OreWasherBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.ANTIMATTER_PURIFIER.get(), OrePurifierBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.FROTH_FLOTATION_CELL.get(), FrothFlotationCellBlockEntity::getFluidHandler);
        battery(event, ModBlockEntities.ANTIMATTER_ENERGY_BATTERY.get());
    }

    /** Singularity: the Event Horizon Reactor through its ports, the processor. */
    private static void tier6(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.HORIZON_PORT.get(), HorizonPortBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getTanks() : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.EVENT_HORIZON_REACTOR.get(),
                (reactor, side) -> side == null ? reactor.getEnergy() : null);
        processing(event, ModBlockEntities.SINGULARITY_PROCESSOR.get());
        battery(event, ModBlockEntities.SINGULARITY_ENERGY_BATTERY.get());
    }

    /** Factories: like their machines. */
    private static void factories(RegisterCapabilitiesEvent event) {
        processing(event, ModBlockEntities.ELECTRIC_SMELTING_FACTORY.get(), ModBlockEntities.PLASMA_GRANULATING_FACTORY.get(),
                ModBlockEntities.PLASMA_SMELTING_FACTORY.get(), ModBlockEntities.ANTIMATTER_GRANULATING_FACTORY.get(),
                ModBlockEntities.ANTIMATTER_SMELTING_FACTORY.get());
        processing(event, ModBlockEntities.PURIFYING_FACTORY.get(), OrePurifierBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.ANTIMATTER_PURIFYING_FACTORY.get(), OrePurifierBlockEntity::getFluidHandler);
        processing(event, ModBlockEntities.ANTIMATTER_WASHING_FACTORY.get(), OreWasherBlockEntity::getFluidHandler);
    }

    /** Ore Miners: their outputs come out of Output faces; FE on every face. */
    private static void oreMiners(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ORE_MINER.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.ORE_MINER.get(), (miner, side) -> miner.getEnergyHandler());
    }

    /** Pipes and cables store nothing: their handlers route straight to the network (not through a cut arm). */
    private static void conduits(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(ConduitBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new PipeFluidHandler(level, pos, side, (FluidPipeBlock) state.getBlock()),
                ModBlocks.FLUID_PIPE.get(), ModBlocks.PLASMA_CABLE.get(), ModBlocks.MAGNETIC_CONDUIT.get());
        event.registerBlock(Capabilities.Energy.BLOCK, (level, pos, state, blockEntity, side) ->
                side != null && state.getValue(EnergyCableBlock.CONNECTIONS.get(side)) == CableConnection.DISABLED
                        ? null : new CableEnergyHandler(level, pos, side, ((EnergyCableBlock) state.getBlock()).getTransferRate()),
                ModBlocks.ENERGY_CABLE.get(), ModBlocks.FUSION_CABLE.get(), ModBlocks.ANTIMATTER_CABLE.get(), ModBlocks.SINGULARITY_CABLE.get());
    }

    /** Creative blocks: endless FE / fluid / items out of every face. */
    private static void creative(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CREATIVE_ENERGY_BATTERY.get(),
                (battery, side) -> CreativeEnergyBatteryBlockEntity.ENDLESS);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CREATIVE_FLUID_TANK.get(), CreativeFluidTankBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CREATIVE_CHEST.get(), CreativeChestBlockEntity::getItemHandler);
    }

    // ---- Shared shapes ----

    /** FE processing machines: items through their side configuration, FE on every face. */
    @SafeVarargs
    private static void processing(RegisterCapabilitiesEvent event, BlockEntityType<? extends ElectricMachineBlockEntity>... types) {
        for (BlockEntityType<? extends ElectricMachineBlockEntity> type : types) {
            event.registerBlockEntity(Capabilities.Item.BLOCK, type, WorldlyContainerWrapper::new);
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (machine, side) -> machine.getEnergyHandler());
        }
    }

    /** A processing machine with tanks (water, oxygen, plasma...) filled through its Input faces. */
    private static <T extends ElectricMachineBlockEntity> void processing(RegisterCapabilitiesEvent event, BlockEntityType<T> type,
                                                                          ICapabilityProvider<? super T, Direction, ResourceHandler<FluidResource>> fluids) {
        processing(event, type);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, type, fluids);
    }

    private static void battery(RegisterCapabilitiesEvent event, BlockEntityType<EnergyBatteryBlockEntity> type) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, type, EnergyBatteryBlockEntity::getEnergyHandler);
    }
}
