package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class IsotopeSeparatorBlock extends MachineBlock {
    public IsotopeSeparatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<IsotopeSeparatorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ISOTOPE_SEPARATOR.get();
    }
}
