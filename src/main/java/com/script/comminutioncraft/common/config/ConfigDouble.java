package com.script.comminutioncraft.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** A decimal {@link MachineConfig} value; reads its default until the config is loaded. */
public final class ConfigDouble {
    private final ModConfigSpec.DoubleValue value;

    ConfigDouble(ModConfigSpec.DoubleValue value) {
        this.value = value;
    }

    public double get() {
        return MachineConfig.loaded() ? value.getAsDouble() : value.getDefault();
    }

    public float getFloat() {
        return (float) get();
    }
}
