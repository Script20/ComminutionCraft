package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Generator GUI: one fuel slot under a flame, energy bar on the left. */
public class ElectricGeneratorMenu extends AbstractContainerMenu {
    private static final int INV_START = ElectricGeneratorBlockEntity.SLOT_COUNT;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;
    private final Level level;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static ElectricGeneratorMenu client(int containerId, Inventory inventory) {
        return new ElectricGeneratorMenu(containerId, inventory,
                new SimpleContainer(ElectricGeneratorBlockEntity.SLOT_COUNT),
                new SimpleContainerData(ElectricGeneratorBlockEntity.DATA_COUNT));
    }

    public ElectricGeneratorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.ELECTRIC_GENERATOR.get(), containerId);
        checkContainerSize(container, ElectricGeneratorBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, ElectricGeneratorBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.level = inventory.player.level();

        addSlot(new FuelSlot(container, ElectricGeneratorBlockEntity.SLOT_FUEL, 80, 53));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
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
        } else if (isFuel(stack)) {
            if (!moveItemStackTo(stack, ElectricGeneratorBlockEntity.SLOT_FUEL, ElectricGeneratorBlockEntity.SLOT_FUEL + 1, false)) {
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

    public boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
    }

    // ---- GUI helpers ----

    public boolean isLit() {
        return data.get(ElectricGeneratorBlockEntity.DATA_LIT_TIME) > 0;
    }

    public float getLitProgress() {
        int duration = data.get(ElectricGeneratorBlockEntity.DATA_LIT_DURATION);
        if (duration == 0) {
            duration = 200;
        }
        return Math.clamp((float) data.get(ElectricGeneratorBlockEntity.DATA_LIT_TIME) / duration, 0.0F, 1.0F);
    }

    public int getEnergy() {
        return EnergySync.join(data.get(ElectricGeneratorBlockEntity.DATA_ENERGY_LOW), data.get(ElectricGeneratorBlockEntity.DATA_ENERGY_HIGH));
    }

    public int getEnergyCapacity() {
        return EnergySync.join(data.get(ElectricGeneratorBlockEntity.DATA_CAPACITY_LOW), data.get(ElectricGeneratorBlockEntity.DATA_CAPACITY_HIGH));
    }

    private class FuelSlot extends Slot {
        FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isFuel(stack) || stack.is(Items.BUCKET);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
        }
    }
}
