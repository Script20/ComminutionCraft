package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Creative Chest GUI: one "ghost" slot showing the picked item. Clicking it with an item picks that item (the item
 * stays in your hand), clicking it empty-handed clears it, shift-clicking an inventory stack picks that stack's item.
 * JEI drags go through {@link com.script.comminutioncraft.common.network.SetCreativeChestItemPayload}.
 */
public class CreativeChestMenu extends AbstractContainerMenu {
    public static final int SLOT_X = 80;
    public static final int SLOT_Y = 35;
    private static final int PICK = 0;

    private final @Nullable CreativeChestBlockEntity chest;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static CreativeChestMenu client(int containerId, Inventory inventory) {
        return new CreativeChestMenu(containerId, inventory, null, new SimpleContainer(1));
    }

    public CreativeChestMenu(int containerId, Inventory inventory, CreativeChestBlockEntity chest) {
        this(containerId, inventory, chest, chest.shown());
    }

    private CreativeChestMenu(int containerId, Inventory inventory, @Nullable CreativeChestBlockEntity chest, Container shown) {
        super(ModMenus.CREATIVE_CHEST.get(), containerId);
        this.chest = chest;
        addSlot(new Slot(shown, 0, SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
    }

    /** Picks the item (EMPTY clears it); server side only. */
    public void pick(ItemStack stack) {
        if (chest != null) {
            chest.setItem(stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack));
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (slotIndex == PICK) {
            pick(getCarried());
            return;
        }
        super.clicked(slotIndex, button, input, player);
    }

    /** Shift-click on an inventory stack picks its item; nothing moves. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index != PICK && index < slots.size()) {
            ItemStack stack = slots.get(index).getItem();
            if (!stack.isEmpty()) {
                pick(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return chest == null || Container.stillValidBlockEntity(chest, player);
    }
}
