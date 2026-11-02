package com.script.comminutioncraft.common.machine.control;

import net.minecraft.network.chat.Component;

/** What hoppers and pipes may do on one machine face. Energy is not affected. */
public enum SideMode {
    /** Items can be inserted (into the input, or the fuel slot for fuel). */
    INPUT("input", 0xFF40A0FF),
    /** Finished items (and empty buckets from the fuel slot) can be taken out. */
    OUTPUT("output", 0xFFFF9628),
    /** Closed to item transport. */
    NONE("none", 0xFF606470);

    private final String name;
    /** Colour used for this mode in the side configuration screen. */
    public final int color;

    SideMode(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public SideMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static SideMode byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NONE;
    }

    public Component displayName() {
        return Component.translatable("gui.comminutioncraft.side_mode." + name);
    }
}
