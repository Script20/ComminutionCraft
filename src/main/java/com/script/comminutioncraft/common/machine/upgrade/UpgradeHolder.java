package com.script.comminutioncraft.common.machine.upgrade;

/** A machine with upgrade slots (every FE processing and fluid machine; not generators, batteries or multiblocks). */
public interface UpgradeHolder {
    MachineUpgrades getUpgrades();
}
