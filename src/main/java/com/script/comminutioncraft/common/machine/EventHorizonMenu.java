package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Event Horizon Controller GUI: the matter intake slot, energy, antimatter, mass, status. */
public class EventHorizonMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_START = 1;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;

    public static EventHorizonMenu client(int containerId, Inventory inventory) {
        return new EventHorizonMenu(containerId, inventory, new SimpleContainer(1), new SimpleContainerData(EventHorizonReactorBlockEntity.DATA_COUNT));
    }

    public EventHorizonMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.EVENT_HORIZON.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, EventHorizonReactorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        addSlot(new Slot(container, EventHorizonReactorBlockEntity.SLOT_INTAKE, 126, 52));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return MachineControls.handleMenuButton(container, buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(EventHorizonReactorBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(EventHorizonReactorBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /** Shift-click: anything from the inventory goes into the intake; the intake goes back to the inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < INV_START
                ? moveItemStackTo(stack, INV_START, HOTBAR_END, true)
                : moveItemStackTo(stack, 0, INV_START, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    // ---- GUI helpers ----

    private int join(int low) {
        return EnergySync.join(data.get(low), data.get(low + 1));
    }

    public int getEnergy() {
        return join(EventHorizonReactorBlockEntity.DATA_ENERGY_LOW);
    }

    public int getEnergyCapacity() {
        return join(EventHorizonReactorBlockEntity.DATA_CAPACITY_LOW);
    }

    public int getRate() {
        return join(EventHorizonReactorBlockEntity.DATA_RATE_LOW);
    }

    public int getMass() {
        return join(EventHorizonReactorBlockEntity.DATA_MASS_LOW);
    }

    public int getMassCapacity() {
        return Math.max(1, join(EventHorizonReactorBlockEntity.DATA_MASS_CAP_LOW));
    }

    public int getAntimatter() {
        return data.get(EventHorizonReactorBlockEntity.DATA_ANTIMATTER);
    }

    public int getAntimatterCapacity() {
        return data.get(EventHorizonReactorBlockEntity.DATA_ANTIMATTER_CAPACITY);
    }

    public EventHorizonReactorBlockEntity.Status getStatus() {
        EventHorizonReactorBlockEntity.Status[] values = EventHorizonReactorBlockEntity.Status.values();
        int i = data.get(EventHorizonReactorBlockEntity.DATA_STATUS);
        return i >= 0 && i < values.length ? values[i] : EventHorizonReactorBlockEntity.Status.NOT_FORMED;
    }

    public int getStarveSeconds() {
        return data.get(EventHorizonReactorBlockEntity.DATA_STARVE_SECONDS);
    }
}
