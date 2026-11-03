package com.script.comminutioncraft.common.fluid;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * One-fluid tank for a machine. Other blocks go through the transfer API (or the {@link #insertOnly()} /
 * {@link #extractOnly()} views for one-way faces); the machine itself uses {@link #fill}/{@link #drain}
 * outside of transactions, like {@code MachineEnergyStorage}.
 */
public class MachineFluidTank extends FluidStacksResourceHandler {
    private final Predicate<FluidResource> accepts;
    private final Runnable onChanged;
    private final ResourceHandler<FluidResource> insertOnly = new OneWay(true);
    private final ResourceHandler<FluidResource> extractOnly = new OneWay(false);

    public MachineFluidTank(int capacity, Predicate<FluidResource> accepts, Runnable onChanged) {
        super(1, capacity);
        this.accepts = accepts;
        this.onChanged = onChanged;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return accepts.test(resource);
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        onChanged.run();
    }

    public FluidResource getFluid() {
        return getResource(0);
    }

    public int getAmount() {
        return getAmountAsInt(0);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return capacity - getAmount();
    }

    /** A copy of what's in the tank. */
    public FluidStack getStack() {
        return stacks.get(0).copy();
    }

    /** Adds up to {@code amount}, returning how much fit. Call outside of transactions (e.g. in a tick). */
    public int fill(FluidResource resource, int amount) {
        if (resource.isEmpty() || !isValid(0, resource) || (getAmount() > 0 && !resource.matches(stacks.get(0)))) {
            return 0;
        }
        int added = Math.min(getSpace(), amount);
        if (added > 0) {
            set(0, resource, getAmount() + added);
        }
        return added;
    }

    /** Removes up to {@code amount}, returning how much came out. Call outside of transactions. */
    public int drain(int amount) {
        int removed = Math.min(getAmount(), amount);
        if (removed > 0) {
            int left = getAmount() - removed;
            set(0, left > 0 ? getFluid() : FluidResource.EMPTY, left);
        }
        return removed;
    }

    /** For faces that only take fluid in. */
    public ResourceHandler<FluidResource> insertOnly() {
        return insertOnly;
    }

    /** For faces that only give fluid out. */
    public ResourceHandler<FluidResource> extractOnly() {
        return extractOnly;
    }

    // ---- Kept on the item when picked up with the wrench ----

    public void saveTo(DataComponentMap.Builder components) {
        if (getAmount() > 0) {
            components.set(ModDataComponents.STORED_FLUID.get(), SimpleFluidContent.copyOf(stacks.get(0)));
        }
    }

    public void loadFrom(DataComponentGetter components) {
        SimpleFluidContent saved = components.get(ModDataComponents.STORED_FLUID.get());
        if (saved != null && !saved.isEmpty()) {
            FluidStack stack = saved.copy();
            set(0, FluidResource.of(stack), Math.min(stack.getAmount(), capacity));
        }
    }

    private class OneWay extends DelegatingResourceHandler<FluidResource> {
        private final boolean in;

        OneWay(boolean in) {
            super(MachineFluidTank.this);
            this.in = in;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return in ? super.insert(index, resource, amount, transaction) : 0;
        }

        @Override
        public int insert(FluidResource resource, int amount, TransactionContext transaction) {
            return in ? super.insert(resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return in ? 0 : super.extract(index, resource, amount, transaction);
        }

        @Override
        public int extract(FluidResource resource, int amount, TransactionContext transaction) {
            return in ? 0 : super.extract(resource, amount, transaction);
        }
    }
}
