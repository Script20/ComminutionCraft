package com.script.comminutioncraft.common.item.tool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Breaking extra blocks as the player (3x3, vein mining, tree felling) without the tool reacting to its own breaks. */
final class AreaBreaker {
    private static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);

    /** True while we're breaking the extra blocks (so the tool doesn't start another area from them). */
    static boolean busy() {
        return BUSY.get();
    }

    /** Breaks the block as the player would: events, protection, drops, and the held tool's own wear / FE. */
    static boolean breakAs(ServerPlayer player, BlockPos pos) {
        BUSY.set(true);
        try {
            return player.gameMode.destroyBlock(pos);
        } finally {
            BUSY.set(false);
        }
    }

    /**
     * Positions connected to {@code start} through matching blocks (diagonals count), nearest first, at most
     * {@code max}; {@code start} itself isn't included.
     */
    static List<BlockPos> connected(BlockPos start, Predicate<BlockPos> matches, int max) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>(List.of(start.immutable()));
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(start.immutable()));
        while (!queue.isEmpty() && found.size() < max) {
            BlockPos pos = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockPos p = next.immutable();
                if (found.size() < max && seen.add(p) && matches.test(p)) {
                    found.add(p);
                    queue.add(p);
                }
            }
        }
        return found;
    }

    private AreaBreaker() {}
}
