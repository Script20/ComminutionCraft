package com.script.comminutioncraft.common.pipe;

import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * Item Pipe (tier 2): its six sides' settings and the stacks travelling through it. Extract sides pull a stack every
 * {@link #EXTRACT_INTERVAL} ticks (when redstone allows) and send it to the nearest Insert side with room, or, with a
 * circuit and round robin on, to each target in turn. Stacks cross a pipe in {@link #TICKS_PER_BLOCK} ticks and are
 * handed from pipe to pipe along their route. Pulls only take what the target can hold, counting what's already on its
 * way. If the target is gone or full anyway they find another, or go back where they came from; failing both they stay
 * in the pipe's core and the pipe is stuffed ({@link ItemPipeBlock#STUFFED}) until somewhere takes them (tried every
 * {@link #RETRY_TICKS} ticks).
 */
public class ItemPipeBlockEntity extends BlockEntity {
    public static final ConfigInt EXTRACT_INTERVAL = MachineConfig.ITEM_PIPE_EXTRACT_INTERVAL;
    public static final ConfigInt EXTRACT_AMOUNT = MachineConfig.ITEM_PIPE_EXTRACT_AMOUNT;
    public static final ConfigInt TICKS_PER_BLOCK = MachineConfig.ITEM_PIPE_TICKS_PER_BLOCK;

    private final Map<Direction, PipeSide> sides = new EnumMap<>(Direction.class);
    public static final int RETRY_TICKS = 20;

    private final List<TravellingItem> items = new ArrayList<>();
    /** Items with nowhere to go, waiting in the core. */
    private final List<TravellingItem> stuck = new ArrayList<>();
    private int retry;
    private boolean syncNeeded;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PIPE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sides.put(direction, new PipeSide());
        }
    }

    public PipeSide side(Direction direction) {
        return sides.get(direction);
    }

    /** What's moving through the pipe right now (read-only view, for the renderer and tests). */
    public List<TravellingItem> items() {
        return List.copyOf(items);
    }

    /** What's stuck in the core (nowhere to go yet). */
    public List<ItemStack> stuck() {
        return stuck.stream().map(TravellingItem::stack).toList();
    }

    /** How many of {@code stack}'s item are travelling through this pipe towards that pipe side. */
    int comingTo(BlockPos pipe, Direction side, ItemStack stack) {
        int count = 0;
        for (TravellingItem item : items) {
            if (item.headsFor(pipe, side) && ItemStack.isSameItemSameComponents(item.stack, stack)) {
                count += item.stack.getCount();
            }
        }
        return count;
    }

    /** Settings changed (mode, filter...): save and tell clients (the renderer draws Extract plates). */
    public void settingsChanged() {
        setChanged();
        syncNeeded = true;
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            syncNeeded = false;
        }
    }

    private static float step() {
        return 1.0F / Math.max(1, TICKS_PER_BLOCK.get());
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        for (Direction direction : Direction.values()) {
            PipeSide side = sides.get(direction);
            if (!side.extract || state.getValue(ConduitBlock.CONNECTIONS.get(direction)) != CableConnection.MACHINE) {
                continue;
            }
            if (--side.cooldown <= 0) {
                side.cooldown = EXTRACT_INTERVAL.get();
                if (side.redstone.allows(powered)) {
                    pull(level, pos, direction, side);
                }
            }
        }
        float step = step();
        for (TravellingItem item : List.copyOf(items)) {
            item.progress += step;
            if (item.progress >= 1) {
                items.remove(item);
                arrive(level, pos, state, item);
                syncNeeded = true;
            }
        }
        if (!stuck.isEmpty() && ++retry >= RETRY_TICKS) {
            retry = 0;
            unstick(level, pos);
        }
        boolean stuffed = !stuck.isEmpty();
        if (state.getValue(ItemPipeBlock.STUFFED) != stuffed) {
            level.setBlock(pos, state.setValue(ItemPipeBlock.STUFFED, stuffed), Block.UPDATE_ALL);
            syncNeeded = true;
        }
        if (syncNeeded) {
            syncNeeded = false;
            setChanged();
            BlockState now = level.getBlockState(pos);
            level.sendBlockUpdated(pos, now, now, Block.UPDATE_CLIENTS);
        }
    }

    /** Sends stuck items on wherever they can go now. */
    private void unstick(ServerLevel level, BlockPos pos) {
        for (TravellingItem item : List.copyOf(stuck)) {
            ItemPipeRouting.Target target = destination(level, pos, item, item.stack);
            if (target != null) {
                stuck.remove(item);
                items.add(item.rerouted(item.stack, target));
                syncNeeded = true;
            }
        }
    }

    /** Client: keeps the stacks moving between server updates (they wait at the exit face for the hand-off). */
    public void clientTick() {
        float step = step();
        for (TravellingItem item : items) {
            item.progress = Math.min(1, item.progress + step);
        }
    }

    /** Pulls one stack (up to the config amount, what the target takes) out of the block on {@code direction}. */
    private void pull(ServerLevel level, BlockPos pos, Direction direction, PipeSide side) {
        ResourceHandler<ItemResource> source = level.getCapability(Capabilities.Item.BLOCK, pos.relative(direction), direction.getOpposite());
        if (source == null) {
            return;
        }
        for (int slot = 0; slot < source.size(); slot++) {
            ItemResource resource = source.getResource(slot);
            if (resource.isEmpty()) {
                continue;
            }
            int have = (int) Math.min(Math.min(source.getAmountAsLong(slot), EXTRACT_AMOUNT.get()), resource.getMaxStackSize());
            ItemStack probe = resource.toStack(Math.max(have, 0));
            if (have <= 0 || !side.passes(probe)) {
                continue;
            }
            List<ItemPipeRouting.Target> targets = ItemPipeRouting.insertTargets(level, pos, probe, pos, direction);
            if (targets.isEmpty()) {
                continue;
            }
            ItemPipeRouting.Target target = side.spreads() ? targets.get(Math.floorMod(side.robin++, targets.size())) : targets.getFirst();
            int moved;
            try (Transaction transaction = Transaction.openRoot()) {
                moved = source.extract(slot, resource, Math.min(have, target.fits()), transaction);
                transaction.commit();
            }
            if (moved > 0) {
                items.add(new TravellingItem(resource.toStack(moved), direction, target.route(), 0, pos, direction,
                        target.pipe(), target.side()));
                syncNeeded = true;
                setChanged();
                return;
            }
        }
    }

    /** A stack reached its exit face: into the next pipe, or into the block at the end of its route. */
    private void arrive(ServerLevel level, BlockPos pos, BlockState state, TravellingItem item) {
        Direction exit = item.to();
        CableConnection connection = state.getValue(ConduitBlock.CONNECTIONS.get(exit));
        if (item.route.size() <= 1) {
            ItemStack left = item.stack;
            if (connection == CableConnection.MACHINE) {
                left = insert(level, pos, exit, item.stack);
            }
            if (!left.isEmpty()) {
                reroute(level, pos, item, left);
            }
            return;
        }
        if (connection == CableConnection.CABLE && level.getBlockEntity(pos.relative(exit)) instanceof ItemPipeBlockEntity next) {
            next.accept(item.next(item.progress - 1));
        } else {
            reroute(level, pos, item, item.stack);
        }
    }

    void accept(TravellingItem item) {
        items.add(item);
        syncNeeded = true;
        setChanged();
    }

    private static ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos.relative(side), side.getOpposite());
        if (handler == null) {
            return stack;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
        return stack.copyWithCount(stack.getCount() - inserted);
    }

    /** Somewhere else for {@code left}: another target, else back to its source, else it waits in this pipe's core. */
    private void reroute(ServerLevel level, BlockPos pos, TravellingItem item, ItemStack left) {
        ItemPipeRouting.Target target = destination(level, pos, item, left);
        if (target != null) {
            items.add(item.rerouted(left, target));
        } else {
            stuck.add(item.stuck(left));
        }
    }

    private static ItemPipeRouting.@Nullable Target destination(ServerLevel level, BlockPos pos, TravellingItem item, ItemStack stack) {
        List<ItemPipeRouting.Target> targets = ItemPipeRouting.insertTargets(level, pos, stack, null, null);
        return targets.isEmpty() ? ItemPipeRouting.to(level, pos, stack, item.source, item.sourceSide) : targets.getFirst();
    }

    // ---- Breaking: the circuits and everything in transit drop ----

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            for (PipeSide side : sides.values()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, side.circuit);
            }
            for (TravellingItem item : items) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item.stack);
            }
            for (TravellingItem item : stuck) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item.stack);
            }
            items.clear();
            stuck.clear();
        }
    }

    // ---- Saving and client sync ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (Direction direction : Direction.values()) {
            input.child(direction.getSerializedName()).ifPresent(sides.get(direction)::load);
        }
        items.clear();
        items.addAll(input.read("items", TravellingItem.CODEC.listOf()).orElse(List.of()));
        stuck.clear();
        stuck.addAll(input.read("stuck", TravellingItem.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (Direction direction : Direction.values()) {
            sides.get(direction).save(output.child(direction.getSerializedName()));
        }
        output.store("items", TravellingItem.CODEC.listOf(), items);
        output.store("stuck", TravellingItem.CODEC.listOf(), stuck);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
