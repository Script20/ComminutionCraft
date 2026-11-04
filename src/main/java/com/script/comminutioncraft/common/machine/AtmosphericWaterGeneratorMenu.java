package com.script.comminutioncraft.common.machine;

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

/** Atmospheric Water Generator GUI: Air Filter slot, water gauge on the right, intake and humidity readout (no FE). */
public class AtmosphericWaterGeneratorMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_START = AtmosphericWaterGeneratorBlockEntity.SLOT_COUNT;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static AtmosphericWaterGeneratorMenu client(int containerId, Inventory inventory) {
        return new AtmosphericWaterGeneratorMenu(containerId, inventory,
                new SimpleContainer(AtmosphericWaterGeneratorBlockEntity.SLOT_COUNT),
                new SimpleContainerData(AtmosphericWaterGeneratorBlockEntity.DATA_COUNT));
    }

    public AtmosphericWaterGeneratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.ATMOSPHERIC_WATER_GENERATOR.get(), containerId);
        checkContainerSize(container, AtmosphericWaterGeneratorBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, AtmosphericWaterGeneratorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;

        addSlot(new FilterSlot(container, AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER, 80, 53));
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
        return RedstoneMode.byOrdinal(data.get(AtmosphericWaterGeneratorBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(AtmosphericWaterGeneratorBlockEntity.DATA_SIDES), side);
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
            if (!moveItemStackTo(stack, AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER, AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER + 1, false)) {
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

    public int getWater() {
        return data.get(AtmosphericWaterGeneratorBlockEntity.DATA_WATER);
    }

    public int getWaterCapacity() {
        return data.get(AtmosphericWaterGeneratorBlockEntity.DATA_WATER_CAPACITY);
    }

    /** Humidity in percent (100 = 1 mB/t per open face). */
    public int getHumidity() {
        return data.get(AtmosphericWaterGeneratorBlockEntity.DATA_HUMIDITY);
    }

    public int getIntake() {
        return data.get(AtmosphericWaterGeneratorBlockEntity.DATA_INTAKE);
    }

    /** mB made per tick, x 10. */
    public int getRateTenths() {
        return data.get(AtmosphericWaterGeneratorBlockEntity.DATA_RATE_TENTHS);
    }

    public boolean hasFilter() {
        return slots.get(AtmosphericWaterGeneratorBlockEntity.SLOT_FILTER).getItem().is(ModItems.AIR_FILTER.get());
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
