package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.registry.ModDataMaps;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;

/**
 * Default masses for the Event Horizon Reactor (everything else weighs 1): storage blocks weigh their 9 ingots,
 * a few rare things weigh a lot. Pack makers override it with their own data map file.
 */
public class ModDataMapProvider extends DataMapProvider {
    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    private void mass(Builder<Integer, Item> builder, Item item, int mass) {
        builder.add(BuiltInRegistries.ITEM.wrapAsHolder(item), mass, false);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<Integer, Item> mass = builder(ModDataMaps.MATTER_MASS);
        for (Item block : new Item[] {Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.COPPER_BLOCK, Items.DIAMOND_BLOCK,
                Items.EMERALD_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.COAL_BLOCK, Items.RAW_IRON_BLOCK,
                Items.RAW_GOLD_BLOCK, Items.RAW_COPPER_BLOCK}) {
            mass(mass, block, 9);
        }
        mass(mass, Items.NETHERITE_INGOT, 4);
        mass(mass, Items.NETHERITE_BLOCK, 36);
        mass(mass, Items.ANCIENT_DEBRIS, 4);
        mass(mass, Items.NETHER_STAR, 200);
        mass(mass, Items.DRAGON_EGG, 1000);
    }
}
