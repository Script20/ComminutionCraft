package com.script.comminutioncraft.common.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Energy cable: connects to anything with FE on that side, except other kinds of conduit. Power moves through
 * {@link CableEnergyHandler}, at most {@link #getTransferRate()} FE per tick per network.
 */
public class EnergyCableBlock extends ConduitBlock {
    /** Tier 2 Energy Cable. */
    public static final int ENERGY_CABLE_RATE = 1_000;

    private final int transferRate;

    public EnergyCableBlock(Properties properties, int transferRate) {
        super(properties);
        this.transferRate = transferRate;
    }

    public int getTransferRate() {
        return transferRate;
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return !(level.getBlockState(neighbourPos).getBlock() instanceof ConduitBlock)
                && level.getCapability(Capabilities.Energy.BLOCK, neighbourPos, side) != null;
    }
}
