package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SingularityProcessorBlock extends MachineBlock {
    public SingularityProcessorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<SingularityProcessorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SINGULARITY_PROCESSOR.get();
    }
}
