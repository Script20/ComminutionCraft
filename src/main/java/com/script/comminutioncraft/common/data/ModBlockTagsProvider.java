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
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.COAL_GRANULATOR.get())
                .add(ModBlocks.COAL_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GRANULATOR.get())
                .add(ModBlocks.ELECTRIC_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GENERATOR.get())
                .add(ModBlocks.ENERGY_BATTERY.get())
                .add(ModBlocks.PLASMA_GENERATOR.get())
                .add(ModBlocks.FLUID_PIPE.get())
                .add(ModBlocks.FLUID_TANK.get())
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
