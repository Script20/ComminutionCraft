package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AntimatterWasherBlock extends MachineBlock {
    public AntimatterWasherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AntimatterWasherBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANTIMATTER_WASHER.get();
    }
}
