package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Tier 4 Isotope Separator: a centrifuge that spins the heavy water out of ordinary water. Every
 * {@link #WATER_PER_MB} mB of water gives 1 mB of deuterium, up to {@link #MAX_DEUTERIUM_PER_TICK} mB/t at
 * {@link #ENERGY_PER_MB} FE per mB. Water comes in through Input faces; deuterium goes out of Output faces.
 */
public class IsotopeSeparatorBlockEntity extends FluidMachineBlockEntity {
    public static final ConfigInt WATER_PER_MB = MachineConfig.ISOTOPE_SEPARATOR_WATER_PER_DEUTERIUM;
    public static final ConfigInt MAX_DEUTERIUM_PER_TICK = MachineConfig.ISOTOPE_SEPARATOR_DEUTERIUM_PER_TICK;
    public static final ConfigInt ENERGY_PER_MB = MachineConfig.ISOTOPE_SEPARATOR_ENERGY_PER_MB;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.ISOTOPE_SEPARATOR_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_INPUT = MachineConfig.ISOTOPE_SEPARATOR_MAX_ENERGY_INPUT;

    public static final int TANK_WATER = 0;
    public static final int TANK_DEUTERIUM = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank water = addTank("water", resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), TankRole.INPUT);
    private final MachineFluidTank deuterium = addTank("deuterium", resource -> resource.is(ModFluids.DEUTERIUM_TYPE.get()), TankRole.OUTPUT);

    public IsotopeSeparatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ISOTOPE_SEPARATOR.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected Collection<Fluid> consumedFluids() {
        return List.of(Fluids.WATER);
    }

    @Override
    protected int work(ServerLevel level) {
        return convert(water, Fluids.WATER, WATER_PER_MB.get(),
                deuterium, ModFluids.DEUTERIUM.get(), MAX_DEUTERIUM_PER_TICK.get(), ENERGY_PER_MB.get());
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.ISOTOPE_SEPARATOR.get();
    }
}
