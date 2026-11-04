package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Plasma Generator: a plain machine block (no fuel fire); hydrogen in, plasma out (see the block entity). */
public class PlasmaGeneratorBlock extends MachineBlock {
    public PlasmaGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<PlasmaGeneratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PLASMA_GENERATOR.get();
    }
}
