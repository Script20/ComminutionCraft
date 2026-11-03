package com.script.comminutioncraft.common.cable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Finds and caches groups of connected conduits (cables or pipes; a network is one conduit block). A network is rebuilt lazily the next time power flows through it;
 * any cable change anywhere in the level simply throws that level's cache away, which keeps this simple and correct.
 */
public final class CableNetworks {
    /** Stops a runaway search on absurdly large cable builds. */
    private static final int MAX_CABLES = 8192;

    private static final Map<Level, Map<BlockPos, Network>> CACHE = new WeakHashMap<>();

    /** A block that stores/uses FE, touching the network: its position and the face the cable touches. */
    public record Endpoint(BlockPos pos, Direction face) {}

    public static final class Network {
        private final List<Endpoint> endpoints;
        /** Set while power is being handed out, so a machine that pushes straight back can't recurse. */
        boolean busy;
        /** What has gone through this network this tick (energy cables cap it per tick). */
        final TickBudget budget = new TickBudget();

        Network(List<Endpoint> endpoints) {
            this.endpoints = endpoints;
        }

        public List<Endpoint> endpoints() {
            return endpoints;
        }
    }

    /**
     * Amount moved through a network during one game tick. It's a transaction journal, so a simulated or cancelled
     * insert gives its share back.
     */
    public static final class TickBudget extends SnapshotJournal<long[]> {
        private long tick = Long.MIN_VALUE;
        private int used;

        /** How much of {@code limit} is left this tick. */
        public int remaining(long gameTime, int limit) {
            return tick == gameTime ? Math.max(0, limit - used) : limit;
        }

        public void use(long gameTime, int amount, TransactionContext transaction) {
            if (amount <= 0) {
                return;
            }
            updateSnapshots(transaction);
            if (tick != gameTime) {
                tick = gameTime;
                used = 0;
            }
            used += amount;
        }

        @Override
        protected long[] createSnapshot() {
            return new long[] {tick, used};
        }

        @Override
        protected void revertToSnapshot(long[] snapshot) {
            tick = snapshot[0];
            used = (int) snapshot[1];
        }
    }

    public static synchronized Network get(Level level, BlockPos cablePos) {
        Map<BlockPos, Network> byPos = CACHE.computeIfAbsent(level, l -> new HashMap<>());
        Network network = byPos.get(cablePos);
        if (network == null) {
            Set<BlockPos> cables = new HashSet<>();
            network = build(level, cablePos.immutable(), cables);
            for (BlockPos pos : cables) {
                byPos.put(pos, network);
            }
        }
        return network;
    }

    public static synchronized void invalidate(LevelReader level) {
        if (level instanceof Level realLevel) {
            CACHE.remove(realLevel);
        }
    }

    private static Network build(Level level, BlockPos start, Set<BlockPos> cables) {
        Block conduit = level.getBlockState(start).getBlock();
        List<Endpoint> endpoints = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        cables.add(start);
        while (!queue.isEmpty() && cables.size() < MAX_CABLES) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!(conduit instanceof ConduitBlock) || !state.is(conduit)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                CableConnection connection = state.getValue(ConduitBlock.CONNECTIONS.get(direction));
                BlockPos next = pos.relative(direction);
                if (connection == CableConnection.CABLE && level.isLoaded(next) && cables.add(next)) {
                    queue.add(next);
                } else if (connection == CableConnection.MACHINE) {
                    endpoints.add(new Endpoint(next, direction.getOpposite()));
                }
            }
        }
        return new Network(List.copyOf(endpoints));
    }

    private CableNetworks() {}
}
