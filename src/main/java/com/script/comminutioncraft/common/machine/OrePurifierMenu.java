package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Ore Purifier menu: input, output (moved up) and a by-product slot under it, plus the oxygen gauge. */
public class OrePurifierMenu extends ElectricMachineMenu {
    /** The by-product slot comes after the player inventory (the parent's slot layout stays as it is). */
    private static final int BYPRODUCT_INDEX = ElectricMachineBlockEntity.SLOT_COUNT + 36 + com.script.comminutioncraft.common.machine.upgrade.UpgradeType.values().length;

    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static OrePurifierMenu client(int containerId, Inventory inventory) {
        return new OrePurifierMenu(containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT + 1), new SimpleContainerData(OrePurifierBlockEntity.DATA_COUNT));
    }

    public static OrePurifierMenu clientAntimatter(int containerId, Inventory inventory) {
        return new OrePurifierMenu(ModMenus.ANTIMATTER_PURIFIER.get(), containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT + 1), new SimpleContainerData(OrePurifierBlockEntity.DATA_COUNT));
    }

    public OrePurifierMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        this(ModMenus.ORE_PURIFIER.get(), containerId, inventory, container, data);
    }

    public OrePurifierMenu(MenuType<?> type, int containerId, Inventory inventory, Container container, ContainerData data) {
        super(type, containerId, inventory, container, data, 24);
        checkContainerDataCount(data, OrePurifierBlockEntity.DATA_COUNT);
        this.data = data;
        addSlot(new MachineOutputSlot(inventory.player, container, OrePurifierBlockEntity.SLOT_BYPRODUCT, 116, 46));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index != BYPRODUCT_INDEX) {
            return super.quickMoveStack(player, index);
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, ElectricMachineBlockEntity.SLOT_COUNT, ElectricMachineBlockEntity.SLOT_COUNT + 36, true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    public int getOxygen() {
        return data.get(OrePurifierBlockEntity.DATA_OXYGEN);
    }

    public int getOxygenCapacity() {
        return data.get(OrePurifierBlockEntity.DATA_OXYGEN_CAPACITY);
    }
}
