package com.script.comminutioncraft.common.machine.upgrade;

import com.script.comminutioncraft.common.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** The four machine upgrades, in their slot order (owner-approved 2026-11-13, design/previews/upgrades.png). */
public enum UpgradeType {
    SPEED("speed", 0xFF3CE6BE, () -> ModItems.SPEED_UPGRADE.get()),
    ENERGY("energy", 0xFFFF6050, () -> ModItems.ENERGY_UPGRADE.get()),
    GAS("gas", 0xFFC8BEFF, () -> ModItems.GAS_UPGRADE.get()),
    LIQUID("liquid", 0xFF408CFF, () -> ModItems.LIQUID_UPGRADE.get());

    /** Most of one upgrade a machine can hold. */
    public static final int MAX = 8;

    private final String name;
    private final int colour;
    private final Supplier<Item> item;

    UpgradeType(String name, int colour, Supplier<Item> item) {
        this.name = name;
        this.colour = colour;
        this.item = item;
    }

    public String id() {
        return name;
    }

    /** ARGB, for the GUI text. */
    public int colour() {
        return colour;
    }

    public Item item() {
        return item.get();
    }

    public static @Nullable UpgradeType of(ItemStack stack) {
        for (UpgradeType type : values()) {
            if (stack.is(type.item())) {
                return type;
            }
        }
        return null;
    }
}
