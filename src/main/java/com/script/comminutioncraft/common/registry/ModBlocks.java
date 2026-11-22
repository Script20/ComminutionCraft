package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.block.WoodSet;
import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.machine.AcceleratorControllerBlock;
import com.script.comminutioncraft.common.machine.AcceleratorPortBlock;
import com.script.comminutioncraft.common.machine.AcceleratorSegmentBlock;
import com.script.comminutioncraft.common.machine.AnnihilationGeneratorBlock;
import com.script.comminutioncraft.common.machine.AntimatterGranulatorBlock;
import com.script.comminutioncraft.common.machine.AntimatterPurifierBlock;
import com.script.comminutioncraft.common.machine.AntimatterSmelterBlock;
import com.script.comminutioncraft.common.machine.AntimatterWasherBlock;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlock;
import com.script.comminutioncraft.common.machine.CircuitAssemblerBlock;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlock;
import com.script.comminutioncraft.common.machine.CoalSmelterBlock;
import com.script.comminutioncraft.common.machine.ContainmentCellBlock;
import com.script.comminutioncraft.common.machine.ContainmentFrameBlock;
import com.script.comminutioncraft.common.machine.CreativeChestBlock;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlock;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlock;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlock;
import com.script.comminutioncraft.common.machine.ElectricSmelterBlock;
import com.script.comminutioncraft.common.machine.ElectrolyserBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.FactoryBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlock;
import com.script.comminutioncraft.common.machine.FrothFlotationCellBlock;
import com.script.comminutioncraft.common.machine.HorizonControllerBlock;
import com.script.comminutioncraft.common.machine.HorizonPortBlock;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlock;
import com.script.comminutioncraft.common.machine.MachineBlock;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlock;
import com.script.comminutioncraft.common.machine.OrePurifierBlock;
import com.script.comminutioncraft.common.machine.OreWasherBlock;
import com.script.comminutioncraft.common.machine.PelletPressBlock;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlock;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlock;
import com.script.comminutioncraft.common.machine.PlasmaSmelterBlock;
import com.script.comminutioncraft.common.machine.ReactorCasingBlock;
import com.script.comminutioncraft.common.machine.ReactorControllerBlock;
import com.script.comminutioncraft.common.machine.ReactorGlassBlock;
import com.script.comminutioncraft.common.machine.ReactorPortBlock;
import com.script.comminutioncraft.common.machine.SingularityProcessorBlock;
import com.script.comminutioncraft.common.machine.TritiumBreederBlock;
import com.script.comminutioncraft.common.machine.WashingFactoryBlock;
import com.script.comminutioncraft.common.machine.miner.OreMinerBlock;
import com.script.comminutioncraft.common.machine.miner.OreMinerTier;
import com.script.comminutioncraft.common.pipe.ItemPipeBlock;
import com.script.comminutioncraft.common.storage.SoarynChestBlock;
import com.script.comminutioncraft.common.worldgen.ModTrees;
import java.util.List;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

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

    /** Item Pipe (tier 2): see-through, so it mustn't hide its neighbours' faces. */
    // ---- Ore Miners: one per tier 2-6 (see OreMinerTier) ----
    public static final DeferredBlock<OreMinerBlock> ELECTRIC_ORE_MINER = BLOCKS.registerBlock(
            "electric_ore_miner", properties -> new OreMinerBlock(properties, OreMinerTier.ELECTRIC), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<OreMinerBlock> PLASMA_ORE_MINER = BLOCKS.registerBlock(
            "plasma_ore_miner", properties -> new OreMinerBlock(properties, OreMinerTier.PLASMA), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<OreMinerBlock> FUSION_ORE_MINER = BLOCKS.registerBlock(
            "fusion_ore_miner", properties -> new OreMinerBlock(properties, OreMinerTier.FUSION), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<OreMinerBlock> ANTIMATTER_ORE_MINER = BLOCKS.registerBlock(
            "antimatter_ore_miner", properties -> new OreMinerBlock(properties, OreMinerTier.ANTIMATTER), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<OreMinerBlock> SINGULARITY_ORE_MINER = BLOCKS.registerBlock(
            "singularity_ore_miner", properties -> new OreMinerBlock(properties, OreMinerTier.SINGULARITY), ModBlocks::tier2MachineProperties);
    public static final List<DeferredBlock<OreMinerBlock>> ORE_MINERS = List.of(ELECTRIC_ORE_MINER, PLASMA_ORE_MINER, FUSION_ORE_MINER, ANTIMATTER_ORE_MINER, SINGULARITY_ORE_MINER);

    // ---- Factories: a machine with 2-4 lanes (see Factories), from a Factory Upgrade Kit ----
    public static final DeferredBlock<FactoryBlock> ELECTRIC_SMELTING_FACTORY = BLOCKS.registerBlock(
            "electric_smelting_factory", properties -> new FactoryBlock(properties, ModBlockEntities.ELECTRIC_SMELTING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> PLASMA_GRANULATING_FACTORY = BLOCKS.registerBlock(
            "plasma_granulating_factory", properties -> new FactoryBlock(properties, ModBlockEntities.PLASMA_GRANULATING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> PLASMA_SMELTING_FACTORY = BLOCKS.registerBlock(
            "plasma_smelting_factory", properties -> new FactoryBlock(properties, ModBlockEntities.PLASMA_SMELTING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> PURIFYING_FACTORY = BLOCKS.registerBlock(
            "purifying_factory", properties -> new FactoryBlock(properties, ModBlockEntities.PURIFYING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<WashingFactoryBlock> ANTIMATTER_WASHING_FACTORY = BLOCKS.registerBlock(
            "antimatter_washing_factory", properties -> new WashingFactoryBlock(properties, ModBlockEntities.ANTIMATTER_WASHING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> ANTIMATTER_PURIFYING_FACTORY = BLOCKS.registerBlock(
            "antimatter_purifying_factory", properties -> new FactoryBlock(properties, ModBlockEntities.ANTIMATTER_PURIFYING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> ANTIMATTER_GRANULATING_FACTORY = BLOCKS.registerBlock(
            "antimatter_granulating_factory", properties -> new FactoryBlock(properties, ModBlockEntities.ANTIMATTER_GRANULATING_FACTORY), ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FactoryBlock> ANTIMATTER_SMELTING_FACTORY = BLOCKS.registerBlock(
            "antimatter_smelting_factory", properties -> new FactoryBlock(properties, ModBlockEntities.ANTIMATTER_SMELTING_FACTORY), ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<ItemPipeBlock> ITEM_PIPE = BLOCKS.registerBlock(
            "item_pipe", properties -> new ItemPipeBlock(properties, false, ModItems.ELECTRONIC_CIRCUIT), properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    /** Fusion Item Pipe (tier 4): moves items instantly; its own network (doesn't join Item Pipes). */
    public static final DeferredBlock<ItemPipeBlock> FUSION_ITEM_PIPE = BLOCKS.registerBlock(
            "fusion_item_pipe", properties -> new ItemPipeBlock(properties, true, ModItems.FUSION_CIRCUIT), properties -> properties
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

    // Tier 5 (antimatter), phase 5A part 1: containment
    /** Antimatter tank; see-through window like the Fluid Tank. */
    public static final DeferredBlock<ContainmentCellBlock> CONTAINMENT_CELL = BLOCKS.registerBlock(
            "containment_cell", ContainmentCellBlock::new, properties -> tier2MachineProperties(properties)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isRedstoneConductor((state, level, pos) -> false));

    /** A pipe for antimatter only; never joins other pipes or cables. */
    public static final DeferredBlock<FluidPipeBlock> MAGNETIC_CONDUIT = BLOCKS.registerBlock(
            "magnetic_conduit", properties -> new FluidPipeBlock(properties, FluidPipeBlock.MAGNETIC_CONDUIT_RATE,
                    ModFluids::isAntimatter, () -> FluidResource.of(ModFluids.ANTIMATTER.get())),
            properties -> properties
                    .mapColor(MapColor.SNOW)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    // Tier 5 (antimatter), phase 5A part 2: the Particle Accelerator ring
    public static final DeferredBlock<AcceleratorSegmentBlock> ACCELERATOR_SEGMENT = BLOCKS.registerBlock(
            "accelerator_segment", AcceleratorSegmentBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<AcceleratorPortBlock> ACCELERATOR_PORT = BLOCKS.registerBlock(
            "accelerator_port", AcceleratorPortBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<AcceleratorControllerBlock> ACCELERATOR_CONTROLLER = BLOCKS.registerBlock(
            "accelerator_controller", AcceleratorControllerBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 6 phase 6A: the Event Horizon Reactor
    public static final DeferredBlock<ContainmentFrameBlock> CONTAINMENT_FRAME = BLOCKS.registerBlock(
            "containment_frame", ContainmentFrameBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<HorizonControllerBlock> HORIZON_CONTROLLER = BLOCKS.registerBlock(
            "horizon_controller", HorizonControllerBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<HorizonPortBlock> HORIZON_PORT = BLOCKS.registerBlock(
            "horizon_port", HorizonPortBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 5 phase 5C: the 4x step and the antimatter machine suite
    public static final DeferredBlock<AntimatterGranulatorBlock> ANTIMATTER_GRANULATOR = BLOCKS.registerBlock(
            "antimatter_granulator", AntimatterGranulatorBlock::new, ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<AntimatterSmelterBlock> ANTIMATTER_SMELTER = BLOCKS.registerBlock(
            "antimatter_smelter", AntimatterSmelterBlock::new, ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<FrothFlotationCellBlock> FROTH_FLOTATION_CELL = BLOCKS.registerBlock(
            "froth_flotation_cell", FrothFlotationCellBlock::new, ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<AntimatterWasherBlock> ANTIMATTER_WASHER = BLOCKS.registerBlock(
            "antimatter_washer", AntimatterWasherBlock::new, ModBlocks::tier2MachineProperties);
    public static final DeferredBlock<AntimatterPurifierBlock> ANTIMATTER_PURIFIER = BLOCKS.registerBlock(
            "antimatter_purifier", AntimatterPurifierBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 5 phase 5B: antimatter power
    public static final DeferredBlock<AnnihilationGeneratorBlock> ANNIHILATION_GENERATOR = BLOCKS.registerBlock(
            "annihilation_generator", AnnihilationGeneratorBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 6 phase 6B: the 5x step and tier-6 power gear
    public static final DeferredBlock<SingularityProcessorBlock> SINGULARITY_PROCESSOR = BLOCKS.registerBlock(
            "singularity_processor", SingularityProcessorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyCableBlock> SINGULARITY_CABLE = BLOCKS.registerBlock(
            "singularity_cable", properties -> new EnergyCableBlock(properties, EnergyCableBlock.SINGULARITY_CABLE_RATE), properties -> properties
                    .mapColor(MapColor.GOLD)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    public static final DeferredBlock<EnergyBatteryBlock> SINGULARITY_ENERGY_BATTERY = BLOCKS.registerBlock(
            "singularity_energy_battery", properties -> new EnergyBatteryBlock(properties, () -> ModBlockEntities.SINGULARITY_ENERGY_BATTERY.get()),
            ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<EnergyCableBlock> ANTIMATTER_CABLE = BLOCKS.registerBlock(
            "antimatter_cable", properties -> new EnergyCableBlock(properties, EnergyCableBlock.ANTIMATTER_CABLE_RATE), properties -> properties
                    .mapColor(MapColor.SNOW)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

    public static final DeferredBlock<EnergyBatteryBlock> ANTIMATTER_ENERGY_BATTERY = BLOCKS.registerBlock(
            "antimatter_energy_battery", properties -> new EnergyBatteryBlock(properties, () -> ModBlockEntities.ANTIMATTER_ENERGY_BATTERY.get()),
            ModBlocks::tier2MachineProperties);

    // Tier 4 (fusion), phase 4A: the fuel chain
    public static final DeferredBlock<IsotopeSeparatorBlock> ISOTOPE_SEPARATOR = BLOCKS.registerBlock(
            "isotope_separator", IsotopeSeparatorBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<TritiumBreederBlock> TRITIUM_BREEDER = BLOCKS.registerBlock(
            "tritium_breeder", TritiumBreederBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<PelletPressBlock> PELLET_PRESS = BLOCKS.registerBlock(
            "pellet_press", PelletPressBlock::new, ModBlocks::tier2MachineProperties);

    // Tier 4 (fusion), phase 4B: the 3x3x3 Fusion Reactor and the fast power blocks
    // Tier 4 phase 4C: the by-product step
    public static final DeferredBlock<OrePurifierBlock> ORE_PURIFIER = BLOCKS.registerBlock(
            "ore_purifier", OrePurifierBlock::new, ModBlocks::tier2MachineProperties);

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

    // Creative only (no recipes): endless FE / endless fluid of a picked kind
    public static final DeferredBlock<CreativeEnergyBatteryBlock> CREATIVE_ENERGY_BATTERY = BLOCKS.registerBlock(
            "creative_energy_battery", CreativeEnergyBatteryBlock::new, ModBlocks::tier2MachineProperties);

    public static final DeferredBlock<CreativeChestBlock> CREATIVE_CHEST = BLOCKS.registerBlock(
            "creative_chest", CreativeChestBlock::new, ModBlocks::tier2MachineProperties);

    /** Soaryn Chest: vanilla chest properties (wood, 2.5 strength, burns in lava). */
    public static final DeferredBlock<SoarynChestBlock> SOARYN_CHEST = BLOCKS.registerBlock(
            "soaryn_chest", SoarynChestBlock::new, properties -> properties
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final DeferredBlock<CreativeFluidTankBlock> CREATIVE_FLUID_TANK = BLOCKS.registerBlock(
            "creative_fluid_tank", CreativeFluidTankBlock::new, properties -> tier2MachineProperties(properties)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isRedstoneConductor((state, level, pos) -> false));

    // Decorative stone (marble generates like granite; volcanic basalt crusts underground lava pools)
    public static final StoneSet MARBLE = new StoneSet(BLOCKS, "marble", MapColor.QUARTZ, 1.5F, 6.0F, SoundType.STONE);
    public static final StoneSet VOLCANIC_BASALT = new StoneSet(BLOCKS, "volcanic_basalt", MapColor.COLOR_BLACK, 2.0F, 6.0F, SoundType.BASALT);
    public static final List<StoneSet> STONE_SETS = List.of(MARBLE, VOLCANIC_BASALT);

    // Purpleheart wood (a tall, straight jungle tree; purple heartwood)
    public static final WoodSet PURPLEHEART = new WoodSet(
            BLOCKS, "purpleheart", MapColor.COLOR_PURPLE, MapColor.COLOR_GRAY, 0xFF3C7640,
            ModTrees.PURPLEHEART_GROWER);

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
