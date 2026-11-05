package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Tier 4 Isotope Separator: a centrifuge that spins the heavy water out of ordinary water. Every
 * {@link #WATER_PER_MB} mB of water gives 1 mB of deuterium, up to {@link #MAX_DEUTERIUM_PER_TICK} mB/t at
 * {@link #ENERGY_PER_MB} FE per mB. Water comes in through Input faces; deuterium goes out of Output faces.
 */
public class IsotopeSeparatorBlockEntity extends FluidMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt WATER_PER_MB = com.script.comminutioncraft.common.config.MachineConfig.ISOTOPE_SEPARATOR_WATER_PER_DEUTERIUM;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_DEUTERIUM_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.ISOTOPE_SEPARATOR_DEUTERIUM_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_MB = com.script.comminutioncraft.common.config.MachineConfig.ISOTOPE_SEPARATOR_ENERGY_PER_MB;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ISOTOPE_SEPARATOR_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.ISOTOPE_SEPARATOR_MAX_ENERGY_INPUT;

    public static final int TANK_WATER = 0;
    public static final int TANK_DEUTERIUM = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank water = addTank("water", resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), TankRole.INPUT);
    private final MachineFluidTank deuterium = addTank("deuterium", resource -> resource.is(ModFluids.DEUTERIUM_TYPE.get()), TankRole.OUTPUT);

    public IsotopeSeparatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ISOTOPE_SEPARATOR.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected int work(ServerLevel level) {
        int make = Math.min(Math.min(MAX_DEUTERIUM_PER_TICK.get(), water.getAmount() / WATER_PER_MB.get()),
                Math.min(deuterium.getSpace(), energy.getEnergy() / ENERGY_PER_MB.get()));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * ENERGY_PER_MB.get());
        water.drain(make * WATER_PER_MB.get());
        deuterium.fill(FluidResource.of(ModFluids.DEUTERIUM.get()), make);
        return make;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.ISOTOPE_SEPARATOR.get();
    }
}
