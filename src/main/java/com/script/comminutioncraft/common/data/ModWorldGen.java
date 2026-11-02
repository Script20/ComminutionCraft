package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModFeatures;
import com.script.comminutioncraft.common.worldgen.LavaCrustFeature;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World generation, as datapack registry entries (written by runData into data/comminutioncraft/worldgen and
 * data/comminutioncraft/neoforge/biome_modifier).
 * <ul>
 *   <li>Marble: exactly like vanilla granite/diorite - 64-block blobs in overworld stone, once every 6 chunks
 *       between y 64 and 128, and twice per chunk between y 0 and 60.</li>
 *   <li>Volcanic basalt: a 1.5-3 block crust around underground lava (lava lakes and the deep lava aquifers),
 *       tried 16 times per chunk between the bottom of the world and y 48.</li>
 * </ul>
 */
public final class ModWorldGen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_MARBLE = configured("ore_marble");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VOLCANIC_BASALT_CRUST = configured("volcanic_basalt_crust");

    public static final ResourceKey<PlacedFeature> ORE_MARBLE_UPPER = placed("ore_marble_upper");
    public static final ResourceKey<PlacedFeature> ORE_MARBLE_LOWER = placed("ore_marble_lower");
    public static final ResourceKey<PlacedFeature> VOLCANIC_BASALT_CRUST_PLACED = placed("volcanic_basalt_crust");

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldGen::configuredFeatures)
            .add(Registries.PLACED_FEATURE, ModWorldGen::placedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGen::biomeModifiers);

    private static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        context.register(ORE_MARBLE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(
                new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD), ModBlocks.MARBLE.stone.get().defaultBlockState(), 64)));
        context.register(VOLCANIC_BASALT_CRUST, new ConfiguredFeature<>(ModFeatures.LAVA_CRUST.get(), new LavaCrustFeature.Config(
                ModBlocks.VOLCANIC_BASALT.stone.get().defaultBlockState(),
                blocks.getOrThrow(BlockTags.BASE_STONE_OVERWORLD), 8, 1.5F, 3.0F)));
    }

    private static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        // Same placement as vanilla's ore_granite_upper / ore_granite_lower
        context.register(ORE_MARBLE_UPPER, new PlacedFeature(features.getOrThrow(ORE_MARBLE), List.of(
                RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128)), BiomeFilter.biome())));
        context.register(ORE_MARBLE_LOWER, new PlacedFeature(features.getOrThrow(ORE_MARBLE), List.of(
                CountPlacement.of(2), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60)), BiomeFilter.biome())));
        context.register(VOLCANIC_BASALT_CRUST_PLACED, new PlacedFeature(features.getOrThrow(VOLCANIC_BASALT_CRUST), List.<PlacementModifier>of(
                CountPlacement.of(16), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(48)), BiomeFilter.biome())));
    }

    private static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        context.register(biomeModifier("add_marble"), new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ORE_MARBLE_UPPER), placed.getOrThrow(ORE_MARBLE_LOWER)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(biomeModifier("add_volcanic_basalt_crust"), new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(VOLCANIC_BASALT_CRUST_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_DECORATION));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ComminutionCraft.id(name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ComminutionCraft.id(name));
    }

    private static ResourceKey<BiomeModifier> biomeModifier(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ComminutionCraft.id(name));
    }

    private ModWorldGen() {}
}
