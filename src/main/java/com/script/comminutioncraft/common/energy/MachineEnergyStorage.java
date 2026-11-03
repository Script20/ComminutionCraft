package com.script.comminutioncraft.common.energy;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * FE buffer for a machine. Other blocks go through {@link #insert}/{@link #extract} (with their per-call limits);
 * the machine itself uses {@link #consume}/{@link #generate}, which ignore those limits.
 */
public class MachineEnergyStorage extends SimpleEnergyHandler {
    private final Runnable onChanged;

    public MachineEnergyStorage(int capacity, int maxInsert, int maxExtract, Runnable onChanged) {
        super(capacity, maxInsert, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChanged.run();
    }

    public int getEnergy() {
        return energy;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return capacity - energy;
    }

    /** Puts the stored FE on the item a machine becomes when picked up with the wrench. */
    public void saveTo(DataComponentMap.Builder components) {
        if (energy > 0) {
            components.set(ModDataComponents.STORED_ENERGY.get(), energy);
        }
    }

    /** Restores the FE from the item the machine was placed from, if it has any. */
    public void loadFrom(DataComponentGetter components) {
        Integer saved = components.get(ModDataComponents.STORED_ENERGY.get());
        if (saved != null) {
            set(Math.clamp(saved, 0, capacity));
        }
    }

    /** Uses {@code amount} FE if it's all there; otherwise uses nothing. Call outside of transactions (e.g. in a tick). */
    public boolean consume(int amount) {
        if (energy < amount) {
            return false;
        }
        set(energy - amount);
        return true;
    }

    /** Adds up to {@code amount} FE, returning how much fit. Call outside of transactions (e.g. in a tick). */
    public int generate(int amount) {
        int added = Math.min(getSpace(), amount);
        if (added > 0) {
            set(energy + added);
        }
        return added;
    }
}
