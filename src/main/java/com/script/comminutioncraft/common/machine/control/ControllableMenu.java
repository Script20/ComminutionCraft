package com.script.comminutioncraft.common.machine.control;

/** A machine menu that exposes the synced redstone and side settings to its screen. */
public interface ControllableMenu {
    RedstoneMode getRedstoneMode();

    SideMode getSideMode(RelativeSide side);

    /** Whether its faces can be set to By-product (shown in the side config legend). */
    default boolean hasByproductOutput() {
        return false;
    }
}
