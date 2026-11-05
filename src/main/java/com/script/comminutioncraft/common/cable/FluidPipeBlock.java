package com.script.comminutioncraft.common.cable;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * A fluid conduit; fluid moves through {@link PipeFluidHandler}. The Fluid Pipe carries any fluid, the Plasma Cable
 * only plasma (and faster). A pipe connects to any block with a fluid handler on that side that can hold something it
 * carries (so a Plasma Cable ignores a water generator), but never to another kind of conduit.
 */
public class FluidPipeBlock extends ConduitBlock {
    /** Fluid Pipe: any fluid. */
    public static final com.script.comminutioncraft.common.config.ConfigInt FLUID_PIPE_RATE = com.script.comminutioncraft.common.config.MachineConfig.CABLES_FLUID_PIPE;
    /** Plasma Cable: plasma only. */
    public static final com.script.comminutioncraft.common.config.ConfigInt PLASMA_CABLE_RATE = com.script.comminutioncraft.common.config.MachineConfig.CABLES_PLASMA_CABLE;

    private final java.util.function.IntSupplier transferRate;
    private final Predicate<FluidResource> carries;
    /** A fluid this pipe carries, used to ask neighbours whether they could hold it (EMPTY = any fluid). */
    private final Supplier<FluidResource> sample;

    public FluidPipeBlock(Properties properties, java.util.function.IntSupplier transferRate, Predicate<FluidResource> carries,
                          Supplier<FluidResource> sample) {
        super(properties);
        this.transferRate = transferRate;
        this.carries = carries;
        this.sample = sample;
    }

    /** A pipe for any fluid. */
    public FluidPipeBlock(Properties properties, java.util.function.IntSupplier transferRate) {
        this(properties, transferRate, resource -> true, () -> FluidResource.EMPTY);
    }

    /** Most mB one insert carries. */
    public int getTransferRate() {
        return transferRate.getAsInt();
    }

    public boolean carries(FluidResource resource) {
        return carries.test(resource);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        if (level.getBlockState(neighbourPos).getBlock() instanceof ConduitBlock) {
            return false;
        }
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, neighbourPos, side);
        if (handler == null) {
            return false;
        }
        FluidResource fluid = sample.get();
        if (fluid.isEmpty()) {
            return true;
        }
        for (int i = 0; i < handler.size(); i++) {
            if (handler.isValid(i, fluid)) {
                return true;
            }
        }
        return false;
    }
}
