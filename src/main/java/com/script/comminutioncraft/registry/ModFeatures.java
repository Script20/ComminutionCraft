package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.worldgen.LavaCrustFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** World-gen feature types. Their configured/placed versions and biome modifiers are datagen'd in ModWorldGen. */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ComminutionCraft.MODID);

    public static final DeferredHolder<Feature<?>, LavaCrustFeature> LAVA_CRUST = FEATURES.register("lava_crust", LavaCrustFeature::new);

    private ModFeatures() {}
}
