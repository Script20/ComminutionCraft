package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PelletPressBlock extends MachineBlock {
    public PelletPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<PelletPressBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PELLET_PRESS.get();
    }
}
