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

/** Reactor Controller GUI: the fuel pellet slot, energy, plasma, tritium, core heat and status. */
public class FusionReactorMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_START = 1;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static FusionReactorMenu client(int containerId, Inventory inventory) {
        return new FusionReactorMenu(containerId, inventory, new SimpleContainer(1), new SimpleContainerData(FusionReactorBlockEntity.DATA_COUNT));
    }

    public FusionReactorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.FUSION_REACTOR.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, FusionReactorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        addSlot(new Slot(container, FusionReactorBlockEntity.SLOT_PELLETS, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.FUSION_FUEL_PELLET.get());
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return MachineControls.handleMenuButton(container, buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(FusionReactorBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(FusionReactorBlockEntity.DATA_SIDES), side);
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
        boolean moved;
        if (index < INV_START) {
            moved = moveItemStackTo(stack, INV_START, HOTBAR_END, true);
        } else if (stack.is(ModItems.FUSION_FUEL_PELLET.get())) {
            moved = moveItemStackTo(stack, 0, INV_START, false);
        } else if (index < INV_END) {
            moved = moveItemStackTo(stack, INV_END, HOTBAR_END, false);
        } else {
            moved = moveItemStackTo(stack, INV_START, INV_END, false);
        }
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

    public int getEnergy() {
        return EnergySync.join(data.get(FusionReactorBlockEntity.DATA_ENERGY_LOW), data.get(FusionReactorBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(FusionReactorBlockEntity.DATA_CAPACITY_LOW), data.get(FusionReactorBlockEntity.DATA_CAPACITY_HIGH));
    }

    public int getHeat() {
        return data.get(FusionReactorBlockEntity.DATA_HEAT);
    }

    public int getBurnTicks() {
        return data.get(FusionReactorBlockEntity.DATA_BURN);
    }

    public int getPlasma() {
        return data.get(FusionReactorBlockEntity.DATA_PLASMA);
    }

    public int getPlasmaCapacity() {
        return data.get(FusionReactorBlockEntity.DATA_PLASMA_CAPACITY);
    }

    public int getTritium() {
        return data.get(FusionReactorBlockEntity.DATA_TRITIUM);
    }

    public int getTritiumCapacity() {
        return data.get(FusionReactorBlockEntity.DATA_TRITIUM_CAPACITY);
    }

    public boolean isFormed() {
        return data.get(FusionReactorBlockEntity.DATA_FORMED) != 0;
    }

    /** FE made last tick. */
    public int getRate() {
        return data.get(FusionReactorBlockEntity.DATA_RATE);
    }
}
