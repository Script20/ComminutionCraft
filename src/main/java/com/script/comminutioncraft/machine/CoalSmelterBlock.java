package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CoalSmelterBlock extends FuelMachineBlock {
    public CoalSmelterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<? extends FuelMachineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.COAL_SMELTER.get();
    }
}
