package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.registry.ModBlockEntities;
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
