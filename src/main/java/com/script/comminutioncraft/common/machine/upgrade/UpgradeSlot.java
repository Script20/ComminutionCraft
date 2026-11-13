package com.script.comminutioncraft.common.machine.upgrade;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One upgrade slot in a machine menu. On the client it only shows (and takes clicks) while the Upgrades panel is open
 * ({@link #panelOpen}, set by the GUI); on the server it's always there.
 */
public class UpgradeSlot extends Slot {
    /** Where the panel's first slot sits, relative to the GUI's top-left (the panel is right of the tabs). */
    public static final int X = 205;
    public static final int Y = 23;
    public static final int STEP = 21;

    /** Client only: whether the Upgrades panel is open (it stays open between machines until closed). */
    public static boolean panelOpen;

    private final boolean clientSide;

    public UpgradeSlot(Container container, UpgradeType type, boolean clientSide) {
        super(container, type.ordinal(), X, Y + STEP * type.ordinal());
        this.clientSide = clientSide;
    }

    public UpgradeType type() {
        return UpgradeType.values()[getContainerSlot()];
    }

    @Override
    public boolean isActive() {
        return !clientSide || panelOpen;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }

    @Override
    public int getMaxStackSize() {
        return UpgradeType.MAX;
    }
}
