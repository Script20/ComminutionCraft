package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AnnihilationGeneratorBlock extends MachineBlock {
    public AnnihilationGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AnnihilationGeneratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANNIHILATION_GENERATOR.get();
    }
}
