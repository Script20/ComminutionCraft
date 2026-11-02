package com.script.comminutioncraft.cable;

import net.minecraft.util.StringRepresentable;

/** What a cable's arm on one side is doing. Stored per side in the blockstate. */
public enum CableConnection implements StringRepresentable {
    /** Nothing to connect to. */
    NONE("none"),
    /** Joined to another cable. */
    CABLE("cable"),
    /** Joined to a block that stores or uses FE (drawn with a connector plate). */
    MACHINE("machine"),
    /** Cut with the wrench: never connects until the wrench restores it. */
    DISABLED("disabled");

    private final String name;

    CableConnection(String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return this == CABLE || this == MACHINE;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
