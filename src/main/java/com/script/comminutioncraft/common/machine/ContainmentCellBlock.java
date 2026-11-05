package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Containment Cell: a Fluid Tank block that holds antimatter (see {@link ContainmentCellBlockEntity}). */
public class ContainmentCellBlock extends FluidTankBlock {
    public ContainmentCellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ContainmentCellBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CONTAINMENT_CELL.get();
    }
}
