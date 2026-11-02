package com.script.comminutioncraft.common.machine.control;

/** A machine menu that exposes the synced redstone and side settings to its screen. */
public interface ControllableMenu {
    RedstoneMode getRedstoneMode();

    SideMode getSideMode(RelativeSide side);
}
