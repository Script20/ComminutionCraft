package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlock;
import com.script.comminutioncraft.common.machine.CoalSmelterBlock;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlock;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlock;
import com.script.comminutioncraft.common.machine.ElectricSmelterBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlock;
import com.script.comminutioncraft.common.machine.MachineBlock;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlock;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlock;
import com.script.comminutioncraft.common.machine.ElectrolyserBlock;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlock;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlock;
import com.script.comminutioncraft.common.machine.PelletPressBlock;
import com.script.comminutioncraft.common.machine.TritiumBreederBlock;
import com.script.comminutioncraft.common.machine.ReactorCasingBlock;
import com.script.comminutioncraft.common.machine.ReactorControllerBlock;
import com.script.comminutioncraft.common.machine.ReactorGlassBlock;
import com.script.comminutioncraft.common.machine.ReactorPortBlock;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlock;
import com.script.comminutioncraft.common.machine.CircuitAssemblerBlock;
import com.script.comminutioncraft.common.machine.OreWasherBlock;
import com.script.comminutioncraft.common.machine.PlasmaSmelterBlock;
import java.util.List;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ComminutionCraft.MODID);

    // Tier 1 (coal)
    public static final DeferredBlock<CoalGranulatorBlock> COAL_GRANULATOR = BLOCKS.registerBlock(
            "coal_granulator", CoalGranulatorBlock::new, ModBlocks::tier1MachineProperties);

    public static final DeferredBlock<CoalSmelterBlock> COAL_SMELTER = BLOCKS.registerBlock(
            "coal_smelter", CoalSmelterBlock::new, ModBlocks::tier1MachineProperties);

    // Tier 2 (electric)
    public static final DeferredBlock<ElectricGranulatorBlock> ELECTRIC_GRANULATOR = BLOCKS.registerBlock(
            "electric_granulator", ElectricGranulatorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<ElectricSmelterBlock> ELECTRIC_SMELTER = BLOCKS.registerBlock(
            "electric_smelter", ElectricSmelterBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<ElectricGeneratorBlock> ELECTRIC_GENERATOR = BLOCKS.registerBlock(
            "electric_generator", ElectricGeneratorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyBatteryBlock> ENERGY_BATTERY = BLOCKS.registerBlock(
            "energy_battery", properties -> new EnergyBatteryBlock(properties, () -> ModBlockEntities.ENERGY_BATTERY.get()),
            ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyCableBlock> ENERGY_CABLE = BLOCKS.registerBlock(
            "energy_cable", properties -> new EnergyCableBlock(properties, EnergyCableBlock.ENERGY_CABLE_RATE), properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    // Tier 3 (plasma). Power chain: water generator -> electrolyser -> plasma generator -> MHD generator
    public static final DeferredBlock<AtmosphericWaterGeneratorBlock> ATMOSPHERIC_WATER_GENERATOR = BLOCKS.registerBlock(
            "atmospheric_water_generator", AtmosphericWaterGeneratorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<ElectrolyserBlock> ELECTROLYSER = BLOCKS.registerBlock(
            "electrolyser", ElectrolyserBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<MhdGeneratorBlock> MHD_GENERATOR = BLOCKS.registerBlock(
            "mhd_generator", MhdGeneratorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<PlasmaGeneratorBlock> PLASMA_GENERATOR = BLOCKS.registerBlock(
            "plasma_generator", PlasmaGeneratorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<PlasmaGranulatorBlock> PLASMA_GRANULATOR = BLOCKS.registerBlock(
            "plasma_granulator", PlasmaGranulatorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<PlasmaSmelterBlock> PLASMA_SMELTER = BLOCKS.registerBlock(
            "plasma_smelter", PlasmaSmelterBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<CircuitAssemblerBlock> CIRCUIT_ASSEMBLER = BLOCKS.registerBlock(
            "circuit_assembler", CircuitAssemblerBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<OreWasherBlock> ORE_WASHER = BLOCKS.registerBlock(
            "ore_washer", OreWasherBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyBatteryBlock> PLASMA_ENERGY_BATTERY = BLOCKS.registerBlock(
            "plasma_energy_battery", properties -> new EnergyBatteryBlock(properties, () -> ModBlockEntities.PLASMA_ENERGY_BATTERY.get()),
            ModBlocks::tier2MachineProperties);

    /** A pipe for plasma only (faster than a Fluid Pipe); never joins Fluid Pipes or cables. */
    public static final DeferredBlock<FluidPipeBlock> PLASMA_CABLE = BLOCKS.registerBlock(
            "plasma_cable", properties -> new FluidPipeBlock(properties, FluidPipeBlock.PLASMA_CABLE_RATE,
                    resource -> resource.is(ModFluids.PLASMA_TYPE.get()), () -> FluidResource.of(ModFluids.PLASMA.get())),
            properties -> properties
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    public static final DeferredBlock<FluidPipeBlock> FLUID_PIPE = BLOCKS.registerBlock(
            "fluid_pipe", properties -> new FluidPipeBlock(properties, FluidPipeBlock.FLUID_PIPE_RATE), properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    /** See-through (cutout) window, so it mustn't hide its neighbours' faces or suffocate. */
    public static final DeferredBlock<FluidTankBlock> FLUID_TANK = BLOCKS.registerBlock(
            "fluid_tank", FluidTankBlock::new, properties -> tier2MachineProperties(properties)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isRedstoneConductor((state, level, pos) -> false));

    // Tier 4 (fusion), phase 4A: the fuel chain
    public static final DeferredBlock<IsotopeSeparatorBlock> ISOTOPE_SEPARATOR = BLOCKS.registerBlock(
            "isotope_separator", IsotopeSeparatorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<TritiumBreederBlock> TRITIUM_BREEDER = BLOCKS.registerBlock(
            "tritium_breeder", TritiumBreederBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<PelletPressBlock> PELLET_PRESS = BLOCKS.registerBlock(
            "pellet_press", PelletPressBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 4 (fusion), phase 4B: the 3x3x3 Fusion Reactor and the fast power blocks
    public static final DeferredBlock<ReactorCasingBlock> REACTOR_CASING = BLOCKS.registerBlock(
            "reactor_casing", ReactorCasingBlock::new, ModBlocks::tier2MachineProperties);

    /** See-through (cutout) window, so it mustn't hide its neighbours' faces or suffocate. */
    public static final DeferredBlock<ReactorGlassBlock> REACTOR_GLASS = BLOCKS.registerBlock(
            "reactor_glass", ReactorGlassBlock::new, properties -> properties
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isRedstoneConductor((state, level, pos) -> false));

    public static final DeferredBlock<ReactorPortBlock> REACTOR_PORT = BLOCKS.registerBlock(
            "reactor_port", ReactorPortBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = BLOCKS.registerBlock(
            "reactor_controller", ReactorControllerBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyCableBlock> FUSION_CABLE = BLOCKS.registerBlock(
            "fusion_cable", properties -> new EnergyCableBlock(properties, EnergyCableBlock.FUSION_CABLE_RATE), properties -> properties
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    public static final DeferredBlock<EnergyBatteryBlock> FUSION_ENERGY_BATTERY = BLOCKS.registerBlock(
            "fusion_energy_battery", properties -> new EnergyBatteryBlock(properties, () -> ModBlockEntities.FUSION_ENERGY_BATTERY.get()),
            ModBlocks::tier2MachineProperties);

    // Decorative stone (marble generates like granite; volcanic basalt crusts underground lava pools)
    public static final StoneSet MARBLE = new StoneSet(BLOCKS, "marble", MapColor.QUARTZ, 1.5F, 6.0F, SoundType.STONE);
    public static final StoneSet VOLCANIC_BASALT = new StoneSet(BLOCKS, "volcanic_basalt", MapColor.COLOR_BLACK, 2.0F, 6.0F, SoundType.BASALT);
    public static final List<StoneSet> STONE_SETS = List.of(MARBLE, VOLCANIC_BASALT);

    /** Same feel as a vanilla furnace: needs a pickaxe, glows while burning. */
    private static BlockBehaviour.Properties tier1MachineProperties(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .lightLevel(state -> state.getValue(MachineBlock.LIT) ? 13 : 0);
    }

    /** Metal machines: needs a pickaxe, softer glow while running. */
    private static BlockBehaviour.Properties tier2MachineProperties(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(MachineBlock.LIT) ? 9 : 0);
    }

    private ModBlocks() {}
}
