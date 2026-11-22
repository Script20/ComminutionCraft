package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.AcceleratorPortBlockEntity;
import com.script.comminutioncraft.common.machine.AnnihilationGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.AntimatterGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.AntimatterPurifierBlockEntity;
import com.script.comminutioncraft.common.machine.AntimatterSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.AntimatterWasherBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.CircuitAssemblerBlockEntity;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.CoalSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.ContainmentCellBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeChestBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.ElectrolyserBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.FrothFlotationCellBlockEntity;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
import com.script.comminutioncraft.common.machine.HorizonPortBlockEntity;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlockEntity;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.OrePurifierBlockEntity;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.ReactorPortBlockEntity;
import com.script.comminutioncraft.common.machine.SingularityProcessorBlockEntity;
import com.script.comminutioncraft.common.machine.TritiumBreederBlockEntity;
import com.script.comminutioncraft.common.machine.miner.OreMinerBlockEntity;
import com.script.comminutioncraft.common.pipe.ItemPipeBlockEntity;
import com.script.comminutioncraft.common.storage.SoarynChestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ComminutionCraft.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalGranulatorBlockEntity>> COAL_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("coal_granulator",
                    () -> new BlockEntityType<>(CoalGranulatorBlockEntity::new, ModBlocks.COAL_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalSmelterBlockEntity>> COAL_SMELTER =
            BLOCK_ENTITY_TYPES.register("coal_smelter",
                    () -> new BlockEntityType<>(CoalSmelterBlockEntity::new, ModBlocks.COAL_SMELTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricGranulatorBlockEntity>> ELECTRIC_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("electric_granulator",
                    () -> new BlockEntityType<>(ElectricGranulatorBlockEntity::new, ModBlocks.ELECTRIC_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricSmelterBlockEntity>> ELECTRIC_SMELTER =
            BLOCK_ENTITY_TYPES.register("electric_smelter",
                    () -> new BlockEntityType<>(ElectricSmelterBlockEntity::new, ModBlocks.ELECTRIC_SMELTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricGeneratorBlockEntity>> ELECTRIC_GENERATOR =
            BLOCK_ENTITY_TYPES.register("electric_generator",
                    () -> new BlockEntityType<>(ElectricGeneratorBlockEntity::new, ModBlocks.ELECTRIC_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("energy_battery",
                    () -> new BlockEntityType<>(EnergyBatteryBlockEntity::new, ModBlocks.ENERGY_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlasmaGeneratorBlockEntity>> PLASMA_GENERATOR =
            BLOCK_ENTITY_TYPES.register("plasma_generator",
                    () -> new BlockEntityType<>(PlasmaGeneratorBlockEntity::new, ModBlocks.PLASMA_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITY_TYPES.register("fluid_tank",
                    () -> new BlockEntityType<>(FluidTankBlockEntity::new, ModBlocks.FLUID_TANK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlasmaGranulatorBlockEntity>> PLASMA_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("plasma_granulator",
                    () -> new BlockEntityType<>(PlasmaGranulatorBlockEntity::new, ModBlocks.PLASMA_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlasmaSmelterBlockEntity>> PLASMA_SMELTER =
            BLOCK_ENTITY_TYPES.register("plasma_smelter",
                    () -> new BlockEntityType<>(PlasmaSmelterBlockEntity::new, ModBlocks.PLASMA_SMELTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> PLASMA_ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("plasma_energy_battery",
                    () -> new BlockEntityType<>(EnergyBatteryBlockEntity::plasma, ModBlocks.PLASMA_ENERGY_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CircuitAssemblerBlockEntity>> CIRCUIT_ASSEMBLER =
            BLOCK_ENTITY_TYPES.register("circuit_assembler",
                    () -> new BlockEntityType<>(CircuitAssemblerBlockEntity::new, ModBlocks.CIRCUIT_ASSEMBLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OreWasherBlockEntity>> ORE_WASHER =
            BLOCK_ENTITY_TYPES.register("ore_washer",
                    () -> new BlockEntityType<>(OreWasherBlockEntity::new, ModBlocks.ORE_WASHER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtmosphericWaterGeneratorBlockEntity>> ATMOSPHERIC_WATER_GENERATOR =
            BLOCK_ENTITY_TYPES.register("atmospheric_water_generator",
                    () -> new BlockEntityType<>(AtmosphericWaterGeneratorBlockEntity::new, ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectrolyserBlockEntity>> ELECTROLYSER =
            BLOCK_ENTITY_TYPES.register("electrolyser",
                    () -> new BlockEntityType<>(ElectrolyserBlockEntity::new, ModBlocks.ELECTROLYSER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MhdGeneratorBlockEntity>> MHD_GENERATOR =
            BLOCK_ENTITY_TYPES.register("mhd_generator",
                    () -> new BlockEntityType<>(MhdGeneratorBlockEntity::new, ModBlocks.MHD_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IsotopeSeparatorBlockEntity>> ISOTOPE_SEPARATOR =
            BLOCK_ENTITY_TYPES.register("isotope_separator",
                    () -> new BlockEntityType<>(IsotopeSeparatorBlockEntity::new, ModBlocks.ISOTOPE_SEPARATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TritiumBreederBlockEntity>> TRITIUM_BREEDER =
            BLOCK_ENTITY_TYPES.register("tritium_breeder",
                    () -> new BlockEntityType<>(TritiumBreederBlockEntity::new, ModBlocks.TRITIUM_BREEDER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PelletPressBlockEntity>> PELLET_PRESS =
            BLOCK_ENTITY_TYPES.register("pellet_press",
                    () -> new BlockEntityType<>(PelletPressBlockEntity::new, ModBlocks.PELLET_PRESS.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FusionReactorBlockEntity>> FUSION_REACTOR =
            BLOCK_ENTITY_TYPES.register("fusion_reactor",
                    () -> new BlockEntityType<>(FusionReactorBlockEntity::new, ModBlocks.REACTOR_CONTROLLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorPortBlockEntity>> REACTOR_PORT =
            BLOCK_ENTITY_TYPES.register("reactor_port",
                    () -> new BlockEntityType<>(ReactorPortBlockEntity::new, ModBlocks.REACTOR_PORT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> FUSION_ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("fusion_energy_battery",
                    () -> new BlockEntityType<>(EnergyBatteryBlockEntity::fusion, ModBlocks.FUSION_ENERGY_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrePurifierBlockEntity>> ORE_PURIFIER =
            BLOCK_ENTITY_TYPES.register("ore_purifier",
                    () -> new BlockEntityType<>(OrePurifierBlockEntity::new, ModBlocks.ORE_PURIFIER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEnergyBatteryBlockEntity>> CREATIVE_ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("creative_energy_battery",
                    () -> new BlockEntityType<>(CreativeEnergyBatteryBlockEntity::new, ModBlocks.CREATIVE_ENERGY_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeFluidTankBlockEntity>> CREATIVE_FLUID_TANK =
            BLOCK_ENTITY_TYPES.register("creative_fluid_tank",
                    () -> new BlockEntityType<>(CreativeFluidTankBlockEntity::new, ModBlocks.CREATIVE_FLUID_TANK.get()));

    /** Every Ore Miner (the tier comes from the block). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OreMinerBlockEntity>> ORE_MINER =
            BLOCK_ENTITY_TYPES.register("ore_miner", () -> new BlockEntityType<>(OreMinerBlockEntity::new,
                    ModBlocks.ORE_MINERS.stream().map(DeferredBlock::get).toArray(Block[]::new)));

    // ---- Factories: the machines' own block entities with more lanes ----
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricSmelterBlockEntity>> ELECTRIC_SMELTING_FACTORY =
            BLOCK_ENTITY_TYPES.register("electric_smelting_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new ElectricSmelterBlockEntity(ModBlockEntities.ELECTRIC_SMELTING_FACTORY.get(), pos, state, 2), ModBlocks.ELECTRIC_SMELTING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlasmaGranulatorBlockEntity>> PLASMA_GRANULATING_FACTORY =
            BLOCK_ENTITY_TYPES.register("plasma_granulating_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new PlasmaGranulatorBlockEntity(ModBlockEntities.PLASMA_GRANULATING_FACTORY.get(), pos, state, 3), ModBlocks.PLASMA_GRANULATING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlasmaSmelterBlockEntity>> PLASMA_SMELTING_FACTORY =
            BLOCK_ENTITY_TYPES.register("plasma_smelting_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new PlasmaSmelterBlockEntity(ModBlockEntities.PLASMA_SMELTING_FACTORY.get(), pos, state, 3), ModBlocks.PLASMA_SMELTING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrePurifierBlockEntity>> PURIFYING_FACTORY =
            BLOCK_ENTITY_TYPES.register("purifying_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new OrePurifierBlockEntity(ModBlockEntities.PURIFYING_FACTORY.get(), pos, state, 3), ModBlocks.PURIFYING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterWasherBlockEntity>> ANTIMATTER_WASHING_FACTORY =
            BLOCK_ENTITY_TYPES.register("antimatter_washing_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new AntimatterWasherBlockEntity(ModBlockEntities.ANTIMATTER_WASHING_FACTORY.get(), pos, state, 4), ModBlocks.ANTIMATTER_WASHING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterPurifierBlockEntity>> ANTIMATTER_PURIFYING_FACTORY =
            BLOCK_ENTITY_TYPES.register("antimatter_purifying_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new AntimatterPurifierBlockEntity(ModBlockEntities.ANTIMATTER_PURIFYING_FACTORY.get(), pos, state, 4), ModBlocks.ANTIMATTER_PURIFYING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterGranulatorBlockEntity>> ANTIMATTER_GRANULATING_FACTORY =
            BLOCK_ENTITY_TYPES.register("antimatter_granulating_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new AntimatterGranulatorBlockEntity(ModBlockEntities.ANTIMATTER_GRANULATING_FACTORY.get(), pos, state, 4), ModBlocks.ANTIMATTER_GRANULATING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterSmelterBlockEntity>> ANTIMATTER_SMELTING_FACTORY =
            BLOCK_ENTITY_TYPES.register("antimatter_smelting_factory", () -> new BlockEntityType<>(
                    (pos, state) -> new AntimatterSmelterBlockEntity(ModBlockEntities.ANTIMATTER_SMELTING_FACTORY.get(), pos, state, 4), ModBlocks.ANTIMATTER_SMELTING_FACTORY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE =
            BLOCK_ENTITY_TYPES.register("item_pipe",
                    () -> new BlockEntityType<>(ItemPipeBlockEntity::new, ModBlocks.ITEM_PIPE.get(), ModBlocks.FUSION_ITEM_PIPE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeChestBlockEntity>> CREATIVE_CHEST =
            BLOCK_ENTITY_TYPES.register("creative_chest",
                    () -> new BlockEntityType<>(CreativeChestBlockEntity::new, ModBlocks.CREATIVE_CHEST.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SoarynChestBlockEntity>> SOARYN_CHEST =
            BLOCK_ENTITY_TYPES.register("soaryn_chest",
                    () -> new BlockEntityType<>(SoarynChestBlockEntity::new, ModBlocks.SOARYN_CHEST.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainmentCellBlockEntity>> CONTAINMENT_CELL =
            BLOCK_ENTITY_TYPES.register("containment_cell",
                    () -> new BlockEntityType<>(ContainmentCellBlockEntity::new, ModBlocks.CONTAINMENT_CELL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParticleAcceleratorBlockEntity>> PARTICLE_ACCELERATOR =
            BLOCK_ENTITY_TYPES.register("particle_accelerator",
                    () -> new BlockEntityType<>(ParticleAcceleratorBlockEntity::new, ModBlocks.ACCELERATOR_CONTROLLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AcceleratorPortBlockEntity>> ACCELERATOR_PORT =
            BLOCK_ENTITY_TYPES.register("accelerator_port",
                    () -> new BlockEntityType<>(AcceleratorPortBlockEntity::new, ModBlocks.ACCELERATOR_PORT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnnihilationGeneratorBlockEntity>> ANNIHILATION_GENERATOR =
            BLOCK_ENTITY_TYPES.register("annihilation_generator",
                    () -> new BlockEntityType<>(AnnihilationGeneratorBlockEntity::new, ModBlocks.ANNIHILATION_GENERATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> ANTIMATTER_ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("antimatter_energy_battery",
                    () -> new BlockEntityType<>(EnergyBatteryBlockEntity::antimatter, ModBlocks.ANTIMATTER_ENERGY_BATTERY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterGranulatorBlockEntity>> ANTIMATTER_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("antimatter_granulator", () -> new BlockEntityType<>(AntimatterGranulatorBlockEntity::new, ModBlocks.ANTIMATTER_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterSmelterBlockEntity>> ANTIMATTER_SMELTER =
            BLOCK_ENTITY_TYPES.register("antimatter_smelter", () -> new BlockEntityType<>(AntimatterSmelterBlockEntity::new, ModBlocks.ANTIMATTER_SMELTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrothFlotationCellBlockEntity>> FROTH_FLOTATION_CELL =
            BLOCK_ENTITY_TYPES.register("froth_flotation_cell", () -> new BlockEntityType<>(FrothFlotationCellBlockEntity::new, ModBlocks.FROTH_FLOTATION_CELL.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterWasherBlockEntity>> ANTIMATTER_WASHER =
            BLOCK_ENTITY_TYPES.register("antimatter_washer", () -> new BlockEntityType<>(AntimatterWasherBlockEntity::new, ModBlocks.ANTIMATTER_WASHER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntimatterPurifierBlockEntity>> ANTIMATTER_PURIFIER =
            BLOCK_ENTITY_TYPES.register("antimatter_purifier", () -> new BlockEntityType<>(AntimatterPurifierBlockEntity::new, ModBlocks.ANTIMATTER_PURIFIER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EventHorizonReactorBlockEntity>> EVENT_HORIZON_REACTOR =
            BLOCK_ENTITY_TYPES.register("event_horizon_reactor",
                    () -> new BlockEntityType<>(EventHorizonReactorBlockEntity::new, ModBlocks.HORIZON_CONTROLLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HorizonPortBlockEntity>> HORIZON_PORT =
            BLOCK_ENTITY_TYPES.register("horizon_port", () -> new BlockEntityType<>(HorizonPortBlockEntity::new, ModBlocks.HORIZON_PORT.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SingularityProcessorBlockEntity>> SINGULARITY_PROCESSOR =
            BLOCK_ENTITY_TYPES.register("singularity_processor",
                    () -> new BlockEntityType<>(SingularityProcessorBlockEntity::new, ModBlocks.SINGULARITY_PROCESSOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBatteryBlockEntity>> SINGULARITY_ENERGY_BATTERY =
            BLOCK_ENTITY_TYPES.register("singularity_energy_battery",
                    () -> new BlockEntityType<>(EnergyBatteryBlockEntity::singularity, ModBlocks.SINGULARITY_ENERGY_BATTERY.get()));

    private ModBlockEntities() {}
}
