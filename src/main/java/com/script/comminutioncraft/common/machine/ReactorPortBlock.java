package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Fusion Reactor port: a machine block (facing for the side config, LIT while the reactor burns) without a GUI. */
public class ReactorPortBlock extends MachineBlock {
    public ReactorPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<ReactorPortBlockEntity> getBlockEntityType() {
        return ModBlockEntities.REACTOR_PORT.get();
    }
}
