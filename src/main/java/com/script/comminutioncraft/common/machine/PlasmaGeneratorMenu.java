package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModItems;
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

/** Plasma Generator GUI: Air Filter slot, energy bar on the left, plasma gauge on the right, intake readout. */
public class PlasmaGeneratorMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_START = PlasmaGeneratorBlockEntity.SLOT_COUNT;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static PlasmaGeneratorMenu client(int containerId, Inventory inventory) {
        return new PlasmaGeneratorMenu(containerId, inventory,
                new SimpleContainer(PlasmaGeneratorBlockEntity.SLOT_COUNT),
                new SimpleContainerData(PlasmaGeneratorBlockEntity.DATA_COUNT));
    }

    public PlasmaGeneratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.PLASMA_GENERATOR.get(), containerId);
        checkContainerSize(container, PlasmaGeneratorBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, PlasmaGeneratorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;

        addSlot(new FilterSlot(container, PlasmaGeneratorBlockEntity.SLOT_FILTER, 80, 53));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // ---- Redstone / side settings (synced through data slots, changed with button clicks) ----

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return MachineControls.handleMenuButton(container, buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(PlasmaGeneratorBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(PlasmaGeneratorBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.AIR_FILTER.get())) {
            if (!moveItemStackTo(stack, PlasmaGeneratorBlockEntity.SLOT_FILTER, PlasmaGeneratorBlockEntity.SLOT_FILTER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < INV_END) {
            if (!moveItemStackTo(stack, INV_END, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INV_START, INV_END, false)) {
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

    public int getEnergy() {
        return EnergySync.join(data.get(PlasmaGeneratorBlockEntity.DATA_ENERGY_LOW), data.get(PlasmaGeneratorBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(PlasmaGeneratorBlockEntity.DATA_CAPACITY_LOW), data.get(PlasmaGeneratorBlockEntity.DATA_CAPACITY_HIGH));
    }

    public int getPlasma() {
        return data.get(PlasmaGeneratorBlockEntity.DATA_PLASMA);
    }

    public int getPlasmaCapacity() {
        return data.get(PlasmaGeneratorBlockEntity.DATA_PLASMA_CAPACITY);
    }

    public int getIntake() {
        return data.get(PlasmaGeneratorBlockEntity.DATA_INTAKE);
    }

    /** mB made last tick. */
    public int getRate() {
        return data.get(PlasmaGeneratorBlockEntity.DATA_RATE);
    }

    public boolean hasFilter() {
        return slots.get(PlasmaGeneratorBlockEntity.SLOT_FILTER).getItem().is(ModItems.AIR_FILTER.get());
    }

    private static class FilterSlot extends Slot {
        FilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.AIR_FILTER.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
