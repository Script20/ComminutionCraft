package com.script.comminutioncraft.common.config;

import java.util.function.IntSupplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** A whole-number {@link MachineConfig} value; reads its default until the config is loaded. */
public final class ConfigInt implements IntSupplier {
    private final ModConfigSpec.IntValue value;

    ConfigInt(ModConfigSpec.IntValue value) {
        this.value = value;
    }

    public int get() {
        return MachineConfig.loaded() ? value.getAsInt() : value.getDefault();
    }

    /** Changes the loaded value (what the config screen does); used by GameTests. */
    public void set(int newValue) {
        value.set(newValue);
    }

    @Override
    public int getAsInt() {
        return get();
    }
}
