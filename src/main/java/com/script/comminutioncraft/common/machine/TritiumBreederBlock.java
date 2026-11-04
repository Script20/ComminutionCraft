package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TritiumBreederBlock extends MachineBlock {
    public TritiumBreederBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<TritiumBreederBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TRITIUM_BREEDER.get();
    }
}
