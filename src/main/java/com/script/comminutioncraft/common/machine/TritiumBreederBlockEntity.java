package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Tier 4 Tritium Breeder: bombards deuterium into tritium, 1 mB for 1 mB, up to {@link #MAX_TRITIUM_PER_TICK} mB/t at
 * a hefty {@link #ENERGY_PER_MB} FE per mB. It primes a Fusion Reactor and tops it up (the reactor breeds most of its
 * own tritium once it runs). Deuterium comes in through Input faces; tritium goes out of Output faces.
 */
public class TritiumBreederBlockEntity extends FluidMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_TRITIUM_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.TRITIUM_BREEDER_TRITIUM_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_MB = com.script.comminutioncraft.common.config.MachineConfig.TRITIUM_BREEDER_ENERGY_PER_MB;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.TRITIUM_BREEDER_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.TRITIUM_BREEDER_MAX_ENERGY_INPUT;

    public static final int TANK_DEUTERIUM = 0;
    public static final int TANK_TRITIUM = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank deuterium = addTank("deuterium", resource -> resource.is(ModFluids.DEUTERIUM_TYPE.get()), TankRole.INPUT);
    private final MachineFluidTank tritium = addTank("tritium", resource -> resource.is(ModFluids.TRITIUM_TYPE.get()), TankRole.OUTPUT);

    public TritiumBreederBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRITIUM_BREEDER.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected int work(ServerLevel level) {
        int perMb = upgradedEnergy(ENERGY_PER_MB.get());
        int make = Math.min(Math.min(upgradedRate(MAX_TRITIUM_PER_TICK.get()), affordable(com.script.comminutioncraft.common.registry.ModFluids.DEUTERIUM.get(), deuterium.getAmount(), 1)),
                Math.min(tritium.getSpace(), energy.getEnergy() / perMb));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * perMb);
        deuterium.drain(drainFor(com.script.comminutioncraft.common.registry.ModFluids.DEUTERIUM.get(), make, 1));
        tritium.fill(FluidResource.of(ModFluids.TRITIUM.get()), make);
        return make;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.TRITIUM_BREEDER.get();
    }
}
