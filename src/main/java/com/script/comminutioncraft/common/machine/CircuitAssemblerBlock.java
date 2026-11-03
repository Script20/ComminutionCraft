package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CircuitAssemblerBlock extends MachineBlock {
    public CircuitAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<CircuitAssemblerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CIRCUIT_ASSEMBLER.get();
    }
}
