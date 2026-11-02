package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menu shared by all tier-2 processing machines: input, output and an energy bar on the left. */
public class ElectricMachineMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int MACHINE_SLOTS = ElectricMachineBlockEntity.SLOT_COUNT;
    private static final int INV_START = MACHINE_SLOTS;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;

    // Client-side constructors (called when the server tells the client to open the GUI)
    public static ElectricMachineMenu clientGranulator(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.ELECTRIC_GRANULATOR.get(), containerId, inventory,
                new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(ElectricMachineBlockEntity.DATA_COUNT));
    }

    public static ElectricMachineMenu clientSmelter(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.ELECTRIC_SMELTER.get(), containerId, inventory,
                new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(ElectricMachineBlockEntity.DATA_COUNT));
    }

    public ElectricMachineMenu(MenuType<?> type, int containerId, Inventory inventory, Container container, ContainerData data) {
        super(type, containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, ElectricMachineBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;

        addSlot(new Slot(container, ElectricMachineBlockEntity.SLOT_INPUT, 56, 35));
        addSlot(new MachineOutputSlot(inventory.player, container, ElectricMachineBlockEntity.SLOT_OUTPUT, 116, 35));
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == ElectricMachineBlockEntity.SLOT_OUTPUT) {
            if (!moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, ElectricMachineBlockEntity.SLOT_INPUT, ElectricMachineBlockEntity.SLOT_INPUT + 1, false)) {
            // Couldn't go in the machine: shuffle between main inventory and hotbar instead.
            if (index < INV_END) {
                if (!moveItemStackTo(stack, INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, INV_START, INV_END, false)) {
                return ItemStack.EMPTY;
            }
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

    public float getProgress() {
        int progress = data.get(ElectricMachineBlockEntity.DATA_PROGRESS);
        int total = data.get(ElectricMachineBlockEntity.DATA_PROGRESS_TOTAL);
        return total != 0 && progress != 0 ? Math.clamp((float) progress / total, 0.0F, 1.0F) : 0.0F;
    }

    public boolean isWorking() {
        return data.get(ElectricMachineBlockEntity.DATA_PROGRESS) > 0;
    }

    public int getEnergy() {
        return EnergySync.join(data.get(ElectricMachineBlockEntity.DATA_ENERGY_LOW), data.get(ElectricMachineBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(ElectricMachineBlockEntity.DATA_CAPACITY_LOW), data.get(ElectricMachineBlockEntity.DATA_CAPACITY_HIGH));
    }
}
