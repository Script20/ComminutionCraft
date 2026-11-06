package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** GUI for the fluid machines (Electrolyser, Plasma Generator, MHD Generator): no slots of their own, just gauges. */
public class FluidMachineMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_END = 27;
    private static final int HOTBAR_END = INV_END + 9;

    /** The machine itself on the server; null on the client, which only sees the synced data. */
    private final @Nullable FluidMachineBlockEntity machine;
    private final ContainerData data;

    /** Client-side constructor (called when the server tells the client to open the GUI). */
    public static FluidMachineMenu client(MenuType<?> type, int containerId, Inventory inventory, int tankCount) {
        return new FluidMachineMenu(type, containerId, inventory, null,
                new SimpleContainerData(FluidMachineBlockEntity.dataCount(tankCount)));
    }

    public FluidMachineMenu(MenuType<?> type, int containerId, Inventory inventory, @Nullable FluidMachineBlockEntity machine,
                            ContainerData data) {
        super(type, containerId);
        this.machine = machine;
        this.data = data;
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // ---- Redstone / side settings (synced through data slots, changed with button clicks) ----

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return machine != null && machine.getControls().handleButton(buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(FluidMachineBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(FluidMachineBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean hasByproductOutput() {
        return getType() == ModMenus.ELECTROLYSER.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return machine == null || Container.stillValidBlockEntity(machine, player);
    }

    /** Shift-click moves stacks between the main inventory and the hotbar. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < INV_END
                ? moveItemStackTo(stack, INV_END, HOTBAR_END, false)
                : moveItemStackTo(stack, 0, INV_END, false);
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
        return EnergySync.join(data.get(FluidMachineBlockEntity.DATA_ENERGY_LOW), data.get(FluidMachineBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(FluidMachineBlockEntity.DATA_CAPACITY_LOW), data.get(FluidMachineBlockEntity.DATA_CAPACITY_HIGH));
    }

    /** mB/t (or FE/t for the MHD Generator) last tick. */
    public int getRate() {
        return EnergySync.join(data.get(FluidMachineBlockEntity.DATA_RATE), data.get(FluidMachineBlockEntity.DATA_RATE_HIGH));
    }

    public int getTankAmount(int tank) {
        return data.get(FluidMachineBlockEntity.DATA_TANKS + tank * 2);
    }

    public int getTankCapacity(int tank) {
        return data.get(FluidMachineBlockEntity.DATA_TANKS + tank * 2 + 1);
    }
}
