package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CoalGranulatorBlock extends FuelMachineBlock {
    public CoalGranulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<? extends FuelMachineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COAL_GRANULATOR.get();
    }
}
