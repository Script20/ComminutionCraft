package com.script.comminutioncraft.common.machine.miner;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.machine.MachineOutputSlot;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeSlot;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeType;
import com.script.comminutioncraft.common.menu.GhostItemMenu;
import com.script.comminutioncraft.common.registry.ModMenus;
import java.util.Locale;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Ore Miner GUI (owner-approved mock-up design/previews/ore_miner_gui.png): the 9 outputs; from tier 3 the circuit slot
 * and 9 ghost filter slots (click with an item to set one, empty-handed to clear it, or drag from JEI; they only work
 * with the tier's circuit in); the player's inventory; the upgrade slots. The tier comes with the open packet.
 */
public class OreMinerMenu extends AbstractContainerMenu implements ControllableMenu, GhostItemMenu {
    public static final int OUTPUT_X = 116;
    public static final int OUTPUT_Y = 66;
    public static final int CIRCUIT_X = 73;
    public static final int CIRCUIT_Y = 42;
    public static final int FILTER_X = 28;
    public static final int FILTER_Y = 66;
    public static final int INVENTORY_Y = 132;
    /** Slot indexes: outputs, circuit, filter, then the player's inventory and the upgrades. */
    public static final int CIRCUIT_SLOT = OreMinerBlockEntity.OUTPUT_SLOTS;
    public static final int FILTER_START = CIRCUIT_SLOT + 1;
    private static final int INVENTORY_START = FILTER_START + OreMinerBlockEntity.FILTER_SLOTS;

    private final @Nullable OreMinerBlockEntity miner;
    private final OreMinerTier tier;
    private final Container filterView;
    private final ContainerData data;
    private final int upgradeStart;
    private final MachineUpgrades upgrades;

    // Client side: the tier comes with the open packet (see OreMinerBlock)
    public static OreMinerMenu client(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        OreMinerTier tier = OreMinerTier.values()[buffer.readVarInt()];
        return new OreMinerMenu(containerId, inventory, null, tier, new SimpleContainer(OreMinerBlockEntity.OUTPUT_SLOTS),
                new SimpleContainer(1 + OreMinerBlockEntity.FILTER_SLOTS), new SimpleContainerData(OreMinerBlockEntity.DATA_COUNT));
    }

    public OreMinerMenu(int containerId, Inventory inventory, OreMinerBlockEntity miner) {
        this(containerId, inventory, miner, miner.tier(), miner, miner.filterView, miner.dataAccess());
    }

    private OreMinerMenu(int containerId, Inventory inventory, @Nullable OreMinerBlockEntity miner, OreMinerTier tier,
                         Container outputs, Container filterView, ContainerData data) {
        super(ModMenus.ORE_MINER.get(), containerId);
        checkContainerDataCount(data, OreMinerBlockEntity.DATA_COUNT);
        this.miner = miner;
        this.tier = tier;
        this.filterView = filterView;
        this.data = data;
        for (int i = 0; i < OreMinerBlockEntity.OUTPUT_SLOTS; i++) {
            addSlot(new MachineOutputSlot(inventory.player, outputs, i, OUTPUT_X + (i % 3) * 18, OUTPUT_Y + (i / 3) * 18));
        }
        addSlot(new Slot(filterView, 0, CIRCUIT_X, CIRCUIT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isCircuit(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return tier.hasOptions();
            }
        });
        for (int i = 0; i < OreMinerBlockEntity.FILTER_SLOTS; i++) {
            addSlot(new Slot(filterView, 1 + i, FILTER_X + (i % 3) * 18, FILTER_Y + (i / 3) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean isActive() {
                    return tier.hasOptions();
                }
            });
        }
        addStandardInventorySlots(inventory, 8, INVENTORY_Y);
        upgradeStart = slots.size();
        upgrades = miner != null ? miner.getUpgrades() : MachineUpgrades.client(() -> data.get(OreMinerBlockEntity.DATA_UPGRADES));
        for (UpgradeType upgrade : UpgradeType.values()) {
            addSlot(new UpgradeSlot(upgrades, upgrade, inventory.player.level().isClientSide()));
        }
        addDataSlots(data);
    }

    // ---- What the screen reads ----

    public OreMinerTier tier() {
        return tier;
    }

    @Override
    public String blockName() {
        return tier.name().toLowerCase(Locale.ROOT) + "_ore_miner";
    }

    public boolean isCircuit(ItemStack stack) {
        Item circuit = tier.circuit();
        return circuit != null && stack.is(circuit);
    }

    public boolean hasCircuit() {
        return isCircuit(getSlot(CIRCUIT_SLOT).getItem());
    }

    public int energy() {
        return EnergySync.join(data.get(OreMinerBlockEntity.DATA_ENERGY_LOW), data.get(OreMinerBlockEntity.DATA_ENERGY_HIGH));
    }

    public int energyCapacity() {
        return EnergySync.join(data.get(OreMinerBlockEntity.DATA_CAPACITY_LOW), data.get(OreMinerBlockEntity.DATA_CAPACITY_HIGH));
    }

    public OreMinerBlockEntity.Phase phase() {
        return OreMinerBlockEntity.Phase.values()[Math.floorMod(data.get(OreMinerBlockEntity.DATA_PHASE), OreMinerBlockEntity.Phase.values().length)];
    }

    public OreMinerBlockEntity.Waiting waiting() {
        return OreMinerBlockEntity.Waiting.values()[Math.floorMod(data.get(OreMinerBlockEntity.DATA_WAITING), OreMinerBlockEntity.Waiting.values().length)];
    }

    /** 1-based chunk number (of {@code tier().chunks()}). */
    public int chunk() {
        return data.get(OreMinerBlockEntity.DATA_CHUNK);
    }

    public int y() {
        return (short) data.get(OreMinerBlockEntity.DATA_Y);
    }

    public int mined() {
        return EnergySync.join(data.get(OreMinerBlockEntity.DATA_MINED_LOW), data.get(OreMinerBlockEntity.DATA_MINED_HIGH));
    }

    private boolean flag(int flag) {
        return (data.get(OreMinerBlockEntity.DATA_FLAGS) & flag) != 0;
    }

    public boolean fillStone() {
        return flag(OreMinerBlockEntity.FLAG_FILL_STONE);
    }

    public boolean oreBlocks() {
        return flag(OreMinerBlockEntity.FLAG_ORE_BLOCKS);
    }

    public boolean running() {
        return flag(OreMinerBlockEntity.FLAG_RUNNING);
    }

    public boolean blacklist() {
        return flag(OreMinerBlockEntity.FLAG_BLACKLIST);
    }

    // ---- Controls (redstone / sides / upgrades tabs and the miner's own buttons) ----

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return miner != null && miner.handleButton(id);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(OreMinerBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(OreMinerBlockEntity.DATA_SIDES), side);
    }

    @Override
    public int upgradeSlotStart() {
        return upgradeStart;
    }

    @Override
    public boolean takesUpgrade(UpgradeType type) {
        return upgrades.allows(type);
    }

    @Override
    public boolean stillValid(Player player) {
        return miner == null || Container.stillValidBlockEntity(miner, player);
    }

    // ---- Ghost filter slots ----

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
        if (miner == null || !tier.hasOptions() || !miner.hasCircuit() || slotIndex < FILTER_START || slotIndex >= INVENTORY_START) {
            return;
        }
        filterView.setItem(slotIndex - FILTER_START + 1, stack);
    }

    /**
     * Shift-click: outputs to the inventory; upgrades to their slots; the tier's circuit into its slot (and back out);
     * anything else (with the circuit in) into the next free filter slot.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int inventoryEnd = INVENTORY_START + 36;
        int upgradeEnd = upgradeStart + UpgradeType.values().length;
        if (index < CIRCUIT_SLOT + 1 || (index >= upgradeStart && index < upgradeEnd)) {
            if (!moveItemStackTo(stack, INVENTORY_START, inventoryEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index >= INVENTORY_START && index < inventoryEnd) {
            if (UpgradeType.of(stack) != null) {
                if (!moveItemStackTo(stack, upgradeStart, upgradeEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (tier.hasOptions() && isCircuit(stack) && !getSlot(CIRCUIT_SLOT).hasItem()) {
                moveItemStackTo(stack, CIRCUIT_SLOT, CIRCUIT_SLOT + 1, false);
            } else {
                if (hasCircuit() && miner != null) {
                    for (int i = 0; i < OreMinerBlockEntity.FILTER_SLOTS; i++) {
                        ItemStack entry = filterView.getItem(1 + i);
                        if (!entry.isEmpty() && ItemStack.isSameItem(entry, stack)) {
                            return ItemStack.EMPTY; // already listed
                        }
                    }
                    for (int i = 0; i < OreMinerBlockEntity.FILTER_SLOTS; i++) {
                        if (filterView.getItem(1 + i).isEmpty()) {
                            setGhost(player, FILTER_START + i, stack);
                            break;
                        }
                    }
                }
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
