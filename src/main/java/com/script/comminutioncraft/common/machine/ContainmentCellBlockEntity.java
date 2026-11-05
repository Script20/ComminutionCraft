package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Containment Cell (tier 5): a Fluid Tank for antimatter only, which no ordinary tank or pipe will take. It needs no
 * power (owner's call). Same faces, wrench pickup (keeps the antimatter), comparator and window renderer as the tank.
 */
public class ContainmentCellBlockEntity extends FluidTankBlockEntity {
    public static final ConfigInt CAPACITY = MachineConfig.CONTAINMENT_CELL_CAPACITY;
    public static final ConfigInt OUTPUT_RATE = MachineConfig.CONTAINMENT_CELL_OUTPUT_RATE;

    public ContainmentCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTAINMENT_CELL.get(), pos, state, CAPACITY.get(), ModFluids::isAntimatter);
    }

    @Override
    protected int outputRate() {
        return OUTPUT_RATE.get();
    }
}
