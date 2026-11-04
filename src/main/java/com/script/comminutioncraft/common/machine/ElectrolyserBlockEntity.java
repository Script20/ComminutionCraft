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
 * mB/t at {@link #ENERGY_PER_HYDROGEN} FE per mB of hydrogen. Water comes in through Input faces; hydrogen and
 * oxygen go out of Output faces into whatever takes them. Oxygen is kept for later tiers: when its tank is full
 * the rest is vented, so a full oxygen tank never stops the hydrogen.
 */
public class ElectrolyserBlockEntity extends FluidMachineBlockEntity {
    public static final int MAX_HYDROGEN_PER_TICK = 10;
    public static final int WATER_PER_HYDROGEN = 2;
    public static final int ENERGY_PER_HYDROGEN = 10;
    public static final int ENERGY_CAPACITY = 20_000;
    public static final int MAX_ENERGY_INPUT = 400;

    public static final int TANK_WATER = 0;
    public static final int TANK_HYDROGEN = 1;
    public static final int TANK_OXYGEN = 2;
    public static final int TANK_COUNT = 3;

    private final MachineFluidTank water = addTank("water", resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), false);
    private final MachineFluidTank hydrogen = addTank("hydrogen", resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), true);
    private final MachineFluidTank oxygen = addTank("oxygen", resource -> resource.is(ModFluids.OXYGEN_TYPE.get()), true);
    /** Oxygen comes in halves of a mB; this is the half left over. */
    private int oxygenHalves;

    public ElectrolyserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYSER.get(), pos, state, ENERGY_CAPACITY, MAX_ENERGY_INPUT, 0);
    }

    @Override
    protected int work(ServerLevel level) {
        int make = Math.min(Math.min(MAX_HYDROGEN_PER_TICK, water.getAmount() / WATER_PER_HYDROGEN),
                Math.min(hydrogen.getSpace(), energy.getEnergy() / ENERGY_PER_HYDROGEN));
        if (make <= 0) {
            return 0;
        }
        energy.consume(make * ENERGY_PER_HYDROGEN);
        water.drain(make * WATER_PER_HYDROGEN);
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
