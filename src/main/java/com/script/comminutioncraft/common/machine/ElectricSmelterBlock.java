package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ElectricSmelterBlock extends MachineBlock {
    public ElectricSmelterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ElectricSmelterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTRIC_SMELTER.get();
    }
}
