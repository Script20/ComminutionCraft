package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MhdGeneratorBlock extends MachineBlock {
    public MhdGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<MhdGeneratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MHD_GENERATOR.get();
    }
}
