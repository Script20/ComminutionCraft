package com.script.comminutioncraft.common.storage;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.config.ComminutionConfig;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Soaryn Chest (from the owner's standalone SoarynChest mod): a normal chest that, when placed, pulls the placer's
 * inventory into it — the main 27 slots, plus the hotbar if {@link ComminutionConfig#SOARYN_CHEST_INCLUDE_HOTBAR}.
 * After that it is an ordinary chest (opening, double chests, hoppers, comparators).
 */
public class SoarynChestBlock extends ChestBlock {
    public static final MapCodec<SoarynChestBlock> CODEC = simpleCodec(SoarynChestBlock::new);

    /** Player inventory slots: 0-8 hotbar, 9-35 main. */
    private static final int HOTBAR_SIZE = 9;
    private static final int MAIN_END = 36;

    public SoarynChestBlock(Properties properties) {
        super(ModBlockEntities.SOARYN_CHEST::get, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
    }

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoarynChestBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(placer instanceof Player player)
                || !(level.getBlockEntity(pos) instanceof SoarynChestBlockEntity chest)) {
            return;
        }
        absorb(player.getInventory(), chest, ComminutionConfig.SOARYN_CHEST_INCLUDE_HOTBAR.get());
    }

    /** Moves the player's stacks into the chest's slots in order, until either runs out. */
    public static void absorb(Inventory inventory, SoarynChestBlockEntity chest, boolean includeHotbar) {
        int slot = 0;
        for (int i = includeHotbar ? 0 : HOTBAR_SIZE; i < MAIN_END && slot < chest.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                chest.setItem(slot++, item.copy());
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        chest.setChanged();
    }
}
