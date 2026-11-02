package com.script.comminutioncraft.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** A machine block entity that runs logic every server tick. Hooked up by {@link MachineBlock#getTicker}. */
public interface TickingMachine {
    void serverTick(ServerLevel level, BlockPos pos, BlockState state);
}
