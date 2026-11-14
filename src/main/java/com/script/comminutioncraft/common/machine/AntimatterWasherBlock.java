package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** The tier-5 Ore Washer: same block behaviour (water buckets pour in), its own block entity. */
public class AntimatterWasherBlock extends OreWasherBlock {
    public AntimatterWasherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<AntimatterWasherBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ANTIMATTER_WASHER.get();
    }
}
