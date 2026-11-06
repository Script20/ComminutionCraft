package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Where a Particle Accelerator meets the world. The whole port is Input or Output ({@link AcceleratorPortBlock#OUTPUT},
 * switched with the wrench): an Input port takes FE and hydrogen on every face; an Output port gives antimatter on
 * every face and pushes it into the blocks around it (a Containment Cell, a Magnetic Conduit), never into the ring. The
 * port only works while it's part of a formed ring; the controller links it on every ring check.
 */
public class AcceleratorPortBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine {
    /** Max mB of antimatter pushed per tick, across all output faces. */
    public static final ConfigInt MAX_ANTIMATTER_OUTPUT = MachineConfig.ACCELERATOR_PORT_ANTIMATTER_OUTPUT;

    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private @Nullable BlockPos controllerPos;

    public AcceleratorPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ACCELERATOR_PORT.get(), pos, state);
    }

    void setController(BlockPos pos) {
        if (!pos.equals(controllerPos)) {
            controllerPos = pos;
            refreshFaces();
        }
    }

    /** The formed accelerator this port belongs to, or null. */
    public @Nullable ParticleAcceleratorBlockEntity getAccelerator() {
        if (controllerPos == null || level == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof ParticleAcceleratorBlockEntity accelerator && accelerator.isFormed()
                ? accelerator : null;
    }

    public boolean isOutput() {
        return AcceleratorPortBlock.isOutput(getBlockState());
    }

    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        ParticleAcceleratorBlockEntity accelerator = getAccelerator();
        if (accelerator == null) {
            return null;
        }
        return isOutput() ? accelerator.getAntimatter().extractOnly() : accelerator.getHydrogen().insertOnly();
    }

    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        ParticleAcceleratorBlockEntity accelerator = getAccelerator();
        return accelerator != null && !isOutput() ? accelerator.getEnergy() : null;
    }

    // ---- Ticking: an Output port pushes antimatter out of every face ----

    @Override
    public MachineControls getControls() {
        return controls;
    }

    private void onControlsChanged() {
        setChanged();
        refreshFaces();
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ParticleAcceleratorBlockEntity accelerator = getAccelerator();
        if (accelerator == null || !isOutput()) {
            return;
        }
        int budget = Math.min(MAX_ANTIMATTER_OUTPUT.get(), accelerator.getAntimatter().getAmount());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            BlockPos next = pos.relative(direction);
            if (level.getBlockEntity(next) instanceof AcceleratorPortBlockEntity || level.getBlockEntity(next) instanceof ParticleAcceleratorBlockEntity) {
                continue; // don't feed the accelerator its own output
            }
            ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, next, direction.getOpposite());
            if (target != null) {
                budget -= ResourceHandlerUtil.move(accelerator.getAntimatter(), target, resource -> true, budget, null);
            }
        }
    }

    // ---- Wrench pickup keeps the settings ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        controls.loadFrom(components);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        controls.save(output);
    }
}
