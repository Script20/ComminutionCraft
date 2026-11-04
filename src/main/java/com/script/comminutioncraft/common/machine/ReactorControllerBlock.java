package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Fusion Reactor controller: the reactor's GUI and logic; it goes in the middle of one face of the 3x3x3. */
public class ReactorControllerBlock extends MachineBlock {
    public ReactorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<FusionReactorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FUSION_REACTOR.get();
    }
}
