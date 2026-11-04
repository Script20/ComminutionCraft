package com.script.comminutioncraft.common.machine.control;

import com.script.comminutioncraft.common.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

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

    /**
     * Pushes finished items out of every Output face into the block there (another machine's Input face, a chest,
     * a hopper...), up to a stack per face per call. Uses our own item capability on that face, so only what that face
     * gives out moves (results, and a fuel machine's empty buckets).
     */
    default void pushItemsOut(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (sideMode(direction) != SideMode.OUTPUT) {
                continue;
            }
            ResourceHandler<ItemResource> from = level.getCapability(Capabilities.Item.BLOCK, pos, direction);
            ResourceHandler<ItemResource> to = level.getCapability(Capabilities.Item.BLOCK, pos.relative(direction), direction.getOpposite());
            if (from != null && to != null) {
                ResourceHandlerUtil.move(from, to, resource -> true, 64, null);
            }
        }
    }

    /** The side mode of a world-facing side, or INPUT for "no side" queries (e.g. internal access). */
    default SideMode sideMode(Direction worldSide) {
        return worldSide == null ? SideMode.INPUT : getControls().side(getFacing(), worldSide);
    }
}
