package com.script.comminutioncraft.common.machine.control;

import com.script.comminutioncraft.common.machine.upgrade.UpgradeType;
import org.jspecify.annotations.Nullable;

/** A machine menu that exposes the synced redstone and side settings to its screen. */
public interface ControllableMenu {
    RedstoneMode getRedstoneMode();

    SideMode getSideMode(RelativeSide side);

    /** Whether its faces can be set to By-product (shown in the side config legend). */
    default boolean hasByproductOutput() {
        return false;
    }

    /** Index of the first of the four upgrade slots (Speed, Energy, Gas, Liquid), or -1 if it takes no upgrades. */
    default int upgradeSlotStart() {
        return -1;
    }

    /**
     * The block id that names the machine's textures (the side config cube), or null when it's the menu's own id. Menus
     * shared by several blocks (factories, ore miners) say which one is open.
     */
    default @Nullable String blockName() {
        return null;
    }

    /** Whether this machine takes that upgrade (Gas / Liquid only where it uses one). */
    default boolean takesUpgrade(UpgradeType type) {
        return upgradeSlotStart() >= 0;
    }
}
