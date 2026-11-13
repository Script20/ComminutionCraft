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
 * Tier 3 Plasma Generator: superheats hydrogen into plasma with FE, 1 mB for 1 mB, up to
 * {@link #MAX_PLASMA_PER_TICK} mB/t at {@link #ENERGY_PER_MB} FE per mB. Hydrogen comes in through Input faces
 * (from an Electrolyser); plasma goes out of Output faces (to machines, tanks or an MHD Generator).
 */
public class PlasmaGeneratorBlockEntity extends FluidMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_PLASMA_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_GENERATOR_PLASMA_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_MB = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_GENERATOR_ENERGY_PER_MB;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_GENERATOR_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_GENERATOR_MAX_ENERGY_INPUT;

    public static final int TANK_HYDROGEN = 0;
    public static final int TANK_PLASMA = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank hydrogen = addTank("hydrogen", resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), TankRole.INPUT);
    private final MachineFluidTank plasma = addTank("plasma", resource -> resource.is(ModFluids.PLASMA_TYPE.get()), TankRole.OUTPUT);

    public PlasmaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_GENERATOR.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected int work(ServerLevel level) {
        int perMb = upgradedEnergy(ENERGY_PER_MB.get());
        int make = Math.min(Math.min(upgradedRate(MAX_PLASMA_PER_TICK.get()), affordable(com.script.comminutioncraft.common.registry.ModFluids.HYDROGEN.get(), hydrogen.getAmount(), 1)),
                Math.min(plasma.getSpace(), energy.getEnergy() / perMb));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * perMb);
        hydrogen.drain(drainFor(com.script.comminutioncraft.common.registry.ModFluids.HYDROGEN.get(), make, 1));
        plasma.fill(FluidResource.of(ModFluids.PLASMA.get()), make);
        return make;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.PLASMA_GENERATOR.get();
    }
}
