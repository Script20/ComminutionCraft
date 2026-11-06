package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.SOARYN_CHEST.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.COAL_GRANULATOR.get())
                .add(ModBlocks.COAL_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GRANULATOR.get())
                .add(ModBlocks.ELECTRIC_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GENERATOR.get())
                .add(ModBlocks.ENERGY_BATTERY.get())
                .add(ModBlocks.PLASMA_GENERATOR.get())
                .add(ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get())
                .add(ModBlocks.ELECTROLYSER.get())
                .add(ModBlocks.MHD_GENERATOR.get())
                .add(ModBlocks.ISOTOPE_SEPARATOR.get())
                .add(ModBlocks.TRITIUM_BREEDER.get())
                .add(ModBlocks.PELLET_PRESS.get())
                .add(ModBlocks.ORE_PURIFIER.get())
                .add(ModBlocks.CREATIVE_ENERGY_BATTERY.get())
                .add(ModBlocks.CREATIVE_FLUID_TANK.get())
                .add(ModBlocks.REACTOR_CASING.get())
                .add(ModBlocks.REACTOR_GLASS.get())
                .add(ModBlocks.REACTOR_PORT.get())
                .add(ModBlocks.REACTOR_CONTROLLER.get())
                .add(ModBlocks.FUSION_CABLE.get())
                .add(ModBlocks.FUSION_ENERGY_BATTERY.get())
                .add(ModBlocks.ANTIMATTER_GRANULATOR.get())
                .add(ModBlocks.ANTIMATTER_SMELTER.get())
                .add(ModBlocks.FROTH_FLOTATION_CELL.get())
                .add(ModBlocks.ANTIMATTER_WASHER.get())
                .add(ModBlocks.ANTIMATTER_PURIFIER.get())
                .add(ModBlocks.ANNIHILATION_GENERATOR.get())
                .add(ModBlocks.CONTAINMENT_FRAME.get())
                .add(ModBlocks.HORIZON_CONTROLLER.get())
                .add(ModBlocks.HORIZON_PORT.get())
                .add(ModBlocks.ANTIMATTER_CABLE.get())
                .add(ModBlocks.ANTIMATTER_ENERGY_BATTERY.get())
                .add(ModBlocks.FLUID_PIPE.get())
                .add(ModBlocks.FLUID_TANK.get())
                .add(ModBlocks.CONTAINMENT_CELL.get())
                .add(ModBlocks.MAGNETIC_CONDUIT.get())
                .add(ModBlocks.ACCELERATOR_SEGMENT.get())
                .add(ModBlocks.ACCELERATOR_PORT.get())
                .add(ModBlocks.ACCELERATOR_CONTROLLER.get())
                .add(ModBlocks.ENERGY_CABLE.get())
                .add(ModBlocks.PLASMA_GRANULATOR.get())
                .add(ModBlocks.PLASMA_SMELTER.get())
                .add(ModBlocks.CIRCUIT_ASSEMBLER.get())
                .add(ModBlocks.ORE_WASHER.get())
                .add(ModBlocks.PLASMA_ENERGY_BATTERY.get())
                .add(ModBlocks.PLASMA_CABLE.get());

        for (StoneSet set : ModBlocks.STONE_SETS) {
            set.all().forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
            set.allStairs().forEach(block -> tag(BlockTags.STAIRS).add(block.get()));
            set.allSlabs().forEach(block -> tag(BlockTags.SLABS).add(block.get()));
            set.allWalls().forEach(block -> tag(BlockTags.WALLS).add(block.get()));
            tag(BlockTags.STONE_BUTTONS).add(set.button.get());
            // Natural stone: caves carve it and ores/dirt/gravel blobs can replace it, like granite.
            tag(BlockTags.BASE_STONE_OVERWORLD).add(set.stone.get());
            tag(Tags.Blocks.STONES).add(set.stone.get());
            tag(Tags.Blocks.COBBLESTONES).add(set.cobbled.get(), set.mossyCobbled.get());
        }
    }
}
