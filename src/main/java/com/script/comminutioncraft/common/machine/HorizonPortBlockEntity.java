package com.script.comminutioncraft.common.machine;

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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import org.jspecify.annotations.Nullable;

/**
 * Where the Event Horizon Reactor meets the world. An Input port takes items (matter for the black hole, into the
 * controller's intake slot) and antimatter (to ignite it) on every face; an Output port gives FE on every face and
 * pushes it into the blocks around it (never into the reactor). Works only while it's part of a formed reactor.
 */
public class HorizonPortBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine {
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::setChanged);
    private @Nullable BlockPos controllerPos;

    public HorizonPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HORIZON_PORT.get(), pos, state);
    }

    void setController(BlockPos pos) {
        if (!pos.equals(controllerPos)) {
            controllerPos = pos;
            refreshFaces();
        }
    }

    public boolean isOutput() {
        return AcceleratorPortBlock.isOutput(getBlockState());
    }

    /** The formed reactor this port belongs to, or null. */
    public @Nullable EventHorizonReactorBlockEntity getReactor() {
        if (controllerPos == null || level == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof EventHorizonReactorBlockEntity reactor && reactor.isFormed() ? reactor : null;
    }

    public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        EventHorizonReactorBlockEntity reactor = getReactor();
        return reactor != null && !isOutput() ? new WorldlyContainerWrapper(reactor, Direction.UP) : null;
    }

    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        EventHorizonReactorBlockEntity reactor = getReactor();
        return reactor != null && !isOutput() ? reactor.getAntimatter().insertOnly() : null;
    }

    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        EventHorizonReactorBlockEntity reactor = getReactor();
        return reactor != null && isOutput() ? reactor.getEnergyOutput() : null;
    }

    @Override
    public MachineControls getControls() {
        return controls;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        EventHorizonReactorBlockEntity reactor = getReactor();
        if (reactor == null || !isOutput()) {
            return;
        }
        int budget = Math.min(EventHorizonReactorBlockEntity.MAX_ENERGY_OUTPUT.get(), reactor.getEnergy().getEnergy());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            BlockPos next = pos.relative(direction);
            if (level.getBlockEntity(next) instanceof HorizonPortBlockEntity || level.getBlockEntity(next) instanceof EventHorizonReactorBlockEntity) {
                continue; // don't feed the reactor its own output
            }
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, next, direction.getOpposite());
            if (target != null) {
                budget -= EnergyHandlerUtil.move(reactor.getEnergyOutput(), target, budget, null);
            }
        }
    }

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
