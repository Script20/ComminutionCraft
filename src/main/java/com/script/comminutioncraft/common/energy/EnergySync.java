package com.script.comminutioncraft.common.energy;

/**
 * Menu data slots are sent as 16-bit shorts, so FE values above 32767 are split
 * into a low and a high half and joined again on the client.
 */
public final class EnergySync {
    public static int low(int value) {
        return value & 0xFFFF;
    }

    public static int high(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    public static int join(int low, int high) {
        return (low & 0xFFFF) | ((high & 0xFFFF) << 16);
    }

    private EnergySync() {}
}
