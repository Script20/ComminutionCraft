package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.registry.ModBlockEntities;
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
