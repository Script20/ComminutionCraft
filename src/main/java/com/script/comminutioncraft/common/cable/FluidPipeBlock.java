package com.script.comminutioncraft.common.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Fluid pipe: connects to anything with a fluid handler on that side. Fluid moves through {@link PipeFluidHandler}. */
public class FluidPipeBlock extends ConduitBlock {
    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return level.getCapability(Capabilities.Fluid.BLOCK, neighbourPos, side) != null;
    }
}
