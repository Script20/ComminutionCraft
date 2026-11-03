package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
            add(ModBlocks.ENERGY_BATTERY.get(), this::keepStoredEnergy);

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

        /** Drops the block with its FE still in it (like a shulker box keeps its items). */
        private LootTable.Builder keepStoredEnergy(Block block) {
            return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(block)
                            .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                    .include(ModDataComponents.STORED_ENERGY.get())))));
        }

        /** Every block we register must have a loot table, or datagen fails (catches forgotten ones). */
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().<Block>map(holder -> holder.value()).toList();
        }
    }
}
