package com.script.comminutioncraft.common.machine;

import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** The Antimatter Washing Factory: a washer block (water buckets pour in) with a lane-washer block entity. */
public class WashingFactoryBlock extends OreWasherBlock {
    private final Supplier<? extends BlockEntityType<? extends OreWasherBlockEntity>> type;

    public WashingFactoryBlock(Properties properties, Supplier<? extends BlockEntityType<? extends OreWasherBlockEntity>> type) {
        super(properties);
        this.type = type;
    }

    @Override
    protected BlockEntityType<? extends OreWasherBlockEntity> getBlockEntityType() {
        return type.get();
    }
}
