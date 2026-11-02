package com.script.comminutioncraft.common.machine.control;

import com.script.comminutioncraft.common.machine.MachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/** A machine block entity with redstone and side settings (all our machines). */
public interface ControllableMachine {
    MachineControls getControls();

    /** Which way the machine's front points. */
    default Direction getFacing() {
        return ((BlockEntity) this).getBlockState().getValue(MachineBlock.FACING);
    }

    /** The side mode of a world-facing side, or INPUT for "no side" queries (e.g. internal access). */
    default SideMode sideMode(Direction worldSide) {
        return worldSide == null ? SideMode.INPUT : getControls().side(getFacing(), worldSide);
    }
}
