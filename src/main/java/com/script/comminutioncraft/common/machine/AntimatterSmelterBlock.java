package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AntimatterSmelterBlock extends MachineBlock {
    public AntimatterSmelterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AntimatterSmelterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANTIMATTER_SMELTER.get();
    }
}
