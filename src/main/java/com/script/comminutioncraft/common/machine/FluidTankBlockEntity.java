package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Fluid Tank: holds 32,000 mB of any one fluid. Faces follow the side config (no GUI: set them with the wrench):
 * Input faces take fluid in, Output faces push up to {@link #OUTPUT_RATE} mB per tick into whatever is there
 * (pipes, machines), None faces don't connect. Buckets work on any face. The contents are synced to clients
 * for {@code FluidTankRenderer}.
 */
public class FluidTankBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine {
    public static final com.script.comminutioncraft.common.config.ConfigInt CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.FLUID_TANK_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt OUTPUT_RATE = com.script.comminutioncraft.common.config.MachineConfig.FLUID_TANK_OUTPUT_RATE;
    /** At most one client sync every this many ticks while the contents keep changing. */
    private static final int SYNC_INTERVAL = 5;

    private final MachineFluidTank tank;
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private boolean needsSync;
    private int lastComparatorSignal;

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_TANK.get(), pos, state);
    }

    /** For the Creative Fluid Tank: any fluid but antimatter, {@link #CAPACITY} mB. */
    protected FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, CAPACITY.get(), resource -> !com.script.comminutioncraft.common.registry.ModFluids.isAntimatter(resource));
    }

    /** For the Containment Cell: its own size and the fluids it takes. */
    protected FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity,
                                   java.util.function.Predicate<FluidResource> accepts) {
        super(type, pos, state);
        tank = new MachineFluidTank(capacity, accepts, this::onTankChanged);
    }

    /** mB per tick pushed out of Output faces. */
    protected int outputRate() {
        return OUTPUT_RATE.get();
    }

    public MachineFluidTank getTank() {
        return tank;
    }

    /** The fluid capability on a face: insert-only on input faces, extract-only on output faces, none on None faces. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return switch (sideMode(side)) {
            case INPUT -> tank.insertOnly();
            case OUTPUT -> tank.extractOnly();
            case NONE, BYPRODUCT -> null;
        };
    }

    public int getComparatorSignal() {
        int amount = tank.getAmount();
        return amount <= 0 ? 0 : 1 + (int) ((long) (amount - 1) * 14 / tank.getCapacity());
    }

    private void onTankChanged() {
        setChanged();
        needsSync = true;
    }

    // ---- Ticking ----

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
        if (controls.canRun(level, pos)) {
            pushFluid(level, pos);
        }
        if (needsSync && level.getGameTime() % SYNC_INTERVAL == 0) {
            needsSync = false;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        int signal = getComparatorSignal();
        if (signal != lastComparatorSignal) {
            lastComparatorSignal = signal;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    private void pushFluid(ServerLevel level, BlockPos pos) {
        int budget = Math.min(outputRate(), tank.getAmount());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            if (getFluidHandler(direction) != tank.extractOnly()) {
                continue;
            }
            ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                budget -= ResourceHandlerUtil.move(tank, target, resource -> true, budget, null);
            }
        }
    }

    // ---- Client sync (for the renderer) ----

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // ---- Wrench pickup: the item keeps the fluid and settings (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        tank.saveTo(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        tank.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input.childOrEmpty("fluid"));
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child("fluid"));
        controls.save(output);
    }
}
