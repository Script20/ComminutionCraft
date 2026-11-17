package com.script.comminutioncraft.common.cable;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import java.util.function.IntSupplier;
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
    public static final ConfigInt ENERGY_CABLE_RATE = MachineConfig.CABLES_ENERGY_CABLE;
    /** Tier 4 Fusion Cable. */
    public static final ConfigInt FUSION_CABLE_RATE = MachineConfig.CABLES_FUSION_CABLE;
    public static final ConfigInt ANTIMATTER_CABLE_RATE = MachineConfig.CABLES_ANTIMATTER_CABLE;
    public static final ConfigInt SINGULARITY_CABLE_RATE = MachineConfig.CABLES_SINGULARITY_CABLE;

    private final IntSupplier transferRate;

    public EnergyCableBlock(Properties properties, IntSupplier transferRate) {
        super(properties);
        this.transferRate = transferRate;
    }

    public int getTransferRate() {
        return transferRate.getAsInt();
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return !(level.getBlockState(neighbourPos).getBlock() instanceof ConduitBlock)
                && level.getCapability(Capabilities.Energy.BLOCK, neighbourPos, side) != null;
    }
}
