package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** Tier 5 Ore Purifier: the same purifying recipes (and by-products) on oxygen, much faster and thriftier. */
public class AntimatterPurifierBlockEntity extends OrePurifierBlockEntity {
    public static final ConfigInt PROCESS_TIME = MachineConfig.ANTIMATTER_PURIFIER_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.ANTIMATTER_PURIFIER_ENERGY_PER_TICK;
    public static final ConfigInt OXYGEN_PER_OPERATION = MachineConfig.ANTIMATTER_PURIFIER_OXYGEN_PER_OPERATION;

    public AntimatterPurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_PURIFIER.get(), pos, state,
                MachineConfig.ANTIMATTER_MACHINES_ENERGY_CAPACITY.get(), MachineConfig.ANTIMATTER_MACHINES_MAX_ENERGY_INPUT.get());
    }

    @Override
    protected int getProcessTime() {
        return PROCESS_TIME.get();
    }

    @Override
    protected int getEnergyPerTick() {
        return ENERGY_PER_TICK.get();
    }

    @Override
    protected int oxygenPerOperation() {
        return OXYGEN_PER_OPERATION.get();
    }

    @Override
    protected MenuType<OrePurifierMenu> menuType() {
        return ModMenus.ANTIMATTER_PURIFIER.get();
    }
}
