package com.script.comminutioncraft.common.machine;

import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** A factory: a processing machine with several lanes (see {@link Factories}). Its block entity is the machine's own class. */
public class FactoryBlock extends MachineBlock {
    private final Supplier<? extends BlockEntityType<? extends ElectricMachineBlockEntity>> type;

    public FactoryBlock(Properties properties, Supplier<? extends BlockEntityType<? extends ElectricMachineBlockEntity>> type) {
        super(properties);
        this.type = type;
    }

    @Override
    protected BlockEntityType<? extends ElectricMachineBlockEntity> getBlockEntityType() {
        return type.get();
    }
}
