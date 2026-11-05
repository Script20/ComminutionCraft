package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 4 Fusion Reactor, run from its Reactor Controller. The reactor is a 3x3x3 shell with the controller in the
 * middle of one face and an empty core behind it: Reactor Casing on the edges and corners (or Reactor Ports), and
 * casing, Reactor Glass or ports in the other face centres. The controller re-checks the shell every
 * {@link #FORM_CHECK_INTERVAL} ticks and links the ports to itself.
 * <p>
 * Formed, it first heats up on plasma ({@link #IGNITION_PLASMA_PER_TICK} mB/t until the core reaches 100%, so
 * ~1,000 mB from cold), then burns Fusion Fuel Pellets: each lasts {@link #PELLET_BURN_TICKS} ticks at
 * {@link #ENERGY_PER_TICK} FE/t and gives {@link #TRITIUM_PER_PELLET} mB of tritium back. Without fuel the core cools
 * and needs plasma again. It pauses (staying hot) while its FE buffer is full. Pellets and plasma come in through ports'
 * Input faces (or the GUI slot); FE and tritium go out of ports' Output faces. While it burns, the shell's seams glow.
 */
public class FusionReactorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, TickingMachine, ControllableMachine {
    public static final int SLOT_PELLETS = 0;
    private static final int[] SLOTS = {SLOT_PELLETS};

    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_OUTPUT = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_MAX_ENERGY_OUTPUT;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_ENERGY_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt PELLET_BURN_TICKS = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_PELLET_BURN_TICKS;
    public static final com.script.comminutioncraft.common.config.ConfigInt TRITIUM_PER_PELLET = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_TRITIUM_PER_PELLET;
    public static final com.script.comminutioncraft.common.config.ConfigInt IGNITION_PLASMA_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_IGNITION_PLASMA_PER_TICK;
    public static final int MAX_HEAT = 100;
    public static final com.script.comminutioncraft.common.config.ConfigInt TANK_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.FUSION_REACTOR_TANK_CAPACITY;
    public static final int FORM_CHECK_INTERVAL = 20;

    // ContainerData indices (energy split in two, see EnergySync)
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_HEAT = 4;
    public static final int DATA_BURN = 5;
    public static final int DATA_PLASMA = 6;
    public static final int DATA_PLASMA_CAPACITY = 7;
    public static final int DATA_TRITIUM = 8;
    public static final int DATA_TRITIUM_CAPACITY = 9;
    public static final int DATA_FORMED = 10;
    public static final int DATA_RATE = 11;
    public static final int DATA_REDSTONE = 12;
    public static final int DATA_SIDES = 13;
    public static final int DATA_COUNT = 14;

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY.get(), 0, MAX_ENERGY_OUTPUT.get(), this::setChanged);
    private final EnergyHandler energyOut = new LimitingEnergyHandler(energy, 0, MAX_ENERGY_OUTPUT.get());
    private final MachineFluidTank plasma = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(ModFluids.PLASMA_TYPE.get()), this::setChanged);
    private final MachineFluidTank tritium = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(ModFluids.TRITIUM_TYPE.get()), this::setChanged);
    private final ResourceHandler<FluidResource> tanks = new CombinedResourceHandler<>(plasma, tritium);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private boolean formed;
    private int heat;
    private int burnTicks;
    /** FE made last tick (for the GUI). */
    private int rate;
    private int formCheck;
    private boolean shellLit;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_HEAT -> heat;
                case DATA_BURN -> burnTicks;
                case DATA_PLASMA -> plasma.getAmount();
                case DATA_PLASMA_CAPACITY -> plasma.getCapacity();
                case DATA_TRITIUM -> tritium.getAmount();
                case DATA_TRITIUM_CAPACITY -> tritium.getCapacity();
                case DATA_FORMED -> formed ? 1 : 0;
                case DATA_RATE -> rate;
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

    public FusionReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR.get(), pos, state);
    }

    // ---- What the ports (and Jade) see ----

    public boolean isFormed() {
        return formed;
    }

    public boolean isBurning() {
        return shellLit;
    }

    public int getHeat() {
        return heat;
    }

    public MachineEnergyStorage getEnergy() {
        return energy;
    }

    /** Extract-only view of the FE buffer (ports' Output faces). */
    public EnergyHandler getEnergyOutput() {
        return energyOut;
    }

    public MachineFluidTank getPlasma() {
        return plasma;
    }

    public MachineFluidTank getTritium() {
        return tritium;
    }

    public ResourceHandler<FluidResource> getTanks() {
        return tanks;
    }

    /** The empty block in the middle of the reactor (behind the controller). */
    public BlockPos getCorePos() {
        return worldPosition.relative(getFacing().getOpposite());
    }

    // ---- Ticking ----

    @Override
    public MachineControls getControls() {
        return controls;
    }

    private void onControlsChanged() {
        setChanged();
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (formCheck-- <= 0) {
            formCheck = FORM_CHECK_INTERVAL;
            formed = checkStructure(level);
        }
        rate = 0;
        if (!formed) {
            heat = 0;
            burnTicks = 0;
            setShellLit(level, false);
            return;
        }
        if (!controls.canRun(level, pos)) {
            setShellLit(level, false);
            return;
        }

        if (heat < MAX_HEAT && burnTicks == 0) {
            // Heating up on plasma (only with fuel waiting, so an idle reactor doesn't burn plasma); otherwise it cools.
            if (items.get(SLOT_PELLETS).is(ModItems.FUSION_FUEL_PELLET.get()) && plasma.getAmount() >= IGNITION_PLASMA_PER_TICK.get()) {
                plasma.drain(IGNITION_PLASMA_PER_TICK.get());
                heat++;
            } else if (heat > 0) {
                heat--;
            }
        } else if (energy.getSpace() >= ENERGY_PER_TICK.get()) {
            if (burnTicks == 0) {
                ItemStack pellets = items.get(SLOT_PELLETS);
                if (pellets.is(ModItems.FUSION_FUEL_PELLET.get())) {
                    pellets.shrink(1);
                    burnTicks = PELLET_BURN_TICKS.get();
                    setChanged();
                } else {
                    heat--; // out of fuel: it cools off and will need plasma again
                }
            }
            if (burnTicks > 0) {
                rate = energy.generate(ENERGY_PER_TICK.get());
                if (--burnTicks == 0) {
                    tritium.fill(FluidResource.of(ModFluids.TRITIUM.get()), TRITIUM_PER_PELLET.get());
                }
            }
        }
        // (A full FE buffer pauses the burn, and the core stays hot.)
        setShellLit(level, rate > 0 || (burnTicks > 0 && heat >= MAX_HEAT));
    }

    /** Positions of the 26 shell blocks around the core. */
    private List<BlockPos> shell() {
        BlockPos core = getCorePos();
        List<BlockPos> shell = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        shell.add(core.offset(dx, dy, dz));
                    }
                }
            }
        }
        return shell;
    }

    /** Whether the 3x3x3 is complete; links its ports to this controller when it is. */
    private boolean checkStructure(ServerLevel level) {
        BlockPos core = getCorePos();
        if (!level.getBlockState(core).isAir()) {
            return false;
        }
        List<ReactorPortBlockEntity> ports = new ArrayList<>();
        for (BlockPos p : shell()) {
            if (p.equals(worldPosition)) {
                continue;
            }
            BlockState block = level.getBlockState(p);
            int centredAxes = (p.getX() == core.getX() ? 1 : 0) + (p.getY() == core.getY() ? 1 : 0) + (p.getZ() == core.getZ() ? 1 : 0);
            boolean faceCentre = centredAxes == 2;
            if (block.is(ModBlocks.REACTOR_CASING.get())) {
                continue;
            }
            if (block.is(ModBlocks.REACTOR_PORT.get()) && level.getBlockEntity(p) instanceof ReactorPortBlockEntity port) {
                ports.add(port);
                continue;
            }
            if (faceCentre && block.is(ModBlocks.REACTOR_GLASS.get())) {
                continue;
            }
            return false;
        }
        ports.forEach(port -> port.setController(worldPosition));
        return true;
    }

    /** Lights (or darkens) the controller and every casing and port when the burn starts or stops. */
    private void setShellLit(ServerLevel level, boolean lit) {
        if (lit == shellLit) {
            return;
        }
        shellLit = lit;
        BlockState own = getBlockState();
        if (own.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(worldPosition, own.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
        for (BlockPos p : shell()) {
            BlockState block = level.getBlockState(p);
            if ((block.is(ModBlocks.REACTOR_CASING.get()) || block.is(ModBlocks.REACTOR_PORT.get()))
                    && block.getValue(MachineBlock.LIT) != lit) {
                level.setBlock(p, block.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
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
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        energy.deserialize(input);
        plasma.deserialize(input.childOrEmpty("plasma"));
        tritium.deserialize(input.childOrEmpty("tritium"));
        heat = input.getIntOr("heat", 0);
        burnTicks = input.getIntOr("burn_ticks", 0);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        plasma.serialize(output.child("plasma"));
        tritium.serialize(output.child("tritium"));
        output.putInt("heat", heat);
        output.putInt("burn_ticks", burnTicks);
        controls.save(output);
    }

    // ---- Container: one slot for fuel pellets ----

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.reactor_controller");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FusionReactorMenu(containerId, inventory, this, dataAccess);
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModItems.FUSION_FUEL_PELLET.get());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
