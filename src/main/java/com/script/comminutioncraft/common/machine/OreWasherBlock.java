package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OreWasherBlock extends MachineBlock {
    public OreWasherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<OreWasherBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ORE_WASHER.get();
    }
}
