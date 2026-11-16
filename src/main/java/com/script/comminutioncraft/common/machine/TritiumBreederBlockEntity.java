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

/**
 * Tier 4 Tritium Breeder: bombards deuterium into tritium, 1 mB for 1 mB, up to {@link #MAX_TRITIUM_PER_TICK} mB/t at
 * a hefty {@link #ENERGY_PER_MB} FE per mB. It primes a Fusion Reactor and tops it up (the reactor breeds most of its
 * own tritium once it runs). Deuterium comes in through Input faces; tritium goes out of Output faces.
 */
public class TritiumBreederBlockEntity extends FluidMachineBlockEntity {
    public static final ConfigInt MAX_TRITIUM_PER_TICK = MachineConfig.TRITIUM_BREEDER_TRITIUM_PER_TICK;
    public static final ConfigInt ENERGY_PER_MB = MachineConfig.TRITIUM_BREEDER_ENERGY_PER_MB;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.TRITIUM_BREEDER_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_INPUT = MachineConfig.TRITIUM_BREEDER_MAX_ENERGY_INPUT;

    public static final int TANK_DEUTERIUM = 0;
    public static final int TANK_TRITIUM = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank deuterium = addTank("deuterium", resource -> resource.is(ModFluids.DEUTERIUM_TYPE.get()), TankRole.INPUT);
    private final MachineFluidTank tritium = addTank("tritium", resource -> resource.is(ModFluids.TRITIUM_TYPE.get()), TankRole.OUTPUT);

    public TritiumBreederBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRITIUM_BREEDER.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected Collection<Fluid> consumedFluids() {
        return List.of(ModFluids.DEUTERIUM.get());
    }

    @Override
    protected int work(ServerLevel level) {
        return convert(deuterium, ModFluids.DEUTERIUM.get(), 1, tritium, ModFluids.TRITIUM.get(),
                MAX_TRITIUM_PER_TICK.get(), ENERGY_PER_MB.get());
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.TRITIUM_BREEDER.get();
    }
}
