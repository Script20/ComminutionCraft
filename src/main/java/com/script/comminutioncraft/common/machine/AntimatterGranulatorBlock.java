package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AntimatterGranulatorBlock extends MachineBlock {
    public AntimatterGranulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AntimatterGranulatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANTIMATTER_GRANULATOR.get();
    }
}
