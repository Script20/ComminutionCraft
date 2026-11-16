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
 * Tier 3 Plasma Generator: superheats hydrogen into plasma with FE, 1 mB for 1 mB, up to
 * {@link #MAX_PLASMA_PER_TICK} mB/t at {@link #ENERGY_PER_MB} FE per mB. Hydrogen comes in through Input faces
 * (from an Electrolyser); plasma goes out of Output faces (to machines, tanks or an MHD Generator).
 */
public class PlasmaGeneratorBlockEntity extends FluidMachineBlockEntity {
    public static final ConfigInt MAX_PLASMA_PER_TICK = MachineConfig.PLASMA_GENERATOR_PLASMA_PER_TICK;
    public static final ConfigInt ENERGY_PER_MB = MachineConfig.PLASMA_GENERATOR_ENERGY_PER_MB;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.PLASMA_GENERATOR_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_INPUT = MachineConfig.PLASMA_GENERATOR_MAX_ENERGY_INPUT;

    public static final int TANK_HYDROGEN = 0;
    public static final int TANK_PLASMA = 1;
    public static final int TANK_COUNT = 2;

    private final MachineFluidTank hydrogen = addTank("hydrogen", resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), TankRole.INPUT);
    private final MachineFluidTank plasma = addTank("plasma", resource -> resource.is(ModFluids.PLASMA_TYPE.get()), TankRole.OUTPUT);

    public PlasmaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_GENERATOR.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0);
    }

    @Override
    protected Collection<Fluid> consumedFluids() {
        return List.of(ModFluids.HYDROGEN.get());
    }

    @Override
    protected int work(ServerLevel level) {
        return convert(hydrogen, ModFluids.HYDROGEN.get(), 1, plasma, ModFluids.PLASMA.get(),
                MAX_PLASMA_PER_TICK.get(), ENERGY_PER_MB.get());
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.PLASMA_GENERATOR.get();
    }
}
