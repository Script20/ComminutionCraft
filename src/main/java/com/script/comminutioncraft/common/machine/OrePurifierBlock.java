package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OrePurifierBlock extends MachineBlock {
    public OrePurifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<OrePurifierBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ORE_PURIFIER.get();
    }
}
