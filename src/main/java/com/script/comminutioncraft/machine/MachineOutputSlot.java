package com.script.comminutioncraft.machine;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Like vanilla's FurnaceResultSlot: take-only, and taking items out pays the XP the machine has stored up. */
public class MachineOutputSlot extends Slot {
    private final Player player;
    private int removeCount;

    public MachineOutputSlot(Player player, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack carried) {
        checkTakeAchievements(carried);
        super.onTake(player, carried);
    }

    @Override
    protected void onQuickCraft(ItemStack picked, int count) {
        removeCount += count;
        checkTakeAchievements(picked);
    }

    @Override
    protected void checkTakeAchievements(ItemStack carried) {
        carried.onCraftedBy(player, removeCount);
        if (player instanceof ServerPlayer serverPlayer && container instanceof MachineExperience.Holder machine) {
            machine.awardUsedRecipesAndPopExperience(serverPlayer);
        }
        removeCount = 0;
    }
}
