package com.script.comminutioncraft.machine;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/** Small inventory helpers shared by every machine. */
public final class MachineSlots {
    /** Whether {@code result} can be added to the stack in {@code slot} without overflowing. */
    public static boolean canFit(NonNullList<ItemStack> items, int slot, ItemStack result, int containerMaxStackSize) {
        ItemStack output = items.get(slot);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        int max = Math.min(containerMaxStackSize, output.getMaxStackSize());
        return output.getCount() + result.getCount() <= max;
    }

    public static void addTo(NonNullList<ItemStack> items, int slot, ItemStack result) {
        ItemStack output = items.get(slot);
        if (output.isEmpty()) {
            items.set(slot, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    /** Uses up one fuel item, leaving its remainder behind (e.g. an empty bucket from a lava bucket). */
    public static void consumeFuel(NonNullList<ItemStack> items, int slot) {
        ItemStack fuel = items.get(slot);
        // Grab the remainder before shrinking.
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(slot, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    private MachineSlots() {}
}
