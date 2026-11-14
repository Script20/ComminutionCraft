package com.script.comminutioncraft.common.worldgen;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/** Tree feature keys (the features themselves are datagen'd in ModWorldGen) and the saplings' growers. */
public final class ModTrees {
    /** Purpleheart (owner, 2026-11-14): tall and straight, a 9-14 block trunk under a rounded crown; grows in jungles. */
    public static final ResourceKey<ConfiguredFeature<?, ?>> PURPLEHEART = ResourceKey.create(Registries.CONFIGURED_FEATURE,
            ComminutionCraft.id("purpleheart"));

    public static final TreeGrower PURPLEHEART_GROWER = new TreeGrower(ComminutionCraft.MODID + ":purpleheart",
            Optional.empty(), Optional.of(PURPLEHEART), Optional.empty());

    private ModTrees() {}
}
