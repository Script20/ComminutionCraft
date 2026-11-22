package com.script.comminutioncraft.common.machine.miner;

import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

/**
 * The five Ore Miners (owner, 2026-11-18/22): a fixed square of chunks per tier, faster and hungrier each tier (numbers in
 * the server config, section {@code ore_miner}). Tier 3 and up can give ore blocks and take a filter with that tier's circuit.
 */
public enum OreMinerTier {
    ELECTRIC(3, null),
    PLASMA(5, () -> ModItems.PLASMA_CIRCUIT.get()),
    FUSION(7, () -> ModItems.FUSION_CIRCUIT.get()),
    ANTIMATTER(9, () -> ModItems.ANTIMATTER_CIRCUIT.get()),
    SINGULARITY(11, () -> ModItems.SINGULARITY_CIRCUIT.get());

    private final int width;
    /** A lambda, not the DeferredItem: ModBlocks builds the miners before ModItems exists. */
    private final @Nullable Supplier<Item> circuit;

    OreMinerTier(int width, @Nullable Supplier<Item> circuit) {
        this.width = width;
        this.circuit = circuit;
    }

    /** Chunks across (and deep): the area is width x width chunks. */
    public int width() {
        return width;
    }

    public int chunks() {
        return width * width;
    }

    /** Raw ore / ore block switch and the circuit filter (tier 3 and up). */
    public boolean hasOptions() {
        return circuit != null;
    }

    /** The circuit that unlocks the filter (null for the Electric miner). */
    public @Nullable Item circuit() {
        return circuit == null ? null : circuit.get();
    }

    public int ticksPerOre() {
        return MachineConfig.ORE_MINER_TICKS_PER_ORE[ordinal()].get();
    }

    public int energyPerOre() {
        return MachineConfig.ORE_MINER_ENERGY_PER_ORE[ordinal()].get();
    }

    public int energyCapacity() {
        return MachineConfig.ORE_MINER_ENERGY_CAPACITY[ordinal()].get();
    }

    public int maxEnergyInput() {
        return MachineConfig.ORE_MINER_MAX_ENERGY_INPUT[ordinal()].get();
    }
}
