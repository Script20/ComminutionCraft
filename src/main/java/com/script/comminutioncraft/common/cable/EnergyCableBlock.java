package com.script.comminutioncraft.common.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Energy cable: connects to anything with FE on that side. Power moves through {@link CableEnergyHandler}. */
public class EnergyCableBlock extends ConduitBlock {
    public EnergyCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return level.getCapability(Capabilities.Energy.BLOCK, neighbourPos, side) != null;
    }
}
