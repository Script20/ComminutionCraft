package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PlasmaSmelterBlock extends MachineBlock {
    public PlasmaSmelterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<PlasmaSmelterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PLASMA_SMELTER.get();
    }
}
