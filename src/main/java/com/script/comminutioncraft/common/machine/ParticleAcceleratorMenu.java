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

/** Accelerator Controller GUI: no slots of its own; energy, hydrogen, antimatter, ring size, status and progress. */
public class ParticleAcceleratorMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int INV_END = 27;
    private static final int HOTBAR_END = INV_END + 9;

    /** The controller on the server; null on the client, which only sees the synced data. */
    private final @Nullable ParticleAcceleratorBlockEntity accelerator;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static ParticleAcceleratorMenu client(int containerId, Inventory inventory) {
        return new ParticleAcceleratorMenu(containerId, inventory, null, new SimpleContainerData(ParticleAcceleratorBlockEntity.DATA_COUNT));
    }

    public ParticleAcceleratorMenu(int containerId, Inventory inventory, @Nullable ParticleAcceleratorBlockEntity accelerator, ContainerData data) {
        super(ModMenus.PARTICLE_ACCELERATOR.get(), containerId);
        checkContainerDataCount(data, ParticleAcceleratorBlockEntity.DATA_COUNT);
        this.accelerator = accelerator;
        this.data = data;
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return accelerator != null && accelerator.getControls().handleButton(buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(ParticleAcceleratorBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(ParticleAcceleratorBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return accelerator == null || Container.stillValidBlockEntity(accelerator, player);
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
        return EnergySync.join(data.get(ParticleAcceleratorBlockEntity.DATA_ENERGY_LOW), data.get(ParticleAcceleratorBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(ParticleAcceleratorBlockEntity.DATA_CAPACITY_LOW), data.get(ParticleAcceleratorBlockEntity.DATA_CAPACITY_HIGH));
    }

    /** FE used last tick. */
    public int getEnergyUse() {
        return EnergySync.join(data.get(ParticleAcceleratorBlockEntity.DATA_USE_LOW), data.get(ParticleAcceleratorBlockEntity.DATA_USE_HIGH));
    }

    public int getHydrogen() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_HYDROGEN);
    }

    public int getHydrogenCapacity() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_HYDROGEN_CAPACITY);
    }

    public int getAntimatter() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_ANTIMATTER);
    }

    public int getAntimatterCapacity() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_ANTIMATTER_CAPACITY);
    }

    public ParticleAcceleratorBlockEntity.Status getStatus() {
        ParticleAcceleratorBlockEntity.Status[] values = ParticleAcceleratorBlockEntity.Status.values();
        int i = data.get(ParticleAcceleratorBlockEntity.DATA_STATUS);
        return i >= 0 && i < values.length ? values[i] : ParticleAcceleratorBlockEntity.Status.NOT_FORMED;
    }

    public int getRingSide() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_SIDE);
    }

    public int getProgress() {
        return data.get(ParticleAcceleratorBlockEntity.DATA_PROGRESS);
    }

    public int getProgressMax() {
        return Math.max(1, data.get(ParticleAcceleratorBlockEntity.DATA_PROGRESS_MAX));
    }
}
