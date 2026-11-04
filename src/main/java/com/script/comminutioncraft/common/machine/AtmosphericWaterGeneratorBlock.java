package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Atmospheric Water Generator: a plain machine block (no fuel fire); the block entity does the breathing. */
public class AtmosphericWaterGeneratorBlock extends MachineBlock {
    public AtmosphericWaterGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AtmosphericWaterGeneratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get();
    }
}
