package com.script.comminutioncraft.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Turns the stone around underground lava into a crust of another block (volcanic basalt): every lava block
 * touching non-lava within {@link Config#searchRadius()} of the origin gets a rough ball of crust stamped round it,
 * replacing only blocks in {@link Config#replaceable()}. Does nothing (cheaply) if there's no lava nearby.
 * <p>
 * Runs in the UNDERGROUND_DECORATION step, after lakes and aquifers have placed their lava.
 * Reach is search radius + max crust radius, which must stay under 16 so features only touch neighbouring chunks.
 */
public class LavaCrustFeature extends Feature<LavaCrustFeature.Config> {
    public record Config(BlockState crust, HolderSet<Block> replaceable, int searchRadius, float minThickness, float maxThickness)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockState.CODEC.fieldOf("crust").forGetter(Config::crust),
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable").forGetter(Config::replaceable),
                Codec.intRange(1, 10).fieldOf("search_radius").forGetter(Config::searchRadius),
                Codec.floatRange(0.5F, 4.0F).fieldOf("min_thickness").forGetter(Config::minThickness),
                Codec.floatRange(0.5F, 4.0F).fieldOf("max_thickness").forGetter(Config::maxThickness)
        ).apply(i, Config::new));
    }

    public LavaCrustFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        Config config = context.config();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int r = config.searchRadius();

        // Cheap probe on a 2-block grid first: most attempts find no lava and stop here.
        if (!anyLava(level, origin, r)) {
            return false;
        }

        List<BlockPos> edges = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    pos.setWithOffset(origin, dx, dy, dz);
                    if (isLava(level, pos) && touchesNonLava(level, pos)) {
                        edges.add(pos.immutable());
                    }
                }
            }
        }

        boolean placed = false;
        for (BlockPos lava : edges) {
            float thickness = config.minThickness() + random.nextFloat() * (config.maxThickness() - config.minThickness());
            int reach = (int) Math.ceil(thickness);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dy = -reach; dy <= reach; dy++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        if (dx * dx + dy * dy + dz * dz > thickness * thickness) {
                            continue;
                        }
                        pos.setWithOffset(lava, dx, dy, dz);
                        if (level.getBlockState(pos).is(config.replaceable())) {
                            level.setBlock(pos, config.crust(), Block.UPDATE_CLIENTS);
                            placed = true;
                        }
                    }
                }
            }
        }
        return placed;
    }

    private static boolean anyLava(WorldGenLevel level, BlockPos origin, int r) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx += 2) {
            for (int dy = -r; dy <= r; dy += 2) {
                for (int dz = -r; dz <= r; dz += 2) {
                    // A pool 1 block deep can slip between grid points vertically, so check the block above too.
                    pos.setWithOffset(origin, dx, dy, dz);
                    if (isLava(level, pos) || isLava(level, pos.move(Direction.UP))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isLava(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.LAVA);
    }

    private static boolean touchesNonLava(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!isLava(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
