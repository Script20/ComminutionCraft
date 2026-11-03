package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Shared logic for tier-3 (plasma) processing machines: the electric machine plus a plasma tank. Each operation needs
 * {@link #getPlasmaPerOperation()} mB of plasma on top of its FE; without either it pauses (progress kept). Plasma
 * comes in through Input faces (pipes, tanks, a generator's output face), like items do.
 */
public abstract class PlasmaMachineBlockEntity extends ElectricMachineBlockEntity {
    public static final int ENERGY_CAPACITY = 50_000;
    public static final int MAX_ENERGY_INPUT = 1_000;
    public static final int PLASMA_CAPACITY = 8_000;

    // ContainerData indices after the electric machine's (plasma fits in one 16-bit slot)
    public static final int DATA_PLASMA = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_PLASMA_CAPACITY = DATA_PLASMA + 1;
    public static final int DATA_COUNT = DATA_PLASMA_CAPACITY + 1;

    private final MachineFluidTank tank = new MachineFluidTank(PLASMA_CAPACITY,
            resource -> resource.is(ModFluids.PLASMA_TYPE.get()), this::setChanged);

    protected PlasmaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, ENERGY_CAPACITY, MAX_ENERGY_INPUT);
    }

    /** mB of plasma one operation uses (taken when it finishes). */
    protected abstract int getPlasmaPerOperation();

    public MachineFluidTank getTank() {
        return tank;
    }

    /** The fluid capability on a face: plasma goes in through Input faces; nothing comes out. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return sideMode(side) == SideMode.INPUT ? tank.insertOnly() : null;
    }

    @Override
    protected boolean hasOperationResources() {
        return tank.getAmount() >= getPlasmaPerOperation();
    }

    @Override
    protected void onOperationDone() {
        tank.drain(getPlasmaPerOperation());
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_PLASMA -> tank.getAmount();
            case DATA_PLASMA_CAPACITY -> tank.getCapacity();
            default -> super.getData(index);
        };
    }

    @Override
    protected int getDataCount() {
        return DATA_COUNT;
    }

    // ---- Wrench pickup keeps the plasma too ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        tank.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        tank.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("plasma"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("plasma"));
    }
}
