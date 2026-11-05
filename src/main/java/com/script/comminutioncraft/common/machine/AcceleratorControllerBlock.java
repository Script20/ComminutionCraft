package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Particle Accelerator controller: the accelerator's GUI and logic; it goes anywhere in the ring. */
public class AcceleratorControllerBlock extends MachineBlock {
    public AcceleratorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ParticleAcceleratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PARTICLE_ACCELERATOR.get();
    }
}
