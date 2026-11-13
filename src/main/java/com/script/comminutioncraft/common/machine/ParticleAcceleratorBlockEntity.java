package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Tier 5 Particle Accelerator, run from its Accelerator Controller. The accelerator is a hollow square ring, one block
 * high, of Accelerator Segments with the controller somewhere in it and any number of Accelerator Ports; its side is
 * {@link #MIN_RING_SIDE}..{@link #MAX_RING_SIDE} blocks. The controller walks the ring every {@link #FORM_CHECK_INTERVAL}
 * ticks (along touching accelerator blocks at its own height) and links the ports to itself. Any extra accelerator block
 * touching the ring, or a second controller, breaks it.
 * <p>
 * Formed, it uses {@link #ENERGY_PER_RING_BLOCK} FE/t per ring block, and each ring block adds 1 per tick towards the
 * next mB of antimatter ({@link #RING_TICKS_PER_MB}; a 7x7 ring makes 1 mB per second), which costs
 * {@link #HYDROGEN_PER_MB} mB of hydrogen. So a bigger ring is faster and hungrier. It waits (keeping its progress)
 * without FE or hydrogen, or while its antimatter tank is full. FE and hydrogen come in through ports' Input faces;
 * antimatter goes out of their Output faces (into a Containment Cell or Magnetic Conduit). While it runs, the ring's
 * beam glows.
 */
public class ParticleAcceleratorBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine, MenuProvider {
    public static final ConfigInt ENERGY_PER_RING_BLOCK = MachineConfig.ACCELERATOR_ENERGY_PER_RING_BLOCK;
    public static final ConfigInt RING_TICKS_PER_MB = MachineConfig.ACCELERATOR_RING_TICKS_PER_MB;
    public static final ConfigInt HYDROGEN_PER_MB = MachineConfig.ACCELERATOR_HYDROGEN_PER_MB;
    public static final ConfigInt MIN_RING_SIDE = MachineConfig.ACCELERATOR_MIN_RING_SIDE;
    public static final ConfigInt MAX_RING_SIDE = MachineConfig.ACCELERATOR_MAX_RING_SIDE;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.ACCELERATOR_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_INPUT = MachineConfig.ACCELERATOR_MAX_ENERGY_INPUT;
    public static final ConfigInt HYDROGEN_CAPACITY = MachineConfig.ACCELERATOR_HYDROGEN_CAPACITY;
    public static final ConfigInt ANTIMATTER_CAPACITY = MachineConfig.ACCELERATOR_ANTIMATTER_CAPACITY;
    public static final int FORM_CHECK_INTERVAL = 20;

    /** What the GUI shows as the state. */
    public enum Status { NOT_FORMED, RUNNING, NO_POWER, NO_HYDROGEN, FULL, OFF }

    // ContainerData indices (energy values split in two, see EnergySync)
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_USE_LOW = 4;
    public static final int DATA_USE_HIGH = 5;
    public static final int DATA_HYDROGEN = 6;
    public static final int DATA_HYDROGEN_CAPACITY = 7;
    public static final int DATA_ANTIMATTER = 8;
    public static final int DATA_ANTIMATTER_CAPACITY = 9;
    public static final int DATA_STATUS = 10;
    public static final int DATA_SIDE = 11;
    public static final int DATA_PROGRESS = 12;
    public static final int DATA_PROGRESS_MAX = 13;
    public static final int DATA_REDSTONE = 14;
    public static final int DATA_SIDES = 15;
    public static final int DATA_COUNT = 16;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get(), 0, this::setChanged);
    private final MachineFluidTank hydrogen = new MachineFluidTank(HYDROGEN_CAPACITY.get(),
            resource -> resource.is(ModFluids.HYDROGEN_TYPE.get()), this::setChanged);
    private final MachineFluidTank antimatter = new MachineFluidTank(ANTIMATTER_CAPACITY.get(), ModFluids::isAntimatter, this::setChanged);
    private final ResourceHandler<FluidResource> tanks = new CombinedResourceHandler<>(hydrogen, antimatter);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::setChanged);
    /** The ring's blocks (controller included) from the last check; empty when not formed. */
    private List<BlockPos> ring = List.of();
    private int ringSide;
    private int progress;
    private int formCheck;
    private boolean ringLit;
    private Status status = Status.NOT_FORMED;
    /** FE used last tick (for the GUI). */
    private int energyUse;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_USE_LOW -> EnergySync.low(energyUse);
                case DATA_USE_HIGH -> EnergySync.high(energyUse);
                case DATA_HYDROGEN -> hydrogen.getAmount();
                case DATA_HYDROGEN_CAPACITY -> hydrogen.getCapacity();
                case DATA_ANTIMATTER -> antimatter.getAmount();
                case DATA_ANTIMATTER_CAPACITY -> antimatter.getCapacity();
                case DATA_STATUS -> status.ordinal();
                case DATA_SIDE -> ringSide;
                case DATA_PROGRESS -> progress;
                case DATA_PROGRESS_MAX -> RING_TICKS_PER_MB.get();
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ParticleAcceleratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PARTICLE_ACCELERATOR.get(), pos, state);
    }

    // ---- What the ports (and tests) see ----

    public boolean isFormed() {
        return !ring.isEmpty();
    }

    public boolean isRunning() {
        return ringLit;
    }

    /** Side of the formed ring in blocks (0 when not formed). */
    public int getRingSide() {
        return ringSide;
    }

    public Status getStatus() {
        return status;
    }

    public MachineEnergyStorage getEnergy() {
        return energy;
    }

    public MachineFluidTank getHydrogen() {
        return hydrogen;
    }

    public MachineFluidTank getAntimatter() {
        return antimatter;
    }

    public ResourceHandler<FluidResource> getTanks() {
        return tanks;
    }

    /** Ring blocks in a ring of this side. */
    public static int ringBlocks(int side) {
        return side < 2 ? 0 : 4 * (side - 1);
    }

    // ---- Ticking ----

    @Override
    public MachineControls getControls() {
        return controls;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (formCheck-- <= 0) {
            formCheck = FORM_CHECK_INTERVAL;
            checkRing(level);
        }
        energyUse = 0;
        status = work(level, pos);
        setRingLit(level, status == Status.RUNNING);
    }

    private Status work(ServerLevel level, BlockPos pos) {
        if (!isFormed()) {
            return Status.NOT_FORMED;
        }
        if (!controls.canRun(level, pos)) {
            return Status.OFF;
        }
        if (antimatter.getSpace() < 1) {
            return Status.FULL;
        }
        if (hydrogen.getAmount() < HYDROGEN_PER_MB.get()) {
            return Status.NO_HYDROGEN;
        }
        int blocks = ring.size();
        int cost = ENERGY_PER_RING_BLOCK.get() * blocks;
        if (!energy.consume(cost)) {
            return Status.NO_POWER;
        }
        energyUse = cost;
        progress += blocks;
        int perMb = RING_TICKS_PER_MB.get();
        while (progress >= perMb && antimatter.getSpace() >= 1 && hydrogen.getAmount() >= HYDROGEN_PER_MB.get()) {
            progress -= perMb;
            hydrogen.drain(HYDROGEN_PER_MB.get());
            antimatter.fill(FluidResource.of(ModFluids.ANTIMATTER.get()), 1);
            com.script.comminutioncraft.common.advancement.MachineEventTrigger.fireNear(level, pos, com.script.comminutioncraft.common.advancement.MachineEventTrigger.ANTIMATTER_MADE, 24);
        }
        progress = Math.min(progress, perMb);
        setChanged();
        return Status.RUNNING;
    }

    /** Walks the ring from the controller; links its ports when it's a complete hollow square of an allowed size. */
    private void checkRing(ServerLevel level) {
        List<BlockPos> found = walkRing(level, worldPosition);
        int side = ringSide(found);
        if (side == 0) {
            ring = List.of();
            ringSide = 0;
            return;
        }
        ring = found;
        ringSide = side;
        for (BlockPos p : found) {
            if (level.getBlockEntity(p) instanceof AcceleratorPortBlockEntity port) {
                port.setController(worldPosition);
            }
        }
    }

    private static boolean isRingBlock(BlockState state) {
        return state.is(ModBlocks.ACCELERATOR_SEGMENT.get()) || state.is(ModBlocks.ACCELERATOR_PORT.get())
                || state.is(ModBlocks.ACCELERATOR_CONTROLLER.get());
    }

    /** Every accelerator block joined to start through its four horizontal neighbours (stops early when too many). */
    private static List<BlockPos> walkRing(Level level, BlockPos start) {
        int limit = ringBlocks(Math.max(3, MAX_RING_SIDE.get())) + 1;
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && found.size() <= limit) {
            BlockPos p = queue.poll();
            found.add(p);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = p.relative(direction);
                if (seen.add(next) && level.isLoaded(next) && isRingBlock(level.getBlockState(next))) {
                    queue.add(next);
                }
            }
        }
        return found;
    }

    /** The side of the square ring these blocks make, or 0 if they don't make one (or it's too small / big). */
    private int ringSide(List<BlockPos> blocks) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : blocks) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }
        int side = maxX - minX + 1;
        if (side != maxZ - minZ + 1 || side < Math.max(3, MIN_RING_SIDE.get()) || side > MAX_RING_SIDE.get()
                || blocks.size() != ringBlocks(side)) {
            return 0;
        }
        // All on the square's edge, and (as the count matches) every edge spot filled; only one controller.
        int controllers = 0;
        for (BlockPos p : blocks) {
            if (p.getX() != minX && p.getX() != maxX && p.getZ() != minZ && p.getZ() != maxZ) {
                return 0;
            }
            if (level != null && level.getBlockState(p).is(ModBlocks.ACCELERATOR_CONTROLLER.get())) {
                controllers++;
            }
        }
        return controllers == 1 ? side : 0;
    }

    /** Lights (or darkens) every block of the ring when it starts or stops running. */
    private void setRingLit(ServerLevel level, boolean lit) {
        if (lit == ringLit) {
            return;
        }
        ringLit = lit;
        BlockState own = getBlockState();
        if (own.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(worldPosition, own.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
        for (BlockPos p : ring) {
            BlockState block = level.getBlockState(p);
            if ((block.is(ModBlocks.ACCELERATOR_SEGMENT.get()) || block.is(ModBlocks.ACCELERATOR_PORT.get()))
                    && block.getValue(MachineBlock.LIT) != lit) {
                level.setBlock(p, block.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
            }
        }
    }

    /** Darkens what's left of the ring when the controller goes. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            for (BlockPos p : ring) {
                BlockState block = server.getBlockState(p);
                if ((block.is(ModBlocks.ACCELERATOR_SEGMENT.get()) || block.is(ModBlocks.ACCELERATOR_PORT.get()))
                        && block.getValue(MachineBlock.LIT)) {
                    server.setBlock(p, block.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
                }
            }
        }
    }

    // ---- Wrench pickup: the item keeps the settings and stored FE (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        energy.saveTo(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        energy.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
        hydrogen.deserialize(input.childOrEmpty("hydrogen"));
        antimatter.deserialize(input.childOrEmpty("antimatter"));
        progress = input.getIntOr("progress", 0);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
        hydrogen.serialize(output.child("hydrogen"));
        antimatter.serialize(output.child("antimatter"));
        output.putInt("progress", progress);
        controls.save(output);
    }

    // ---- GUI ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ParticleAcceleratorMenu(containerId, inventory, this, dataAccess);
    }
}
