package com.script.comminutioncraft.common.cable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * What a pipe exposes as its fluid capability. Like {@link CableEnergyHandler}, pipes store nothing: fluid inserted
 * into any pipe is handed straight to every fluid block on the same network (except the one it came from) that
 * accepts that fluid, split evenly. Different fluids never mix because nothing waits inside the pipe.
 * Each insert carries at most {@link #TRANSFER_RATE} mB.
 */
public record PipeFluidHandler(Level level, BlockPos pos, @Nullable Direction side) implements ResourceHandler<FluidResource> {
    public static final int TRANSFER_RATE = 1000;

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return 0;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return TRANSFER_RATE;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return true;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || level.isClientSide()) {
            return 0;
        }
        CableNetworks.Network network = CableNetworks.get(level, pos);
        if (network.busy) {
            return 0;
        }
        network.busy = true;
        try {
            List<ResourceHandler<FluidResource>> targets = findTargets(network);
            int offered = Math.min(amount, TRANSFER_RATE);
            int remaining = offered;
            for (int i = 0; i < targets.size() && remaining > 0; i++) {
                // Even split; whatever one target refuses is offered to the ones after it.
                int share = Math.ceilDiv(remaining, targets.size() - i);
                remaining -= targets.get(i).insert(resource, share, transaction);
            }
            return offered - remaining;
        } finally {
            network.busy = false;
        }
    }

    private List<ResourceHandler<FluidResource>> findTargets(CableNetworks.Network network) {
        BlockPos source = side != null ? pos.relative(side) : null;
        List<ResourceHandler<FluidResource>> targets = new ArrayList<>();
        for (CableNetworks.Endpoint endpoint : network.endpoints()) {
            if (endpoint.pos().equals(source) || !level.isLoaded(endpoint.pos())) {
                continue;
            }
            ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, endpoint.pos(), endpoint.face());
            if (handler != null && !(handler instanceof PipeFluidHandler)) {
                targets.add(handler);
            }
        }
        return targets;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
