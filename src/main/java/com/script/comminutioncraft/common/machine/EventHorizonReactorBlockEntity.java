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
import com.script.comminutioncraft.common.registry.ModDamageTypes;
import com.script.comminutioncraft.common.registry.ModDataMaps;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 6 Event Horizon Reactor, run from its controller. The reactor is a 7x7x7 cage: Containment Frame (or Horizon
 * Ports) on all 12 edges, the controller in the middle of one bottom edge facing out, the inner 5x5x5 empty (air);
 * the faces' openings may hold anything (glass keeps people out). The controller re-checks it every
 * {@link #FORM_CHECK_INTERVAL} ticks and links the ports.
 * <p>
 * Formed, {@link #IGNITION_ANTIMATTER} mB of antimatter ignites a black hole in the middle. While it lives, the inner
 * 5x5x5 is a field: items in it are eaten for mass, anything alive in it dies (even in creative), anything else is
 * swallowed. Items also come in through Input ports / the GUI slot. Each mass unit (an item's
 * {@code comminutioncraft:matter_mass}, 1 by default) is worth {@link #ENERGY_PER_MASS} FE; it burns up to
 * {@link #MASS_PER_TICK} a tick (pausing when the FE buffer is full, or when redstone says so). With no mass left it
 * starves: after {@link #STARVE_TICKS} ticks it evaporates and needs igniting again. FE goes out of Output ports.
 */
public class EventHorizonReactorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, TickingMachine, ControllableMachine {
    public static final int SLOT_INTAKE = 0;
    private static final int[] SLOTS = {SLOT_INTAKE};

    public static final ConfigInt IGNITION_ANTIMATTER = MachineConfig.EVENT_HORIZON_IGNITION_ANTIMATTER;
    public static final ConfigInt ENERGY_PER_MASS = MachineConfig.EVENT_HORIZON_ENERGY_PER_MASS;
    public static final ConfigInt MASS_PER_TICK = MachineConfig.EVENT_HORIZON_MASS_PER_TICK;
    public static final ConfigInt MASS_CAPACITY = MachineConfig.EVENT_HORIZON_MASS_CAPACITY;
    public static final ConfigInt STARVE_TICKS = MachineConfig.EVENT_HORIZON_STARVE_TICKS;
    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.EVENT_HORIZON_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_OUTPUT = MachineConfig.EVENT_HORIZON_MAX_ENERGY_OUTPUT;
    public static final ConfigInt ANTIMATTER_CAPACITY = MachineConfig.EVENT_HORIZON_ANTIMATTER_CAPACITY;
    public static final int FORM_CHECK_INTERVAL = 20;
    /** Half the cage's side: the cage spans centre -3 .. +3; the field the inner 5x5x5 (centre -2 .. +2). */
    public static final int HALF = 3;

    /** What the GUI shows as the state. */
    public enum Status { NOT_FORMED, NEEDS_ANTIMATTER, RUNNING, HUNGRY, FULL, OFF }

    // ContainerData indices (big values split in two, see EnergySync)
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_RATE_LOW = 4;
    public static final int DATA_RATE_HIGH = 5;
    public static final int DATA_MASS_LOW = 6;
    public static final int DATA_MASS_HIGH = 7;
    public static final int DATA_MASS_CAP_LOW = 8;
    public static final int DATA_MASS_CAP_HIGH = 9;
    public static final int DATA_ANTIMATTER = 10;
    public static final int DATA_ANTIMATTER_CAPACITY = 11;
    public static final int DATA_STATUS = 12;
    public static final int DATA_STARVE_SECONDS = 13;
    public static final int DATA_REDSTONE = 14;
    public static final int DATA_SIDES = 15;
    public static final int DATA_COUNT = 16;

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY.get(), 0, MAX_ENERGY_OUTPUT.get(), this::setChanged);
    private final EnergyHandler energyOut = new LimitingEnergyHandler(energy, 0, MAX_ENERGY_OUTPUT.get());
    private final MachineFluidTank antimatter = new MachineFluidTank(ANTIMATTER_CAPACITY.get(), ModFluids::isAntimatter, this::setChanged);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::setChanged);
    private boolean formed;
    private boolean ignited;
    private int mass;
    private int starve;
    /** FE made last tick (for the GUI). */
    private int rate;
    private int formCheck;
    private boolean cageLit;
    private Status status = Status.NOT_FORMED;
    /** The cage's frame and port positions from the last check (for lighting). */
    private List<BlockPos> cage = List.of();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_RATE_LOW -> EnergySync.low(rate);
                case DATA_RATE_HIGH -> EnergySync.high(rate);
                case DATA_MASS_LOW -> EnergySync.low(mass);
                case DATA_MASS_HIGH -> EnergySync.high(mass);
                case DATA_MASS_CAP_LOW -> EnergySync.low(MASS_CAPACITY.get());
                case DATA_MASS_CAP_HIGH -> EnergySync.high(MASS_CAPACITY.get());
                case DATA_ANTIMATTER -> antimatter.getAmount();
                case DATA_ANTIMATTER_CAPACITY -> antimatter.getCapacity();
                case DATA_STATUS -> status.ordinal();
                case DATA_STARVE_SECONDS -> Math.max(0, (STARVE_TICKS.get() - starve) / 20);
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

    public EventHorizonReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EVENT_HORIZON_REACTOR.get(), pos, state);
    }

    // ---- What the ports, the renderer and tests see ----

    public boolean isFormed() {
        return formed;
    }

    /** Whether the black hole exists (and the field is deadly). */
    public boolean isIgnited() {
        return ignited;
    }

    public int getMass() {
        return mass;
    }

    public Status getStatus() {
        return status;
    }

    public MachineEnergyStorage getEnergy() {
        return energy;
    }

    public EnergyHandler getEnergyOutput() {
        return energyOut;
    }

    public MachineFluidTank getAntimatter() {
        return antimatter;
    }

    public ResourceHandler<FluidResource> getTanks() {
        return new CombinedResourceHandler<>(antimatter);
    }

    /** The middle of the cage: up 3 and 3 in from the controller (which faces out of the cage). */
    public BlockPos getCentre() {
        return worldPosition.above(HALF).relative(getFacing().getOpposite(), HALF);
    }

    /** The field: the inner 5x5x5. */
    public AABB getField() {
        BlockPos c = getCentre();
        return new AABB(c.getX() - 2, c.getY() - 2, c.getZ() - 2, c.getX() + 3, c.getY() + 3, c.getZ() + 3);
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
            formed = checkStructure(level);
        }
        rate = 0;
        if (!formed) {
            ignited = false; // the cage broke: the black hole slips away
            status = Status.NOT_FORMED;
            setCageLit(level, false);
            return;
        }
        if (!ignited) {
            if (antimatter.getAmount() >= IGNITION_ANTIMATTER.get()) {
                antimatter.drain(IGNITION_ANTIMATTER.get());
                ignited = true;
                starve = 0;
                setChanged();
            } else {
                status = Status.NEEDS_ANTIMATTER;
                setCageLit(level, false);
                return;
            }
        }
        feedFromIntake();
        swallow(level);
        status = burn(level, pos);
        if (mass > 0) {
            starve = 0;
        } else if (++starve >= STARVE_TICKS.get()) {
            ignited = false; // starved: it evaporates
            starve = 0;
            status = Status.NEEDS_ANTIMATTER;
        }
        setCageLit(level, ignited);
    }

    private Status burn(ServerLevel level, BlockPos pos) {
        if (!controls.canRun(level, pos)) {
            return Status.OFF;
        }
        if (mass <= 0) {
            return Status.HUNGRY;
        }
        int perMass = ENERGY_PER_MASS.get();
        int burn = Math.min(Math.min(MASS_PER_TICK.get(), mass), energy.getSpace() / Math.max(1, perMass));
        if (burn <= 0) {
            return Status.FULL;
        }
        mass -= burn;
        rate = energy.generate(burn * perMass);
        setChanged();
        return Status.RUNNING;
    }

    /** Moves what's in the intake slot into the black hole, as far as its mass capacity allows. */
    private void feedFromIntake() {
        ItemStack stack = items.get(SLOT_INTAKE);
        if (stack.isEmpty()) {
            return;
        }
        int each = Math.max(1, ModDataMaps.massOf(stack));
        int take = Math.min(stack.getCount(), (MASS_CAPACITY.get() - mass) / each);
        if (take > 0) {
            stack.shrink(take);
            mass += take * each;
            setChanged();
        }
    }

    /** The field: eats items, kills anything alive (creative players included), swallows anything else. */
    private void swallow(ServerLevel level) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, getField(), Entity::isAlive)) {
            if (entity instanceof ItemEntity item) {
                ItemStack stack = item.getItem();
                int each = Math.max(1, ModDataMaps.massOf(stack));
                int take = Math.min(stack.getCount(), Math.max(0, MASS_CAPACITY.get() - mass) / each);
                mass += take * each;
                stack.shrink(take);
                if (stack.isEmpty() || take == 0) {
                    item.discard(); // a full black hole still swallows it, it just gets nothing from it
                }
                setChanged();
            } else if (entity instanceof LivingEntity living) {
                living.hurtServer(level, ModDamageTypes.eventHorizon(level), Float.MAX_VALUE);
                if (living.isAlive() && !(living instanceof net.minecraft.world.entity.player.Player)) {
                    living.kill(level);
                }
            } else {
                entity.discard();
                mass = Math.min(MASS_CAPACITY.get(), mass + 1);
            }
        }
    }

    /** Whether the 7x7x7 is complete; links its ports to this controller when it is. */
    private boolean checkStructure(ServerLevel level) {
        BlockPos c = getCentre();
        List<BlockPos> found = new ArrayList<>();
        List<HorizonPortBlockEntity> ports = new ArrayList<>();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dy = -HALF; dy <= HALF; dy++) {
                for (int dz = -HALF; dz <= HALF; dz++) {
                    int outer = (Math.abs(dx) == HALF ? 1 : 0) + (Math.abs(dy) == HALF ? 1 : 0) + (Math.abs(dz) == HALF ? 1 : 0);
                    BlockPos p = c.offset(dx, dy, dz);
                    if (outer == 0) {
                        if (!level.getBlockState(p).isAir()) {
                            return false; // the inside must be empty
                        }
                    } else if (outer >= 2) {
                        if (p.equals(worldPosition)) {
                            continue;
                        }
                        BlockState block = level.getBlockState(p);
                        if (block.is(ModBlocks.CONTAINMENT_FRAME.get())) {
                            found.add(p);
                        } else if (block.is(ModBlocks.HORIZON_PORT.get()) && level.getBlockEntity(p) instanceof HorizonPortBlockEntity port) {
                            found.add(p);
                            ports.add(port);
                        } else {
                            return false;
                        }
                    }
                    // outer == 1: the faces' openings, anything goes
                }
            }
        }
        cage = found;
        ports.forEach(port -> port.setController(worldPosition));
        return true;
    }

    /** Lights (or darkens) the controller, frame and ports when the black hole appears or goes. */
    private void setCageLit(ServerLevel level, boolean lit) {
        if (lit == cageLit) {
            return;
        }
        cageLit = lit;
        BlockState own = getBlockState();
        if (own.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(worldPosition, own.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
        for (BlockPos p : cage) {
            BlockState block = level.getBlockState(p);
            if ((block.is(ModBlocks.CONTAINMENT_FRAME.get()) || block.is(ModBlocks.HORIZON_PORT.get()))
                    && block.getValue(MachineBlock.LIT) != lit) {
                level.setBlock(p, block.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            for (BlockPos p : cage) {
                BlockState block = server.getBlockState(p);
                if ((block.is(ModBlocks.CONTAINMENT_FRAME.get()) || block.is(ModBlocks.HORIZON_PORT.get())) && block.getValue(MachineBlock.LIT)) {
                    server.setBlock(p, block.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
                }
            }
        }
    }

    // ---- Wrench pickup: the item keeps the settings and stored FE ----

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
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        energy.deserialize(input);
        antimatter.deserialize(input.childOrEmpty("antimatter"));
        ignited = input.getBooleanOr("ignited", false);
        mass = input.getIntOr("mass", 0);
        starve = input.getIntOr("starve", 0);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        antimatter.serialize(output.child("antimatter"));
        output.putBoolean("ignited", ignited);
        output.putInt("mass", mass);
        output.putInt("starve", starve);
        controls.save(output);
    }

    // ---- Container: one intake slot that takes anything ----

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.horizon_controller");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new EventHorizonMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
