package com.script.comminutioncraft.common.pipe;

import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.menu.GhostItemMenu;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Item Pipe GUI, one side at a time: the circuit slot (a real slot), 9 ghost filter slots (click with an item to set
 * one, empty-handed to clear it, or drag from JEI; only with a circuit in) and the player's inventory. Buttons go
 * through the vanilla menu button packet: {@link #BUTTON_SIDE} + side ordinal picks the side, then mode, list,
 * spread and redstone.
 */
public class ItemPipeMenu extends AbstractContainerMenu implements GhostItemMenu {
    public static final int CIRCUIT_X = 150;
    public static final int CIRCUIT_Y = 19;
    public static final int FILTER_X = 8;
    public static final int FILTER_Y = 52;
    public static final int BUTTON_SIDE = 0;
    public static final int BUTTON_MODE = 10;
    public static final int BUTTON_LIST = 11;
    public static final int BUTTON_SPREAD = 12;
    public static final int BUTTON_REDSTONE = 13;
    /** Slot indexes: circuit, then the filter, then the player's inventory. */
    public static final int CIRCUIT_SLOT = 0;
    public static final int FILTER_START = 1;
    private static final int INVENTORY_START = FILTER_START + PipeSide.FILTER_SLOTS;
    private static final int DATA_SIDE = 0;
    private static final int DATA_MODE = 1;
    private static final int DATA_FLAGS = 2;
    private static final int DATA_REDSTONE = 3;
    private static final int DATA_SIDES = 4;
    private static final int DATA_COUNT = 5;
    private static final int FLAG_BLACKLIST = 1;
    private static final int FLAG_ROUND_ROBIN = 2;

    private final @Nullable ItemPipeBlockEntity pipe;
    private final BlockPos pos;
    private Direction selected;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static ItemPipeMenu client(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        Direction side = buffer.readEnum(Direction.class);
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        data.set(DATA_SIDE, side.ordinal()); // until the first sync
        return new ItemPipeMenu(containerId, inventory, null, pos, side, new SimpleContainer(1 + PipeSide.FILTER_SLOTS), data);
    }

    public ItemPipeMenu(int containerId, Inventory inventory, ItemPipeBlockEntity pipe, Direction side) {
        this(containerId, inventory, pipe, pipe.getBlockPos(), side, null, null);
    }

    private ItemPipeMenu(int containerId, Inventory inventory, @Nullable ItemPipeBlockEntity pipe, BlockPos pos, Direction side,
                         @Nullable Container clientView, @Nullable ContainerData clientData) {
        super(ModMenus.ITEM_PIPE.get(), containerId);
        this.pipe = pipe;
        this.pos = pos;
        this.selected = side;
        Container view = clientView != null ? clientView : new SideView();
        this.data = clientData != null ? clientData : new ServerData();
        addSlot(new Slot(view, 0, CIRCUIT_X, CIRCUIT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PipeSide.isCircuit(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < PipeSide.FILTER_SLOTS; i++) {
            addSlot(new Slot(view, 1 + i, FILTER_X + i * 18, FILTER_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // ---- What the screen reads ----

    public BlockPos pos() {
        return pos;
    }

    public Direction side() {
        return Direction.values()[Math.floorMod(data.get(DATA_SIDE), 6)];
    }

    public ItemPipeBlock.Mode mode() {
        return ItemPipeBlock.Mode.values()[Math.floorMod(data.get(DATA_MODE), 3)];
    }

    public boolean blacklist() {
        return (data.get(DATA_FLAGS) & FLAG_BLACKLIST) != 0;
    }

    public boolean roundRobin() {
        return (data.get(DATA_FLAGS) & FLAG_ROUND_ROBIN) != 0;
    }

    public RedstoneMode redstone() {
        return RedstoneMode.byOrdinal(data.get(DATA_REDSTONE));
    }

    /** Sides that touch a block with an inventory (bit = direction ordinal). */
    public int inventorySides() {
        return data.get(DATA_SIDES);
    }

    public boolean hasCircuit() {
        return PipeSide.isCircuit(getSlot(CIRCUIT_SLOT).getItem());
    }

    // ---- Server side ----

    private PipeSide settings() {
        return pipe == null ? new PipeSide() : pipe.side(selected);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (pipe == null || pipe.getLevel() == null) {
            return false;
        }
        if (id >= BUTTON_SIDE && id < BUTTON_SIDE + 6) {
            Direction side = Direction.values()[id - BUTTON_SIDE];
            if ((ItemPipeBlock.inventorySides(pipe.getLevel(), pos) & (1 << side.ordinal())) != 0) {
                selected = side;
            }
            return true;
        }
        PipeSide settings = settings();
        switch (id) {
            case BUTTON_MODE -> ItemPipeBlock.cycleMode(pipe.getLevel(), pos, selected);
            case BUTTON_LIST -> settings.blacklist = !settings.blacklist;
            case BUTTON_SPREAD -> settings.roundRobin = !settings.roundRobin;
            case BUTTON_REDSTONE -> settings.redstone = settings.redstone.next();
            default -> {
                return false;
            }
        }
        pipe.settingsChanged();
        return true;
    }

    /** Filter slots are ghost slots: clicking sets (or clears) them, and they only work with a circuit in. */
    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (slotIndex >= FILTER_START && slotIndex < INVENTORY_START) {
            setGhost(player, slotIndex, getCarried());
            return;
        }
        super.clicked(slotIndex, button, input, player);
    }

    @Override
    public void setGhost(Player player, int slotIndex, ItemStack stack) {
        if (pipe == null || slotIndex < FILTER_START || slotIndex >= INVENTORY_START) {
            return;
        }
        PipeSide settings = settings();
        if (settings.hasCircuit()) {
            settings.filter.set(slotIndex - FILTER_START, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
            pipe.settingsChanged();
        }
    }

    /** Shift-click: a circuit goes into the circuit slot (and back out); anything else fills the next free filter slot. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (index == CIRCUIT_SLOT) {
            ItemStack original = stack.copy();
            if (!moveItemStackTo(stack, INVENTORY_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            return original;
        }
        if (index >= INVENTORY_START) {
            if (PipeSide.isCircuit(stack) && !getSlot(CIRCUIT_SLOT).hasItem()) {
                ItemStack original = stack.copy();
                moveItemStackTo(stack, CIRCUIT_SLOT, CIRCUIT_SLOT + 1, false);
                slot.setChanged();
                return original.getCount() == stack.getCount() ? ItemStack.EMPTY : original;
            }
            PipeSide settings = settings();
            if (pipe == null || !settings.hasCircuit() || settings.lists(stack)) {
                return ItemStack.EMPTY;
            }
            for (int i = 0; i < PipeSide.FILTER_SLOTS; i++) {
                if (settings.filter.get(i).isEmpty()) {
                    setGhost(player, FILTER_START + i, stack);
                    break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipe == null || Container.stillValidBlockEntity(pipe, player);
    }

    /** The selected side's circuit (slot 0) and filter (slots 1-9), straight from the pipe. */
    private final class SideView implements Container {
        @Override
        public int getContainerSize() {
            return 1 + PipeSide.FILTER_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            PipeSide settings = settings();
            return slot == 0 ? settings.circuit : settings.filter.get(slot - 1);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            if (slot != 0 || settings().circuit.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = settings().circuit.split(count);
            setChanged();
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, 64);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot == 0) {
                settings().circuit = stack;
                setChanged();
            }
        }

        @Override
        public void setChanged() {
            if (pipe != null) {
                pipe.settingsChanged();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return ItemPipeMenu.this.stillValid(player);
        }

        @Override
        public void clearContent() {}
    }

    private final class ServerData implements ContainerData {
        @Override
        public int get(int index) {
            if (pipe == null || pipe.getLevel() == null) {
                return 0;
            }
            PipeSide settings = settings();
            return switch (index) {
                case DATA_SIDE -> selected.ordinal();
                case DATA_MODE -> ItemPipeBlock.mode(pipe.getLevel(), pos, selected).ordinal();
                case DATA_FLAGS -> (settings.blacklist ? FLAG_BLACKLIST : 0) | (settings.roundRobin ? FLAG_ROUND_ROBIN : 0);
                case DATA_REDSTONE -> settings.redstone.ordinal();
                case DATA_SIDES -> ItemPipeBlock.inventorySides(pipe.getLevel(), pos);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }
}
