package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** Tier 5 Ore Washer: the same washing recipes (raw ore -> 3 washed, concentrate -> 1) and water, much faster. */
public class AntimatterWasherBlockEntity extends OreWasherBlockEntity {
    public static final ConfigInt PROCESS_TIME = MachineConfig.ANTIMATTER_WASHER_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.ANTIMATTER_WASHER_ENERGY_PER_TICK;
    public static final ConfigInt WATER_PER_OPERATION = MachineConfig.ANTIMATTER_WASHER_WATER_PER_OPERATION;

    public AntimatterWasherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_WASHER.get(), pos, state,
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
    protected int waterPerOperation() {
        return WATER_PER_OPERATION.get();
    }

    @Override
    protected MenuType<OreWasherMenu> menuType() {
        return ModMenus.ANTIMATTER_WASHER.get();
    }
}
