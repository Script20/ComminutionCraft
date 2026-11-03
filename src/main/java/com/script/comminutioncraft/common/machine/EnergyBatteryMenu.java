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
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Battery GUI: no slots of its own, just the player inventory, the energy bar and the in/out rates. */
public class EnergyBatteryMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_END = 27;
    private static final int HOTBAR_END = INV_END + 9;

    /** The battery itself on the server; null on the client, which only sees the synced data. */
    private final @Nullable EnergyBatteryBlockEntity battery;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static EnergyBatteryMenu client(int containerId, Inventory inventory) {
        return new EnergyBatteryMenu(containerId, inventory, null, new SimpleContainerData(EnergyBatteryBlockEntity.DATA_COUNT));
    }

    public EnergyBatteryMenu(int containerId, Inventory inventory, @Nullable EnergyBatteryBlockEntity battery, ContainerData data) {
        super(ModMenus.ENERGY_BATTERY.get(), containerId);
        checkContainerDataCount(data, EnergyBatteryBlockEntity.DATA_COUNT);
        this.battery = battery;
        this.data = data;
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // ---- Redstone / side settings (synced through data slots, changed with button clicks) ----

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return battery != null && battery.getControls().handleButton(buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(EnergyBatteryBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(EnergyBatteryBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return battery == null || Container.stillValidBlockEntity(battery, player);
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
        return EnergySync.join(data.get(EnergyBatteryBlockEntity.DATA_ENERGY_LOW), data.get(EnergyBatteryBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(EnergyBatteryBlockEntity.DATA_CAPACITY_LOW), data.get(EnergyBatteryBlockEntity.DATA_CAPACITY_HIGH));
    }

    public int getReceived() {
        return data.get(EnergyBatteryBlockEntity.DATA_RECEIVED);
    }

    public int getSent() {
        return data.get(EnergyBatteryBlockEntity.DATA_SENT);
    }
}
