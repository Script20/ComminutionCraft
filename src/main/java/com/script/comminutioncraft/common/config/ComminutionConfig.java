package com.script.comminutioncraft.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The mod's common config ({@code config/comminutioncraft-common.toml}), editable in game from the Mods menu
 * (NeoForge's ConfigurationScreen; labels are {@code comminutioncraft.configuration.*} lang keys).
 */
public final class ComminutionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SOARYN_CHEST_INCLUDE_HOTBAR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("comminutioncraft.configuration.soaryn_chest").push("soaryn_chest");
        SOARYN_CHEST_INCLUDE_HOTBAR = builder
                .comment("If true, placing a Soaryn Chest also takes the hotbar (slots 0-8), not just the main inventory.")
                .translation("comminutioncraft.configuration.includeHotbar")
                .define("includeHotbar", false);
        builder.pop();
        SPEC = builder.build();
    }

    private ComminutionConfig() {}
}
