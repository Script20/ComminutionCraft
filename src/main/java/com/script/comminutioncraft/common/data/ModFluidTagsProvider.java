package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.level.material.Fluid;

/**
 * {@code #comminutioncraft:gases}: what the Gas Upgrade saves (everything else is a liquid for the Liquid Upgrade).
 * Datapack-editable, so packs can move other mods' gases in.
 */
public class ModFluidTagsProvider extends KeyTagProvider<Fluid> {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.FLUID, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MachineUpgrades.GASES).add(
                ModFluids.HYDROGEN.getKey(), ModFluids.FLOWING_HYDROGEN.getKey(),
                ModFluids.OXYGEN.getKey(), ModFluids.FLOWING_OXYGEN.getKey(),
                ModFluids.PLASMA.getKey(), ModFluids.FLOWING_PLASMA.getKey(),
                ModFluids.DEUTERIUM.getKey(), ModFluids.FLOWING_DEUTERIUM.getKey(),
                ModFluids.TRITIUM.getKey(), ModFluids.FLOWING_TRITIUM.getKey());
    }
}
