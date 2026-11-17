package com.script.comminutioncraft.common.pipe;

import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * One side of an Item Pipe that touches a block: Insert (default) or Extract (None = the side is cut, see
 * {@link ItemPipeBlock}), the redstone mode for Extract (starts at High), and, with an Electronic Circuit in, a 9-item
 * filter (whitelist or blacklist) and, on Extract, round robin instead of closest first.
 */
public final class PipeSide {
    public static final int FILTER_SLOTS = 9;

    boolean extract;
    RedstoneMode redstone = RedstoneMode.HIGH;
    ItemStack circuit = ItemStack.EMPTY;
    final NonNullList<ItemStack> filter = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
    boolean blacklist;
    /** With a circuit: round robin (true, the default) or closest first. Without one it's always closest first. */
    boolean roundRobin = true;
    /** Round robin: which target is next. */
    int robin;
    /** Ticks until this Extract side pulls again. */
    int cooldown;

    public boolean isExtract() {
        return extract;
    }

    public RedstoneMode redstone() {
        return redstone;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public boolean isRoundRobin() {
        return roundRobin;
    }

    public boolean hasCircuit() {
        return isCircuit(circuit);
    }

    public static boolean isCircuit(ItemStack stack) {
        return stack.is(ModItems.ELECTRONIC_CIRCUIT.get());
    }

    /** Whether this side lets the item through: always without a circuit, else by its whitelist / blacklist. */
    public boolean passes(ItemStack stack) {
        if (!hasCircuit()) {
            return true;
        }
        return blacklist != lists(stack);
    }

    /** Whether the item is in the filter (any count or components). */
    public boolean lists(ItemStack stack) {
        for (ItemStack entry : filter) {
            if (!entry.isEmpty() && ItemStack.isSameItem(entry, stack)) {
                return true;
            }
        }
        return false;
    }

    /** Extract side: spreads items over its targets in turn (needs a circuit). */
    public boolean spreads() {
        return hasCircuit() && roundRobin;
    }

    void save(ValueOutput output) {
        output.putBoolean("extract", extract);
        output.putInt("redstone", redstone.ordinal());
        if (!circuit.isEmpty()) {
            output.store("circuit", ItemStack.CODEC, circuit);
        }
        output.store("filter", ItemStack.OPTIONAL_CODEC.listOf(), filter);
        output.putBoolean("blacklist", blacklist);
        output.putBoolean("round_robin", roundRobin);
        output.putInt("robin", robin);
    }

    void load(ValueInput input) {
        extract = input.getBooleanOr("extract", false);
        redstone = RedstoneMode.byOrdinal(input.getIntOr("redstone", RedstoneMode.HIGH.ordinal()));
        circuit = input.read("circuit", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        var list = input.read("filter", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        for (int i = 0; i < FILTER_SLOTS; i++) {
            filter.set(i, i < list.size() ? list.get(i) : ItemStack.EMPTY);
        }
        blacklist = input.getBooleanOr("blacklist", false);
        roundRobin = input.getBooleanOr("round_robin", true);
        robin = input.getIntOr("robin", 0);
    }
}
