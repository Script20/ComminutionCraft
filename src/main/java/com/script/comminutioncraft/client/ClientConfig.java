package com.script.comminutioncraft.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Per-player settings ({@code config/comminutioncraft-client.toml}), also on the Mods menu config screen.
 */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MACHINE_SOUND_VOLUME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("comminutioncraft.configuration.sounds").push("sounds");
        MACHINE_SOUND_VOLUME = builder
                .comment("Volume of the machines' working sounds and the black hole, in percent (on top of Minecraft's Blocks volume).")
                .translation("comminutioncraft.configuration.machineSoundVolume")
                .defineInRange("machineSoundVolume", 100, 0, 100);
        builder.pop();
        SPEC = builder.build();
    }

    /** 0..1, or 1 before the config has loaded. */
    public static float machineVolume() {
        try {
            return MACHINE_SOUND_VOLUME.get() / 100F;
        } catch (IllegalStateException notLoaded) {
            return 1F;
        }
    }

    private ClientConfig() {}
}
