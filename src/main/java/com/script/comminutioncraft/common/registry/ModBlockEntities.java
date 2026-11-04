package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.CoalSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectrolyserBlockEntity;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.machine.TritiumBreederBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.CircuitAssemblerBlockEntity;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaSmelterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    private ModBlockEntities() {}
}
