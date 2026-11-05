package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Creative Fluid Tank: a Fluid Tank (buckets work, window drawn by the renderer) whose right-click opens the fluid picker. */
public class CreativeFluidTankBlock extends FluidTankBlock {
    public CreativeFluidTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<CreativeFluidTankBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CREATIVE_FLUID_TANK.get();
    }
}
