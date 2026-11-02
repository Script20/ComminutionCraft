package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.cable.EnergyCableBlock;
import com.script.comminutioncraft.machine.CoalGranulatorBlock;
import com.script.comminutioncraft.machine.CoalSmelterBlock;
import com.script.comminutioncraft.machine.ElectricGeneratorBlock;
import com.script.comminutioncraft.machine.ElectricGranulatorBlock;
import com.script.comminutioncraft.machine.ElectricSmelterBlock;
import com.script.comminutioncraft.machine.MachineBlock;
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

    public static final DeferredBlock<EnergyCableBlock> ENERGY_CABLE = BLOCKS.registerBlock(
            "energy_cable", EnergyCableBlock::new, properties -> properties
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion());

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
