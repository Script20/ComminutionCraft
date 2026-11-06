package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AntimatterPurifierBlock extends MachineBlock {
    public AntimatterPurifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AntimatterPurifierBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANTIMATTER_PURIFIER.get();
    }
}
