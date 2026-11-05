package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

/**
 * Creative Fluid Tank GUI: pick the fluid it makes. A button click sends id 0 (no fluid) or the fluid's position in
 * {@link CreativeFluidTankBlockEntity#fluids()} + 1, through the vanilla menu button packet.
 */
public class CreativeFluidTankMenu extends AbstractContainerMenu {
    private static final int INV_END = 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final @Nullable CreativeFluidTankBlockEntity tank;
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static CreativeFluidTankMenu client(int containerId, Inventory inventory) {
        return new CreativeFluidTankMenu(containerId, inventory, null, new SimpleContainerData(CreativeFluidTankBlockEntity.DATA_COUNT));
    }

    public CreativeFluidTankMenu(int containerId, Inventory inventory, @Nullable CreativeFluidTankBlockEntity tank, ContainerData data) {
        super(ModMenus.CREATIVE_FLUID_TANK.get(), containerId);
        this.tank = tank;
        this.data = data;
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        List<Fluid> fluids = CreativeFluidTankBlockEntity.fluids();
        if (tank == null || buttonId < 0 || buttonId > fluids.size()) {
            return false;
        }
        tank.setFluid(buttonId == 0 ? null : fluids.get(buttonId - 1));
        return true;
    }

    /** The picked fluid, or null. */
    public @Nullable Fluid getFluid() {
        int index = data.get(CreativeFluidTankBlockEntity.DATA_FLUID) - 1;
        List<Fluid> fluids = CreativeFluidTankBlockEntity.fluids();
        return index >= 0 && index < fluids.size() ? fluids.get(index) : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return tank == null || Container.stillValidBlockEntity(tank, player);
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
}
