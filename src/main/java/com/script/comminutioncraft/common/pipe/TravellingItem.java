package com.script.comminutioncraft.common.pipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * A stack moving through one Item Pipe: it came in through {@code from}, and {@code route} lists the side it leaves
 * this pipe by, then the next pipe's, and so on; the last one faces the block it goes into. {@code progress} runs
 * 0 (at the {@code from} face) through 0.5 (the middle) to 1 (the exit face). It remembers where it was pulled from,
 * so it can go back there when nothing else takes it.
 */
public final class TravellingItem {
    public static final Codec<TravellingItem> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(t -> t.stack),
            Direction.CODEC.fieldOf("from").forGetter(t -> t.from),
            Direction.CODEC.listOf().fieldOf("route").forGetter(t -> t.route),
            Codec.FLOAT.fieldOf("progress").forGetter(t -> t.progress),
            BlockPos.CODEC.fieldOf("source").forGetter(t -> t.source),
            Direction.CODEC.fieldOf("source_side").forGetter(t -> t.sourceSide)
    ).apply(i, TravellingItem::new));

    final ItemStack stack;
    final Direction from;
    final List<Direction> route;
    float progress;
    final BlockPos source;
    final Direction sourceSide;

    TravellingItem(ItemStack stack, Direction from, List<Direction> route, float progress, BlockPos source, Direction sourceSide) {
        this.stack = stack;
        this.from = from;
        this.route = List.copyOf(route);
        this.progress = progress;
        this.source = source;
        this.sourceSide = sourceSide;
    }

    public ItemStack stack() {
        return stack;
    }

    public Direction from() {
        return from;
    }

    /** The side it leaves this pipe by. */
    public Direction to() {
        return route.isEmpty() ? from.getOpposite() : route.getFirst();
    }

    public float progress() {
        return progress;
    }

    /** The same stack carried on into the next pipe (entering through the side facing this one). */
    TravellingItem next(float carry) {
        Direction exit = to();
        return new TravellingItem(stack, exit.getOpposite(), route.subList(1, route.size()), carry, source, sourceSide);
    }

    /** The same stack (or what's left of it) sent another way from the middle of this pipe. */
    TravellingItem rerouted(ItemStack left, List<Direction> newRoute) {
        return new TravellingItem(left, from, newRoute, 0.5F, source, sourceSide);
    }
}
