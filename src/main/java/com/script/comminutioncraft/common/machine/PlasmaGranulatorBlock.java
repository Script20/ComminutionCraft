package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PlasmaGranulatorBlock extends MachineBlock {
    public PlasmaGranulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<PlasmaGranulatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PLASMA_GRANULATOR.get();
    }
}
