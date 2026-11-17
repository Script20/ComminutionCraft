package com.script.comminutioncraft.common.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A menu with "ghost" slots (they show an item but hold none): set from JEI through {@code SetGhostItemPayload}. */
public interface GhostItemMenu {
    /** Sets (or, with an empty stack, clears) the ghost slot at menu slot index {@code slot}; server side. */
    void setGhost(Player player, int slot, ItemStack stack);
}
