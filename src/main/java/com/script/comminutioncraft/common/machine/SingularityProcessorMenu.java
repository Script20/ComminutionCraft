package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Singularity Processor menu: input, output (moved up) and a by-products slot under it, and whether a black hole is near. */
public class SingularityProcessorMenu extends ElectricMachineMenu {
    /** The by-product slot comes after the player inventory (the parent's slot layout stays as it is). */
    private static final int BYPRODUCT_INDEX = ElectricMachineBlockEntity.SLOT_COUNT + 36;

    private final ContainerData data;

    public static SingularityProcessorMenu client(int containerId, Inventory inventory) {
        return new SingularityProcessorMenu(containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT + 1), new SimpleContainerData(SingularityProcessorBlockEntity.DATA_COUNT));
    }

    public SingularityProcessorMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.SINGULARITY_PROCESSOR.get(), containerId, inventory, container, data, 24);
        checkContainerDataCount(data, SingularityProcessorBlockEntity.DATA_COUNT);
        this.data = data;
        addSlot(new MachineOutputSlot(inventory.player, container, SingularityProcessorBlockEntity.SLOT_BYPRODUCT, 116, 46));
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
        if (!moveItemStackTo(stack, ElectricMachineBlockEntity.SLOT_COUNT, BYPRODUCT_INDEX, true)) {
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

    public boolean isNearHole() {
        return data.get(SingularityProcessorBlockEntity.DATA_NEAR_HOLE) != 0;
    }
}
