package com.script.comminutioncraft.common.machine.control;

import net.minecraft.network.chat.Component;

/** When a machine is allowed to run, based on the redstone signal reaching it. Cycled with the GUI's redstone tab. */
public enum RedstoneMode {
    /** Always run. */
    IGNORE("ignore"),
    /** Run only while powered. */
    HIGH("high"),
    /** Run only while not powered. */
    LOW("low");

    private final String name;

    RedstoneMode(String name) {
        this.name = name;
    }

    public boolean allows(boolean powered) {
        return switch (this) {
            case IGNORE -> true;
            case HIGH -> powered;
            case LOW -> !powered;
        };
    }

    public RedstoneMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static RedstoneMode byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : IGNORE;
    }

    public String getSerializedName() {
        return name;
    }

    public Component displayName() {
        return Component.translatable("gui.comminutioncraft.redstone." + name);
    }

    public Component description() {
        return Component.translatable("gui.comminutioncraft.redstone." + name + ".desc");
    }
}
