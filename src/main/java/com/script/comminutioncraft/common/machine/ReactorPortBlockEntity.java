package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
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
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jspecify.annotations.Nullable;

/**
 * Where a Fusion Reactor meets the world. Each face follows the side config like a machine: Input faces take fuel
 * pellets and plasma, Output faces give FE and tritium (and push them into the block there). The port only works while
 * it's part of a formed reactor; the controller links it on every structure check.
 */
public class ReactorPortBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine {
    /** Max mB of tritium pushed per tick, across all output faces. */
    public static final int MAX_TRITIUM_OUTPUT = 500;

    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private @Nullable BlockPos controllerPos;

    public ReactorPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_PORT.get(), pos, state);
    }

    void setController(BlockPos pos) {
        if (!pos.equals(controllerPos)) {
            controllerPos = pos;
            refreshFaces();
        }
    }

    /** The formed reactor this port belongs to, or null. */
    public @Nullable FusionReactorBlockEntity getReactor() {
        if (controllerPos == null || level == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof FusionReactorBlockEntity reactor && reactor.isFormed() ? reactor : null;
    }

    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        FusionReactorBlockEntity reactor = getReactor();
        return reactor != null && sideMode(side) == SideMode.INPUT ? new WorldlyContainerWrapper(reactor, Direction.UP) : null;
    }

    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        FusionReactorBlockEntity reactor = getReactor();
        if (reactor == null) {
            return null;
        }
        return switch (sideMode(side)) {
            case INPUT -> reactor.getPlasma().insertOnly();
            case OUTPUT -> reactor.getTritium().extractOnly();
            case NONE, BYPRODUCT -> null;
        };
    }

    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        FusionReactorBlockEntity reactor = getReactor();
        return reactor != null && sideMode(side) == SideMode.OUTPUT ? reactor.getEnergyOutput() : null;
    }

    // ---- Ticking: push FE and tritium out of Output faces ----

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
        FusionReactorBlockEntity reactor = getReactor();
        if (reactor == null) {
            return;
        }
        int energyBudget = Math.min(FusionReactorBlockEntity.MAX_ENERGY_OUTPUT, reactor.getEnergy().getEnergy());
        int tritiumBudget = Math.min(MAX_TRITIUM_OUTPUT, reactor.getTritium().getAmount());
        for (Direction direction : Direction.values()) {
            if (sideMode(direction) != SideMode.OUTPUT) {
                continue;
            }
            BlockPos next = pos.relative(direction);
            if (level.getBlockEntity(next) instanceof ReactorPortBlockEntity || level.getBlockEntity(next) instanceof FusionReactorBlockEntity) {
                continue; // don't feed the reactor its own output
            }
            if (energyBudget > 0) {
                EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, next, direction.getOpposite());
                if (target != null) {
                    energyBudget -= EnergyHandlerUtil.move(reactor.getEnergyOutput(), target, energyBudget, null);
                }
            }
            if (tritiumBudget > 0) {
                ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, next, direction.getOpposite());
                if (target != null) {
                    tritiumBudget -= ResourceHandlerUtil.move(reactor.getTritium(), target, resource -> true, tritiumBudget, null);
                }
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
