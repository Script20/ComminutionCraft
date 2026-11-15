package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.registry.ModDataMaps;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;

/**
 * Masses for the Event Horizon Reactor's black hole (owner, 2026-11-14): everything else weighs 1 (stone, cobble, dirt,
 * wood...). Values go up in powers of 2 with rarity; storage blocks weigh 9 x their ingot / gem so crafting a block
 * never gains or loses mass. Metals and gems are set through their {@code c:} tags (so other mods' tin, lead, silver...
 * count too), then single items override them: the generated file lists tag entries ({@code #c:...}) first and the
 * last matching entry wins. Pack makers override any of it with their own data map file.
 */
public class ModDataMapProvider extends DataMapProvider {
    /** Mass of one ingot (and of one raw ore, ore block, or one of our processed forms of that metal). */
    private static final Map<String, Integer> METALS = Map.ofEntries(
            Map.entry("copper", 8), Map.entry("tin", 8), Map.entry("zinc", 8), Map.entry("aluminum", 8),
            Map.entry("iron", 16), Map.entry("lead", 16), Map.entry("nickel", 16), Map.entry("osmium", 16),
            Map.entry("gold", 32), Map.entry("silver", 32),
            Map.entry("uranium", 64));

    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    private static void mass(Builder<Integer, Item> builder, ItemLike item, int mass) {
        builder.add(BuiltInRegistries.ITEM.wrapAsHolder(item.asItem()), mass, false);
    }

    private static void mass(Builder<Integer, Item> builder, TagKey<Item> tag, int mass) {
        builder.add(tag, mass, false);
    }

    private static TagKey<Item> c(String path) {
        return ProcessedMetal.commonItemTag(path);
    }

    /** The biggest power of 2 that 9 of don't outweigh the ingot (at least 1). */
    private static int nugget(int ingot) {
        int n = 1;
        while (n * 2 * 9 <= ingot) {
            n *= 2;
        }
        return n;
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<Integer, Item> mass = builder(ModDataMaps.MATTER_MASS);

        // ---- Metals: ingot, raw ore, ore block and our processed forms all weigh the ingot
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            int ingot = METALS.getOrDefault(metal.name(), 16);
            String m = metal.name();
            mass(mass, c("ingots/" + m), ingot);
            mass(mass, c("raw_materials/" + m), ingot);
            mass(mass, c("ores/" + m), ingot);
            mass(mass, c("dusts/" + m), ingot); // our granulated ore and other mods' dusts
            mass(mass, c("nuggets/" + m), nugget(ingot));
            mass(mass, c("storage_blocks/" + m), 9 * ingot);
            mass(mass, c("storage_blocks/raw_" + m), 9 * ingot);
            mass(mass, metal.washed().get(), ingot);
            mass(mass, metal.purified().get(), ingot);
            mass(mass, metal.concentrate().get(), ingot);
        }
        mass(mass, Items.COPPER_ORE, 32);           // drops 2-5 raw copper
        mass(mass, Items.DEEPSLATE_COPPER_ORE, 32);
        mass(mass, Items.NETHER_GOLD_ORE, 8);       // drops gold nuggets
        mass(mass, Items.NETHERITE_SCRAP, 128);
        mass(mass, Items.ANCIENT_DEBRIS, 128);
        mass(mass, c("ingots/netherite"), 1024);    // 4 scrap + 4 gold = 640, rounded up
        mass(mass, c("storage_blocks/netherite"), 9 * 1024);

        // ---- Gems and minerals (ores weigh roughly what they drop)
        gem(mass, "diamond", 128, 128);
        gem(mass, "emerald", 64, 64);
        gem(mass, "lapis", 32, 256);                // lapis ore drops 4-9
        gem(mass, "quartz", 8, 8);
        mass(mass, Items.QUARTZ_BLOCK, 32);         // 4 quartz
        mass(mass, c("gems/amethyst"), 8);
        mass(mass, Items.AMETHYST_BLOCK, 32);       // 4 shards
        mass(mass, c("dusts/redstone"), 4);
        mass(mass, c("storage_blocks/redstone"), 36);
        mass(mass, c("ores/redstone"), 16);          // drops 4-5
        mass(mass, c("dusts/glowstone"), 4);
        mass(mass, Items.GLOWSTONE, 16);             // 4 dust
        mass(mass, Items.COAL, 8);
        mass(mass, Items.COAL_BLOCK, 72);
        mass(mass, c("ores/coal"), 8);
        mass(mass, Items.CHARCOAL, 4);
        mass(mass, ModItems.TINY_COAL.get(), 1);     // 8 from one coal: must stay 1
        mass(mass, Items.OBSIDIAN, 4);
        mass(mass, Items.CRYING_OBSIDIAN, 8);
        mass(mass, Items.PRISMARINE_SHARD, 2);
        mass(mass, Items.PRISMARINE_CRYSTALS, 2);
        mass(mass, Items.ECHO_SHARD, 64);

        // ---- Mob drops and loot
        for (Item item : new Item[] {Items.SLIME_BALL, Items.GUNPOWDER, Items.FLINT, Items.CLAY_BALL}) {
            mass(mass, item, 2);
        }
        for (Item item : new Item[] {Items.PHANTOM_MEMBRANE, Items.BLAZE_POWDER}) {
            mass(mass, item, 8);
        }
        for (Item item : new Item[] {Items.ENDER_PEARL, Items.BLAZE_ROD, Items.BREEZE_ROD, Items.TRIAL_KEY}) {
            mass(mass, item, 16);
        }
        for (Item item : new Item[] {Items.ENDER_EYE, Items.GHAST_TEAR, Items.NAUTILUS_SHELL, Items.DRAGON_BREATH}) {
            mass(mass, item, 32);
        }
        for (Item item : new Item[] {Items.SHULKER_SHELL, Items.SNIFFER_EGG, Items.OMINOUS_TRIAL_KEY}) {
            mass(mass, item, 64);
        }
        mass(mass, Tags.Items.MUSIC_DISCS, 64);
        BuiltInRegistries.ITEM.keySet().stream() // every armor trim template (vanilla has no tag for them)
                .filter(id -> id.getNamespace().equals("minecraft") && id.getPath().endsWith("_armor_trim_smithing_template"))
                .forEach(id -> mass.add(id, 64, false));
        for (Item item : new Item[] {Items.GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.WITHER_SKELETON_SKULL, Items.TRIDENT,
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE}) {
            mass(mass, item, 256);
        }
        mass(mass, Items.HEART_OF_THE_SEA, 512);
        mass(mass, Items.HEAVY_CORE, 512);
        for (Item item : new Item[] {Items.ELYTRA, Items.ENCHANTED_GOLDEN_APPLE, Items.CONDUIT}) {
            mass(mass, item, 1024);
        }
        mass(mass, Items.NETHER_STAR, 2048);
        mass(mass, Items.BEACON, 2048);
        mass(mass, Items.DRAGON_EGG, 4096);

        // ---- Our parts
        mass(mass, ModItems.ELECTRONIC_CIRCUIT.get(), 32);  // 2 redstone + 3 copper
        mass(mass, ModItems.PLASMA_CIRCUIT.get(), 64);
        mass(mass, ModItems.REACTOR_PLATING.get(), 64);     // an iron block makes 2
        mass(mass, ModItems.FUSION_CIRCUIT.get(), 128);
        mass(mass, ModItems.FUSION_FUEL_PELLET.get(), 128);
        mass(mass, ModItems.ANTIMATTER_CIRCUIT.get(), 512);
        mass(mass, ModItems.SINGULARITY_SHARD.get(), 1024);
        mass(mass, ModItems.SINGULARITY_CIRCUIT.get(), 2048);

        // Tiny Coal: an eighth of coal's 1,600 ticks, i.e. one item in a furnace
        builder(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.FURNACE_FUELS)
                .add(ModItems.TINY_COAL.getId(), new net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel(200), false);

        // An axe strips Purpleheart logs and wood
        var strip = builder(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.STRIPPABLES);
        var wood = com.script.comminutioncraft.common.registry.ModBlocks.PURPLEHEART;
        strip.add(wood.log.getId(), new net.neoforged.neoforge.registries.datamaps.builtin.Strippable(wood.strippedLog.get()), false);
        strip.add(wood.wood.getId(), new net.neoforged.neoforge.registries.datamaps.builtin.Strippable(wood.strippedWood.get()), false);
    }

    /** A gem: the gem, its block (9 x) and its ore (roughly what it drops). */
    private static void gem(Builder<Integer, Item> mass, String name, int gem, int ore) {
        mass(mass, c("gems/" + name), gem);
        mass(mass, c("storage_blocks/" + name), 9 * gem);
        mass(mass, c("ores/" + name), ore);
    }
}
