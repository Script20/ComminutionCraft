package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Event Horizon Reactor port, in the frame: Input (takes items as matter, and antimatter to ignite) or Output (gives
 * FE), switched with the wrench like the Accelerator Port (it shares that block's {@code output} property and toggle).
 */
public class HorizonPortBlock extends AcceleratorPortBlock {
    public HorizonPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<? extends net.minecraft.world.level.block.entity.BlockEntity> getBlockEntityType() {
        return ModBlockEntities.HORIZON_PORT.get();
    }
}
