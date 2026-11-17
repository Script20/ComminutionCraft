package com.script.comminutioncraft.common.pipe;

import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/** Finding where items can go through connected Item Pipes (a breadth-first search, so nearest first). */
final class ItemPipeRouting {
    /** Most pipes one search walks through. */
    private static final int MAX_PIPES = 4096;

    /** A side of a pipe that takes the item: how far away, the route to it, how many it would take now. */
    record Target(BlockPos pipe, Direction side, int distance, List<Direction> route, int fits) {}

    /** Which pipe sides count as targets (the side's own settings are passed in). */
    @FunctionalInterface
    interface Accepts {
        boolean test(BlockPos pipe, Direction side, PipeSide settings);
    }

    /** Every Insert side (not {@code skip}) that lets the stack through its filter and has room for some of it. */
    static List<Target> insertTargets(Level level, BlockPos start, ItemStack stack, @Nullable BlockPos skipPipe,
                                      @Nullable Direction skipSide) {
        return search(level, start, stack, (pipe, side, settings) -> !settings.isExtract() && settings.passes(stack)
                && !(pipe.equals(skipPipe) && side == skipSide));
    }

    /** The route to one particular side (whatever its mode), if it's reachable and has room. */
    static @Nullable Target to(Level level, BlockPos start, ItemStack stack, BlockPos pipe, Direction side) {
        List<Target> found = search(level, start, stack, (p, s, settings) -> p.equals(pipe) && s == side);
        return found.isEmpty() ? null : found.getFirst();
    }

    private static List<Target> search(Level level, BlockPos start, ItemStack stack, Accepts accepts) {
        List<Target> targets = new ArrayList<>();
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        Map<BlockPos, Direction> step = new HashMap<>();
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        distance.put(start, 0);
        while (!queue.isEmpty() && distance.size() <= MAX_PIPES) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ItemPipeBlock) || !(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                CableConnection connection = state.getValue(ConduitBlock.CONNECTIONS.get(direction));
                if (connection == CableConnection.MACHINE && accepts.test(pos, direction, pipe.side(direction))) {
                    int fits = simulateInsert(level, pos, direction, stack);
                    if (fits > 0) {
                        targets.add(new Target(pos, direction, distance.get(pos), route(pos, direction, parent, step), fits));
                    }
                } else if (connection == CableConnection.CABLE) {
                    BlockPos next = pos.relative(direction);
                    if (!distance.containsKey(next)) {
                        distance.put(next, distance.get(pos) + 1);
                        parent.put(next, pos);
                        step.put(next, direction);
                        queue.add(next);
                    }
                }
            }
        }
        return targets;
    }

    /** The exit sides from the search's start to {@code pipe}, then {@code side}. */
    private static List<Direction> route(BlockPos pipe, Direction side, Map<BlockPos, BlockPos> parent, Map<BlockPos, Direction> step) {
        List<Direction> route = new ArrayList<>();
        route.add(side);
        for (BlockPos pos = pipe; parent.containsKey(pos); pos = parent.get(pos)) {
            route.add(step.get(pos));
        }
        return route.reversed();
    }

    /** How many of the stack the block on {@code side} of the pipe would take right now (nothing is moved). */
    static int simulateInsert(Level level, BlockPos pipe, Direction side, ItemStack stack) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pipe.relative(side), side.getOpposite());
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            return handler.insert(ItemResource.of(stack), stack.getCount(), transaction); // not committed: rolled back
        }
    }

    private ItemPipeRouting() {}
}
