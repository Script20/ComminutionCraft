package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Creative Energy Battery: a machine block (it faces the player, for its front) without a GUI. */
public class CreativeEnergyBatteryBlock extends MachineBlock {
    public CreativeEnergyBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<CreativeEnergyBatteryBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CREATIVE_ENERGY_BATTERY.get();
    }
}
