package com.script.comminutioncraft.common.compat.patchouli;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;

/** Everything that touches Patchouli's classes; only called when Patchouli is loaded (see {@link GuideBook}). */
final class PatchouliBridge {
    static void give(ServerPlayer player, Identifier book) {
        ItemStack stack = PatchouliAPI.get().getBookStack(book);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private PatchouliBridge() {}
}
