package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Burns fuel, so it smokes and crackles like the tier-1 machines. Its firebox is the top half of the front. */
public class ElectricGeneratorBlock extends MachineBlock {
    public ElectricGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ElectricGeneratorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTRIC_GENERATOR.get();
    }

    @Override
    protected boolean burnsFuel() {
        return true;
    }

    @Override
    protected double getFireboxBottom() {
        return 9.0 / 16.0;
    }
}
