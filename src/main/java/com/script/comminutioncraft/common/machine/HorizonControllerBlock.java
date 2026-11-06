package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Event Horizon Reactor controller: the middle block of one bottom edge of the 7x7x7, facing out. */
public class HorizonControllerBlock extends MachineBlock {
    public HorizonControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<EventHorizonReactorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.EVENT_HORIZON_REACTOR.get();
    }
}
