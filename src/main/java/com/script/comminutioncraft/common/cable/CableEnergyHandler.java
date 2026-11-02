package com.script.comminutioncraft.common.cable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * What a cable exposes as its FE capability. Cables store nothing: energy inserted into any cable is handed
 * straight to every FE block on the same network (except the one it came from), split evenly.
 * Each insert carries at most {@link #TRANSFER_RATE} FE.
 */
public record CableEnergyHandler(Level level, BlockPos pos, @Nullable Direction side) implements EnergyHandler {
    public static final int TRANSFER_RATE = 1000;

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return TRANSFER_RATE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0 || level.isClientSide()) {
            return 0;
        }
        CableNetworks.Network network = CableNetworks.get(level, pos);
        if (network.busy) {
            return 0;
        }
        network.busy = true;
        try {
            List<EnergyHandler> targets = findTargets(network);
            int offered = Math.min(amount, TRANSFER_RATE);
            int remaining = offered;
            for (int i = 0; i < targets.size() && remaining > 0; i++) {
                // Even split; whatever one target refuses is offered to the ones after it.
                int share = Math.ceilDiv(remaining, targets.size() - i);
                remaining -= targets.get(i).insert(share, transaction);
            }
            return offered - remaining;
        } finally {
            network.busy = false;
        }
    }

    private List<EnergyHandler> findTargets(CableNetworks.Network network) {
        BlockPos source = side != null ? pos.relative(side) : null;
        List<EnergyHandler> targets = new ArrayList<>();
        for (CableNetworks.Endpoint endpoint : network.endpoints()) {
            if (endpoint.pos().equals(source) || !level.isLoaded(endpoint.pos())) {
                continue;
            }
            EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, endpoint.pos(), endpoint.face());
            if (handler != null && !(handler instanceof CableEnergyHandler)) {
                targets.add(handler);
            }
        }
        return targets;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
