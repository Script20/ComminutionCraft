package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Shared logic for the tier-3 power chain's fluid machines (Electrolyser, Plasma Generator, MHD Generator): an FE
 * buffer and a few one-fluid tanks, no item slots. Input tanks fill through Input faces; output tanks push up to
 * {@link #MAX_FLUID_OUTPUT} mB/t each out of Output faces. Each tick {@link #work} turns inputs into outputs and
 * returns its rate for the GUI (mB/t, or FE/t for the MHD Generator). Redstone-off pauses everything.
 * <p>
 * Mined with a pickaxe it forgets everything; picked up with the wrench it keeps its FE and settings.
 */
public abstract class FluidMachineBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine, MenuProvider {
    public static final int TANK_CAPACITY = 8_000;
    public static final int MAX_FLUID_OUTPUT = 500;
    private static final int LIT_COOLDOWN = 10;

    // ContainerData indices, synced to the open GUI (energy split in two, see EnergySync); then amount + capacity per tank
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_REDSTONE = 4;
    public static final int DATA_SIDES = 5;
    public static final int DATA_RATE = 6;
    public static final int DATA_TANKS = 7;

    protected final MachineEnergyStorage energy;
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private final List<MachineFluidTank> tanks = new ArrayList<>();
    private final List<String> tankNames = new ArrayList<>();
    private final List<Boolean> tankIsOutput = new ArrayList<>();
    private @Nullable ResourceHandler<FluidResource> allTanks;
    private @Nullable ResourceHandler<FluidResource> inputFaces;
    private @Nullable ResourceHandler<FluidResource> outputFaces;
    private int rate;
    private int litCooldown;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= DATA_TANKS) {
                MachineFluidTank tank = tanks.get((index - DATA_TANKS) / 2);
                return (index - DATA_TANKS) % 2 == 0 ? tank.getAmount() : tank.getCapacity();
            }
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
                case DATA_RATE -> rate;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return dataCount(tanks.size());
        }
    };

    protected FluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                      int energyCapacity, int maxEnergyInput, int maxEnergyOutput) {
        super(type, pos, state);
        energy = new MachineEnergyStorage(energyCapacity, maxEnergyInput, maxEnergyOutput, this::setChanged);
    }

    public static int dataCount(int tankCount) {
        return DATA_TANKS + tankCount * 2;
    }

    /** Adds a tank (call from the constructor); {@code name} is its save key. */
    protected MachineFluidTank addTank(String name, Predicate<FluidResource> accepts, boolean output) {
        MachineFluidTank tank = new MachineFluidTank(TANK_CAPACITY, accepts, this::setChanged);
        tanks.add(tank);
        tankNames.add(name);
        tankIsOutput.add(output);
        return tank;
    }

    /** Turns inputs into outputs for one tick; returns the rate to show (0 = idle). */
    protected abstract int work(ServerLevel level);

    protected abstract MenuType<FluidMachineMenu> getMenuType();

    /** Whether this machine pushes FE out of every face (the MHD Generator does). */
    protected boolean pushesEnergy() {
        return false;
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    public List<MachineFluidTank> getTanks() {
        return tanks;
    }

    /** mB/t (or FE/t) last tick. */
    public int getRate() {
        return rate;
    }

    /** Sets the stored FE directly (creative/testing). */
    public void setEnergy(int amount) {
        energy.set(Math.clamp(amount, 0, energy.getCapacity()));
    }

    /** Fluid capability: Input faces fill the input tanks, Output faces drain the output tanks, None faces have none. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (allTanks == null) {
            List<ResourceHandler<FluidResource>> ins = new ArrayList<>();
            List<ResourceHandler<FluidResource>> outs = new ArrayList<>();
            for (int i = 0; i < tanks.size(); i++) {
                if (tankIsOutput.get(i)) {
                    outs.add(tanks.get(i).extractOnly());
                } else {
                    ins.add(tanks.get(i).insertOnly());
                }
            }
            allTanks = new CombinedResourceHandler<>(List.copyOf(tanks));
            inputFaces = new CombinedResourceHandler<>(ins);
            outputFaces = new CombinedResourceHandler<>(outs);
        }
        if (side == null) {
            return allTanks;
        }
        return switch (sideMode(side)) {
            case INPUT -> inputFaces;
            case OUTPUT -> outputFaces;
            case NONE -> null;
        };
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
        if (!controls.canRun(level, pos)) {
            rate = 0;
            litCooldown = 0;
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }
        rate = work(level);
        if (rate > 0) {
            litCooldown = LIT_COOLDOWN;
        }
        pushFluids(level, pos);
        if (pushesEnergy()) {
            pushEnergy(level, pos);
        }

        if (litCooldown > 0) {
            litCooldown--;
        }
        boolean lit = litCooldown > 0;
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** Offers each output tank's fluid to the neighbours on Output faces, up to {@link #MAX_FLUID_OUTPUT} mB per tank per tick. */
    private void pushFluids(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < tanks.size(); i++) {
            MachineFluidTank tank = tanks.get(i);
            if (!tankIsOutput.get(i) || tank.getAmount() <= 0) {
                continue;
            }
            int budget = Math.min(MAX_FLUID_OUTPUT, tank.getAmount());
            for (Direction direction : Direction.values()) {
                if (budget <= 0) {
                    break;
                }
                if (sideMode(direction) != SideMode.OUTPUT) {
                    continue;
                }
                ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());
                if (target != null) {
                    budget -= ResourceHandlerUtil.move(tank, target, resource -> true, budget, null);
                }
            }
        }
    }

    /** Like the Electric Generator: offers FE to every neighbour, up to the energy buffer's extract limit in total. */
    private void pushEnergy(ServerLevel level, BlockPos pos) {
        int budget = Math.min(energy.getMaxExtract(), energy.getEnergy());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                budget -= EnergyHandlerUtil.move(energy, target, budget, null);
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
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).deserialize(input.childOrEmpty(tankNames.get(i)));
        }
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).serialize(output.child(tankNames.get(i)));
        }
        controls.save(output);
    }

    // ---- GUI ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FluidMachineMenu(getMenuType(), containerId, inventory, this, dataAccess);
    }
}
