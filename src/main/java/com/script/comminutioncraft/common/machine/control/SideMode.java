package com.script.comminutioncraft.common.machine.control;

import net.minecraft.network.chat.Component;

/** What hoppers and pipes may do on one machine face. Energy is not affected. */
public enum SideMode {
    /** Items can be inserted (into the input, or the fuel slot for fuel). */
    INPUT("input", 0xFF40A0FF),
    /** Finished items (and empty buckets from the fuel slot) can be taken out. */
    OUTPUT("output", 0xFFFF9628),
    /** Closed to item transport. */
    NONE("none", 0xFF606470),
    /**
     * A second output, only on machines that make a by-product (the Electrolyser's oxygen): the by-product leaves
     * here, the main product through Output faces, so the two never share a pipe.
     */
    BYPRODUCT("byproduct", 0xFFB4E050);

    private final String name;
    /** Colour used for this mode in the side configuration screen. */
    public final int color;

    SideMode(String name, int color) {
        this.name = name;
        this.color = color;
    }

    /** Input -> Output -> (By-product, if the machine has one) -> None -> Input. */
    public SideMode next(boolean byproduct) {
        return switch (this) {
            case INPUT -> OUTPUT;
            case OUTPUT -> byproduct ? BYPRODUCT : NONE;
            case BYPRODUCT -> NONE;
            case NONE -> INPUT;
        };
    }

    public static SideMode byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NONE;
    }

    public Component displayName() {
        return Component.translatable("gui.comminutioncraft.side_mode." + name);
    }
}
