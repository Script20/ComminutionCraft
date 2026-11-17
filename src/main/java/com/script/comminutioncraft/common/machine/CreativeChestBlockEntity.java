package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Creative Chest (creative only, no recipe): endless items of one picked kind. Anything can pull as many as it likes,
 * and every tick it pushes a full stack into the block on each face. It takes nothing in. The item is picked in its
 * one-slot GUI (or dragged in from JEI) and kept on a wrench pickup.
 */
public class CreativeChestBlockEntity extends BlockEntity implements MenuProvider, TickingMachine {
    private ItemResource item = ItemResource.EMPTY;
    /** What the GUI slot shows: one of the picked item (the menu syncs it like any slot). */
    private final SimpleContainer shown = new SimpleContainer(1);
    private final ResourceHandler<ItemResource> endless = new Endless();

    public CreativeChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_CHEST.get(), pos, state);
    }

    public ItemResource getItem() {
        return item;
    }

    /** Picks the item it gives (EMPTY = nothing). */
    public void setItem(ItemResource picked) {
        item = picked;
        shown.setItem(0, picked.isEmpty() ? ItemStack.EMPTY : picked.toStack(1));
        setChanged();
    }

    SimpleContainer shown() {
        return shown;
    }

    /** Every face (and "no side") gives the item endlessly. */
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return endless;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (item.isEmpty()) {
            return;
        }
        for (Direction direction : Direction.values()) {
            ResourceHandler<ItemResource> target = level.getCapability(Capabilities.Item.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                ResourceHandlerUtil.move(endless, target, resource -> true, item.getMaxStackSize(), null);
            }
        }
    }

    /** Always holds "lots" of the picked item, gives any amount of it, takes nothing. */
    private final class Endless implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            return item;
        }

        @Override
        public long getAmountAsLong(int index) {
            return item.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return false;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return !item.isEmpty() && resource.equals(item) ? amount : 0;
        }
    }

    // ---- Saving, and the wrench pickup ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setItem(input.read("creative_item", ItemResource.CODEC).orElse(ItemResource.EMPTY));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!item.isEmpty()) {
            output.store("creative_item", ItemResource.CODEC, item);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!item.isEmpty()) {
            components.set(ModDataComponents.CREATIVE_ITEM.get(), item);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        setItem(components.getOrDefault(ModDataComponents.CREATIVE_ITEM.get(), ItemResource.EMPTY));
    }

    // ---- GUI ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CreativeChestMenu(containerId, inventory, this);
    }
}
