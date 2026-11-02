package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ElectricGranulatorBlock extends MachineBlock {
    public ElectricGranulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ElectricGranulatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTRIC_GRANULATOR.get();
    }
}
