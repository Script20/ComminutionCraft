package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ElectrolyserBlock extends MachineBlock {
    public ElectrolyserBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ElectrolyserBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTROLYSER.get();
    }
}
