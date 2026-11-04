package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier 3 MHD (magnetohydrodynamic) Generator: plasma rushing through a magnetic field makes FE directly,
 * {@link #ENERGY_PER_MB} FE per mB, up to {@link #MAX_PLASMA_PER_TICK} mB/t (600 FE/t). Plasma comes in through Input
 * faces; FE goes out of every face, up to {@link #MAX_ENERGY_OUTPUT} FE/t in total, like the Electric Generator. It
 * pauses while its buffer is full.
 * <p>
 * The whole chain pays: a mB of plasma costs 2 mB water + 10 FE (Electrolyser) + 16 FE (Plasma Generator) and gives 60 FE.
 */
public class MhdGeneratorBlockEntity extends FluidMachineBlockEntity {
    public static final int MAX_PLASMA_PER_TICK = 10;
    public static final int ENERGY_PER_MB = 60;
    public static final int ENERGY_CAPACITY = 50_000;
    public static final int MAX_ENERGY_OUTPUT = 1_000;

    public static final int TANK_PLASMA = 0;
    public static final int TANK_COUNT = 1;

    private final MachineFluidTank plasma = addTank("plasma", resource -> resource.is(ModFluids.PLASMA_TYPE.get()), TankRole.INPUT);

    public MhdGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MHD_GENERATOR.get(), pos, state, ENERGY_CAPACITY, 0, MAX_ENERGY_OUTPUT);
    }

    @Override
    protected int work(ServerLevel level) {
        int burn = Math.min(Math.min(MAX_PLASMA_PER_TICK, plasma.getAmount()), energy.getSpace() / ENERGY_PER_MB);
        if (burn <= 0) {
            return 0;
        }
        plasma.drain(burn);
        return energy.generate(burn * ENERGY_PER_MB);
    }

    @Override
    protected boolean pushesEnergy() {
        return true;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.MHD_GENERATOR.get();
    }
}
