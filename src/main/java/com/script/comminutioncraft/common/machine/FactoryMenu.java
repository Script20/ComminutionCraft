package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeHolder;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeSlot;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeType;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Every factory's menu: one column per lane (input on top, output underneath), a shared by-product slot for the
 * purifying factories, the player inventory, the four upgrade slots. The {@link Layout} comes with the open packet.
 */
public class FactoryMenu extends AbstractContainerMenu implements ControllableMenu {
    public static final int INPUT_Y = 18;
    public static final int OUTPUT_Y = 52;

    /**
     * How a factory's GUI is laid out: lanes, which fluid gauge (0 none, 1 water, 2 oxygen), whether it has a
     * by-product slot, and the factory's block id (its textures, for the side config cube).
     */
    public record Layout(int lanes, int fluid, boolean byproduct, String block) {
        public static Layout read(RegistryFriendlyByteBuf buffer) {
            return new Layout(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readUtf());
        }

        public int slots() {
            return 2 * lanes + (byproduct ? 1 : 0);
        }

        /** The machine's own data values (more with a fluid tank), before the other lanes' progress. */
        public int baseData() {
            return ElectricMachineBlockEntity.DATA_COUNT + (fluid != 0 ? 2 : 0);
        }

        public int dataCount() {
            return baseData() + lanes - 1;
        }

        private int columns() {
            return lanes + (byproduct ? 1 : 0);
        }

        /** Where column {@code i}'s slots sit (the item's x): spread between the energy tube and the right edge or gauge. */
        public int columnX(int i) {
            int left = 28;
            int width = (fluid != 0 ? 150 : 168) - left;
            int step = Math.min(26, width / columns());
            int start = left + (width - step * columns()) / 2;
            return start + i * step + (step - 18) / 2 + 1;
        }
    }

    private final Container container;
    private final ContainerData data;
    private final Layout layout;
    private final int machineSlots;
    private final int upgradeStart;
    private final MachineUpgrades upgrades;

    // Client side: the layout comes with the open packet (see ElectricMachineBlockEntity.writeFactoryLayout)
    public static FactoryMenu client(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        Layout layout = Layout.read(buffer);
        return new FactoryMenu(containerId, inventory, new SimpleContainer(layout.slots()), new SimpleContainerData(layout.dataCount()), layout);
    }

    public FactoryMenu(int containerId, Inventory inventory, Container container, ContainerData data, Layout layout) {
        super(ModMenus.FACTORY.get(), containerId);
        checkContainerSize(container, layout.slots());
        checkContainerDataCount(data, layout.dataCount());
        this.container = container;
        this.data = data;
        this.layout = layout;
        for (int lane = 0; lane < layout.lanes(); lane++) {
            addSlot(new Slot(container, lane, layout.columnX(lane), INPUT_Y));
        }
        for (int lane = 0; lane < layout.lanes(); lane++) {
            addSlot(new MachineOutputSlot(inventory.player, container, layout.lanes() + lane, layout.columnX(lane), OUTPUT_Y));
        }
        if (layout.byproduct()) {
            addSlot(new MachineOutputSlot(inventory.player, container, 2 * layout.lanes(), layout.columnX(layout.lanes()), OUTPUT_Y));
        }
        machineSlots = slots.size();
        addStandardInventorySlots(inventory, 8, 84);
        upgradeStart = slots.size();
        upgrades = container instanceof UpgradeHolder holder
                ? holder.getUpgrades() : MachineUpgrades.client(() -> data.get(ElectricMachineBlockEntity.DATA_UPGRADES));
        for (UpgradeType upgrade : UpgradeType.values()) {
            addSlot(new UpgradeSlot(upgrades, upgrade, inventory.player.level().isClientSide()));
        }
        addDataSlots(data);
    }

    public Layout layout() {
        return layout;
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
    public boolean clickMenuButton(Player player, int buttonId) {
        return MachineControls.handleMenuButton(container, buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(ElectricMachineBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(ElectricMachineBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /** Shift-click: machine slots to the inventory; upgrades to their slots; anything else into the lanes' inputs. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int inventoryEnd = machineSlots + 36;
        int upgradeEnd = upgradeStart + UpgradeType.values().length;
        if (index < machineSlots || (index >= upgradeStart && index < upgradeEnd)) {
            if (!moveItemStackTo(stack, machineSlots, inventoryEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (UpgradeType.of(stack) != null && moveItemStackTo(stack, upgradeStart, upgradeEnd, false)) {
            // an upgrade: into its slot
        } else if (!moveItemStackTo(stack, 0, layout.lanes(), false)) {
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

    // ---- GUI helpers ----

    /** How far a lane's current item is (0..1). */
    public float getProgress(int lane) {
        int progress = lane == 0 ? data.get(ElectricMachineBlockEntity.DATA_PROGRESS) : data.get(layout.baseData() + lane - 1);
        int total = data.get(ElectricMachineBlockEntity.DATA_PROGRESS_TOTAL);
        return total != 0 && progress != 0 ? Math.clamp((float) progress / total, 0.0F, 1.0F) : 0.0F;
    }

    public boolean isWorking() {
        for (int lane = 0; lane < layout.lanes(); lane++) {
            if (getProgress(lane) > 0) {
                return true;
            }
        }
        return false;
    }

    public int getEnergy() {
        return EnergySync.join(data.get(ElectricMachineBlockEntity.DATA_ENERGY_LOW), data.get(ElectricMachineBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(ElectricMachineBlockEntity.DATA_CAPACITY_LOW), data.get(ElectricMachineBlockEntity.DATA_CAPACITY_HIGH));
    }

    /** The water / oxygen tank (washers' and purifiers' data both start right after the machine's own). */
    public int getFluid() {
        return layout.fluid() != 0 ? data.get(ElectricMachineBlockEntity.DATA_COUNT) : 0;
    }

    public int getFluidCapacity() {
        return layout.fluid() != 0 ? data.get(ElectricMachineBlockEntity.DATA_COUNT + 1) : 0;
    }
}
