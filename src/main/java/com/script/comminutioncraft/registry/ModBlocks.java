package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.machine.CoalGranulatorBlock;
import com.script.comminutioncraft.machine.CoalSmelterBlock;
import com.script.comminutioncraft.machine.FuelMachineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ComminutionCraft.MODID);

    public static final DeferredBlock<CoalGranulatorBlock> COAL_GRANULATOR = BLOCKS.registerBlock(
            "coal_granulator", CoalGranulatorBlock::new, ModBlocks::tier1MachineProperties);

    public static final DeferredBlock<CoalSmelterBlock> COAL_SMELTER = BLOCKS.registerBlock(
            "coal_smelter", CoalSmelterBlock::new, ModBlocks::tier1MachineProperties);

    /** Same feel as a vanilla furnace: needs a pickaxe, glows while burning. */
    private static BlockBehaviour.Properties tier1MachineProperties(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .lightLevel(state -> state.getValue(FuelMachineBlock.LIT) ? 13 : 0);
    }

    private ModBlocks() {}
}
