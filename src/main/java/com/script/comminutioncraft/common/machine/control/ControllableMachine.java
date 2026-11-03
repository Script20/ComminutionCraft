package com.script.comminutioncraft.common.machine.control;

import com.script.comminutioncraft.common.machine.MachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** A machine block entity with redstone and side settings (all our machines). */
public interface ControllableMachine {
    MachineControls getControls();

    /** Which way the machine's front points. */
    default Direction getFacing() {
        return ((BlockEntity) this).getBlockState().getValue(MachineBlock.FACING);
    }

    /**
     * Call after the side settings change: drops cached capabilities (hoppers, pipes) and makes neighbouring cables
     * and pipes re-check their connection to us, since a face may have gained or lost its FE / fluid capability.
     */
    default void refreshFaces() {
        BlockEntity blockEntity = (BlockEntity) this;
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(blockEntity.getBlockPos());
            blockEntity.getBlockState().updateNeighbourShapes(level, blockEntity.getBlockPos(), Block.UPDATE_ALL);
        }
    }

    /** The side mode of a world-facing side, or INPUT for "no side" queries (e.g. internal access). */
    default SideMode sideMode(Direction worldSide) {
        return worldSide == null ? SideMode.INPUT : getControls().side(getFacing(), worldSide);
    }
}
