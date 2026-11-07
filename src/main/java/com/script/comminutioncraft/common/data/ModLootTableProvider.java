package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.registry.ModBlocks;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), lookupProvider);
    }

    private static class BlockLoot extends BlockLootSubProvider {
        BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.VANILLA_SET, registries);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.COAL_GRANULATOR.get());
            dropSelf(ModBlocks.COAL_SMELTER.get());
            dropSelf(ModBlocks.ELECTRIC_GRANULATOR.get());
            dropSelf(ModBlocks.ELECTRIC_SMELTER.get());
            dropSelf(ModBlocks.ELECTRIC_GENERATOR.get());
            dropSelf(ModBlocks.ENERGY_CABLE.get());
            dropSelf(ModBlocks.ENERGY_BATTERY.get());
            dropSelf(ModBlocks.PLASMA_GENERATOR.get());
            dropSelf(ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get());
            dropSelf(ModBlocks.ELECTROLYSER.get());
            dropSelf(ModBlocks.MHD_GENERATOR.get());
            dropSelf(ModBlocks.ISOTOPE_SEPARATOR.get());
            dropSelf(ModBlocks.TRITIUM_BREEDER.get());
            dropSelf(ModBlocks.PELLET_PRESS.get());
            dropSelf(ModBlocks.ORE_PURIFIER.get());
            dropSelf(ModBlocks.CREATIVE_ENERGY_BATTERY.get());
            dropSelf(ModBlocks.CREATIVE_FLUID_TANK.get());
            dropSelf(ModBlocks.REACTOR_CASING.get());
            dropSelf(ModBlocks.REACTOR_GLASS.get());
            dropSelf(ModBlocks.REACTOR_PORT.get());
            dropSelf(ModBlocks.REACTOR_CONTROLLER.get());
            dropSelf(ModBlocks.FUSION_CABLE.get());
            dropSelf(ModBlocks.FUSION_ENERGY_BATTERY.get());
            dropSelf(ModBlocks.ANTIMATTER_GRANULATOR.get());
            dropSelf(ModBlocks.ANTIMATTER_SMELTER.get());
            dropSelf(ModBlocks.FROTH_FLOTATION_CELL.get());
            dropSelf(ModBlocks.ANTIMATTER_WASHER.get());
            dropSelf(ModBlocks.ANTIMATTER_PURIFIER.get());
            dropSelf(ModBlocks.ANNIHILATION_GENERATOR.get());
            dropSelf(ModBlocks.CONTAINMENT_FRAME.get());
            dropSelf(ModBlocks.SINGULARITY_PROCESSOR.get());
            dropSelf(ModBlocks.SINGULARITY_CABLE.get());
            dropSelf(ModBlocks.SINGULARITY_ENERGY_BATTERY.get());
            dropSelf(ModBlocks.HORIZON_CONTROLLER.get());
            dropSelf(ModBlocks.HORIZON_PORT.get());
            dropSelf(ModBlocks.ANTIMATTER_CABLE.get());
            dropSelf(ModBlocks.ANTIMATTER_ENERGY_BATTERY.get());
            dropSelf(ModBlocks.FLUID_PIPE.get());
            dropSelf(ModBlocks.FLUID_TANK.get());
            dropSelf(ModBlocks.CONTAINMENT_CELL.get());
            dropSelf(ModBlocks.MAGNETIC_CONDUIT.get());
            dropSelf(ModBlocks.ACCELERATOR_SEGMENT.get());
            dropSelf(ModBlocks.ACCELERATOR_PORT.get());
            dropSelf(ModBlocks.ACCELERATOR_CONTROLLER.get());
            add(ModBlocks.SOARYN_CHEST.get(), createNameableBlockEntityTable(ModBlocks.SOARYN_CHEST.get()));
            dropSelf(ModBlocks.PLASMA_GRANULATOR.get());
            dropSelf(ModBlocks.PLASMA_SMELTER.get());
            dropSelf(ModBlocks.CIRCUIT_ASSEMBLER.get());
            dropSelf(ModBlocks.ORE_WASHER.get());
            dropSelf(ModBlocks.PLASMA_ENERGY_BATTERY.get());
            dropSelf(ModBlocks.PLASMA_CABLE.get());

            // Natural stone drops its cobbled form (silk touch keeps it); slabs drop 2 when doubled.
            for (StoneSet set : ModBlocks.STONE_SETS) {
                var slabs = set.allSlabs();
                for (var block : set.all()) {
                    if (block == set.stone) {
                        add(block.get(), createSingleItemTableWithSilkTouch(block.get(), set.cobbled.get()));
                    } else if (slabs.contains(block)) {
                        add(block.get(), createSlabItemTable(block.get()));
                    } else {
                        dropSelf(block.get());
                    }
                }
            }
        }

        /** Every block we register must have a loot table, or datagen fails (catches forgotten ones). */
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().<Block>map(holder -> holder.value()).toList();
        }
    }
}
