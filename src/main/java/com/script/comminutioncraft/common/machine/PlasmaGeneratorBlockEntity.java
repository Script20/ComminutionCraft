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
    public static final int MAX_PLASMA_PER_TICK = 10;
    public static final int ENERGY_PER_MB = 16;
    public static final int ENERGY_CAPACITY = 40_000;
    public static final int MAX_ENERGY_INPUT = 400;

    public static final int TANK_HYDROGEN = 0;
    public static final int TANK_PLASMA = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank hydrogen = addTank("hydrogen", resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), false);
    private final MachineFluidTank plasma = addTank("plasma", resource -> resource.is(ModFluids.PLASMA_TYPE.get()), true);

    public PlasmaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_GENERATOR.get(), pos, state, ENERGY_CAPACITY, MAX_ENERGY_INPUT, 0);
    }

    @Override
    protected int work(ServerLevel level) {
        int make = Math.min(Math.min(MAX_PLASMA_PER_TICK, hydrogen.getAmount()),
                Math.min(plasma.getSpace(), energy.getEnergy() / ENERGY_PER_MB));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * ENERGY_PER_MB);
        hydrogen.drain(make);
        plasma.fill(FluidResource.of(ModFluids.PLASMA.get()), make);
        return make;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.PLASMA_GENERATOR.get();
    }
}
