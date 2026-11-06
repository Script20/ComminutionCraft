package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FrothFlotationCellBlock extends MachineBlock {
    public FrothFlotationCellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<FrothFlotationCellBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FROTH_FLOTATION_CELL.get();
    }
}
