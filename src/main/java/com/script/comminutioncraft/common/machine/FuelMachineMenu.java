package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RedstoneMode;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Furnace-style menu shared by all tier-1 machines. Slot layout matches the vanilla furnace. */
public class FuelMachineMenu extends AbstractContainerMenu implements ControllableMenu {
    private static final int MACHINE_SLOTS = FuelMachineBlockEntity.SLOT_COUNT;
    private static final int INV_START = MACHINE_SLOTS;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final RecipeType<?> fuelRecipeType;

    // Client-side constructors (called when the server tells the client to open the GUI)
    public static FuelMachineMenu clientGranulator(int containerId, Inventory inventory) {
        return new FuelMachineMenu(ModMenus.COAL_GRANULATOR.get(), containerId, inventory,
                new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(FuelMachineBlockEntity.DATA_COUNT),
                ModRecipes.GRANULATING_TYPE.get());
    }

    public static FuelMachineMenu clientSmelter(int containerId, Inventory inventory) {
        return new FuelMachineMenu(ModMenus.COAL_SMELTER.get(), containerId, inventory,
                new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(FuelMachineBlockEntity.DATA_COUNT),
                RecipeType.SMELTING);
    }

    public FuelMachineMenu(MenuType<?> type, int containerId, Inventory inventory, Container container,
                           ContainerData data, RecipeType<?> fuelRecipeType) {
        super(type, containerId);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, FuelMachineBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.level = inventory.player.level();
        this.fuelRecipeType = fuelRecipeType;

        addSlot(new Slot(container, FuelMachineBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new FuelSlot(container, FuelMachineBlockEntity.SLOT_FUEL, 56, 53));
        addSlot(new MachineOutputSlot(inventory.player, container, FuelMachineBlockEntity.SLOT_OUTPUT, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    // ---- Redstone / side settings (synced through data slots, changed with button clicks) ----

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return MachineControls.handleMenuButton(container, buttonId);
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return RedstoneMode.byOrdinal(data.get(FuelMachineBlockEntity.DATA_REDSTONE));
    }

    @Override
    public SideMode getSideMode(RelativeSide side) {
        return MachineControls.unpackSide(data.get(FuelMachineBlockEntity.DATA_SIDES), side);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return original;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();

        if (index == FuelMachineBlockEntity.SLOT_OUTPUT) {
            if (!moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isFuel(stack)) {
            if (!moveItemStackTo(stack, FuelMachineBlockEntity.SLOT_FUEL, FuelMachineBlockEntity.SLOT_FUEL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, FuelMachineBlockEntity.SLOT_INPUT, FuelMachineBlockEntity.SLOT_INPUT + 1, false)) {
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

    public boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(fuelRecipeType, level.fuelValues()) > 0;
    }

    // ---- GUI progress helpers (0.0 - 1.0) ----

    public boolean isLit() {
        return data.get(FuelMachineBlockEntity.DATA_LIT_TIME) > 0;
    }

    public float getBurnProgress() {
        int progress = data.get(FuelMachineBlockEntity.DATA_PROGRESS);
        int total = data.get(FuelMachineBlockEntity.DATA_PROGRESS_TOTAL);
        return total != 0 && progress != 0 ? Math.clamp((float) progress / total, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int duration = data.get(FuelMachineBlockEntity.DATA_LIT_DURATION);
        if (duration == 0) {
            duration = 200;
        }
        return Math.clamp((float) data.get(FuelMachineBlockEntity.DATA_LIT_TIME) / duration, 0.0F, 1.0F);
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
