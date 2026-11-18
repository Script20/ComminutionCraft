package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.machine.upgrade.UpgradeType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A processing machine's input slot: takes anything but machine upgrades (they belong in the Upgrades tab, owner 2026-11-18). */
public class MachineInputSlot extends Slot {
    public MachineInputSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return UpgradeType.of(stack) == null;
    }
}
