package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
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

/**
 * Tier 5 Annihilation Generator: antimatter meets matter and turns entirely into energy. Each mB of antimatter gives
 * {@link #ENERGY_PER_MB} FE, spread over {@link #BURN_TICKS_PER_MB} ticks (draft: 2,000,000 FE over 20 ticks =
 * 100,000 FE/t, so it burns up to 0.05 mB/t - what a 7x7 Particle Accelerator makes). Antimatter comes in through
 * Input faces (a Magnetic Conduit or Containment Cell); FE goes out of every face, up to {@link #MAX_ENERGY_OUTPUT}
 * FE/t in total. It pauses (mid-burn, nothing lost) while its buffer can't take the next tick's FE.
 */
public class AnnihilationGeneratorBlockEntity extends FluidMachineBlockEntity {
    public static final ConfigInt ENERGY_PER_MB = MachineConfig.ANNIHILATION_GENERATOR_ENERGY_PER_MB;
    public static final ConfigInt BURN_TICKS_PER_MB = MachineConfig.ANNIHILATION_GENERATOR_BURN_TICKS_PER_MB;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.ANNIHILATION_GENERATOR_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_OUTPUT = MachineConfig.ANNIHILATION_GENERATOR_MAX_ENERGY_OUTPUT;

    public static final int TANK_ANTIMATTER = 0;
    public static final int TANK_COUNT = 1;

    private final MachineFluidTank antimatter = addTank("antimatter", ModFluids::isAntimatter, TankRole.INPUT);
    /** Ticks left of the mB being burned. */
    private int burnTicks;

    public AnnihilationGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANNIHILATION_GENERATOR.get(), pos, state, ENERGY_CAPACITY.get(), 0, MAX_ENERGY_OUTPUT.get());
    }

    public MachineFluidTank getAntimatter() {
        return antimatter;
    }

    /** FE made per tick while burning. */
    public static int energyPerTick() {
        return ENERGY_PER_MB.get() / Math.max(1, BURN_TICKS_PER_MB.get());
    }

    @Override
    protected int work(ServerLevel level) {
        int perTick = energyPerTick();
        if (energy.getSpace() < perTick) {
            return 0;
        }
        if (burnTicks <= 0) {
            if (antimatter.getAmount() < 1) {
                return 0;
            }
            antimatter.drain(1);
            burnTicks = BURN_TICKS_PER_MB.get();
        }
        burnTicks--;
        setChanged();
        return energy.generate(perTick);
    }

    @Override
    protected boolean pushesEnergy() {
        return true;
    }

    @Override
    protected MenuType<FluidMachineMenu> getMenuType() {
        return ModMenus.ANNIHILATION_GENERATOR.get();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTicks = input.getIntOr("burn_ticks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("burn_ticks", burnTicks);
    }
}
