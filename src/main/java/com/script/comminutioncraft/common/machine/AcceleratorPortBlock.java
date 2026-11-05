package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Particle Accelerator port: a machine block (facing for the side config, LIT while the ring runs) without a GUI. */
public class AcceleratorPortBlock extends MachineBlock {
    public AcceleratorPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AcceleratorPortBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ACCELERATOR_PORT.get();
    }
}
