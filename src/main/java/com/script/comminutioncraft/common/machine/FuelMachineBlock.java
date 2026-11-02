package com.script.comminutioncraft.common.machine;

import net.minecraft.world.level.block.entity.BlockEntityType;

/** Tier-1 machine block: burns fuel, so it smokes and crackles while lit. */
public abstract class FuelMachineBlock extends MachineBlock {
    protected FuelMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected abstract BlockEntityType<? extends FuelMachineBlockEntity> getBlockEntityType();

    @Override
    protected boolean burnsFuel() {
        return true;
    }
}
