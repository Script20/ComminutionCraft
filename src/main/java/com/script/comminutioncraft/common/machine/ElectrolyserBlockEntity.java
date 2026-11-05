package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Tier 3 Electrolyser: splits water with FE. Every {@link #WATER_PER_HYDROGEN} mB of water gives 1 mB of hydrogen
 * and half a mB of oxygen (hydrogen to oxygen 2:1, like real electrolysis), up to {@link #MAX_HYDROGEN_PER_TICK}
 * mB/t at {@link #ENERGY_PER_HYDROGEN} FE per mB of hydrogen. Water comes in through Input faces; hydrogen goes out
 * of Output faces and oxygen out of By-product faces (so the two never share a pipe or a tank). Oxygen is kept for
 * later tiers: when its tank is full the rest is vented, so a full oxygen tank never stops the hydrogen.
 */
public class ElectrolyserBlockEntity extends FluidMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_HYDROGEN_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.ELECTROLYSER_HYDROGEN_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt WATER_PER_HYDROGEN = com.script.comminutioncraft.common.config.MachineConfig.ELECTROLYSER_WATER_PER_HYDROGEN;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_HYDROGEN = com.script.comminutioncraft.common.config.MachineConfig.ELECTROLYSER_ENERGY_PER_HYDROGEN;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ELECTROLYSER_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.ELECTROLYSER_MAX_ENERGY_INPUT;

    public static final int TANK_WATER = 0;
    public static final int TANK_HYDROGEN = 1;
    public static final int TANK_OXYGEN = 2;
    public static final int TANK_COUNT = 3;

    private final MachineFluidTank water = addTank("water", resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), TankRole.INPUT);
    private final MachineFluidTank hydrogen = addTank("hydrogen", resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), TankRole.OUTPUT);
    private final MachineFluidTank oxygen = addTank("oxygen", resource -> resource.is(ModFluids.OXYGEN_TYPE.get()), TankRole.BYPRODUCT);
    /** Oxygen comes in halves of a mB; this is the half left over. */
    private int oxygenHalves;

    public ElectrolyserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYSER.get(), pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0, true);
    }

    @Override
    protected int work(ServerLevel level) {
        int make = Math.min(Math.min(MAX_HYDROGEN_PER_TICK.get(), water.getAmount() / WATER_PER_HYDROGEN.get()),
                Math.min(hydrogen.getSpace(), energy.getEnergy() / ENERGY_PER_HYDROGEN.get()));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * ENERGY_PER_HYDROGEN.get());
        water.drain(make * WATER_PER_HYDROGEN.get());
        hydrogen.fill(FluidResource.of(ModFluids.HYDROGEN.get()), make);
        oxygenHalves += make;
        int madeOxygen = oxygenHalves / 2;
        oxygenHalves %= 2;
        int kept = Math.min(madeOxygen, oxygen.getSpace());
        if (kept > 0) {
            oxygen.fill(FluidResource.of(ModFluids.OXYGEN.get()), kept);
        }
        return make;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.ELECTROLYSER.get();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        oxygenHalves = input.getIntOr("oxygen_halves", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("oxygen_halves", oxygenHalves);
    }
}
