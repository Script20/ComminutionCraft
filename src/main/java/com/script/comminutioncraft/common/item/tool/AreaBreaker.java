package com.script.comminutioncraft.common.item.tool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Breaking extra blocks as the player (3x3, vein mining, tree felling) without the tool reacting to its own breaks. */
public final class AreaBreaker {
    private static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);
    /** While set, block drops and XP go to this player instead of where the block was. */
    private static final ThreadLocal<ServerPlayer> COLLECTOR = new ThreadLocal<>();

    /** Runs {@code breaking} with every drop and XP orb landing on the player's head. */
    static void dropsTo(ServerPlayer player, Runnable breaking) {
        COLLECTOR.set(player);
        try {
            breaking.run();
        } finally {
            COLLECTOR.remove();
        }
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        ServerPlayer player = COLLECTOR.get();
        if (player == null) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            drop.setPos(player.getX(), player.getEyeY(), player.getZ());
            drop.setDeltaMovement(Vec3.ZERO);
            drop.setNoPickUpDelay();
        }
        int xp = event.getDroppedExperience();
        if (xp > 0) {
            event.setDroppedExperience(0);
            ExperienceOrb.award(event.getLevel(), new Vec3(player.getX(), player.getEyeY(), player.getZ()), xp);
        }
    }

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
