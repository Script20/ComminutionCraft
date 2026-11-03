package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 3 Plasma Generator: pulls nitrogen out of the air and superheats it with FE into plasma.
 * <p>
 * It breathes: every face touching an air block is an intake, and each intake makes {@link #PLASMA_PER_INTAKE} mB
 * per tick (fully walled in = nothing). With an Air Filter in its slot it runs at full speed and the filter wears
 * 1 point per mB; without one it only makes plasma every other tick. Each mB costs {@link #ENERGY_PER_MB} FE,
 * so six open faces with a filter make 6 mB/t for 96 FE/t. Plasma goes out of the faces set to Output.
 */
public class PlasmaGeneratorBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, ControllableMachine {
    public static final int SLOT_FILTER = 0;
    public static final int SLOT_COUNT = 1;
    private static final int[] SLOTS_FILTER = {SLOT_FILTER};
    private static final int[] SLOTS_NONE = {};

    public static final int PLASMA_PER_INTAKE = 1;
    public static final int ENERGY_PER_MB = 16;
    public static final int FILTER_DURABILITY = 10_000;
    public static final int ENERGY_CAPACITY = 40_000;
    public static final int MAX_ENERGY_INPUT = 400;
    public static final int PLASMA_CAPACITY = 8_000;
    /** Max mB pushed out per tick, across all output faces. */
    public static final int MAX_PLASMA_OUTPUT = 500;
    private static final int LIT_COOLDOWN = 10;

    // ContainerData indices, synced to the open GUI (big values split in two, see EnergySync)
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_PLASMA = 4;
    public static final int DATA_PLASMA_CAPACITY = 5;
    public static final int DATA_INTAKE = 6;
    public static final int DATA_RATE = 7;
    public static final int DATA_REDSTONE = 8;
    public static final int DATA_SIDES = 9;
    public static final int DATA_COUNT = 10;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_INPUT, 0, this::setChanged);
    private final MachineFluidTank tank = new MachineFluidTank(PLASMA_CAPACITY,
            resource -> resource.is(ModFluids.PLASMA_TYPE.get()), this::setChanged);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private int intake;
    /** mB made last tick (for the GUI and Jade). */
    private int rate;
    private int litCooldown;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_PLASMA -> tank.getAmount();
                case DATA_PLASMA_CAPACITY -> tank.getCapacity();
                case DATA_INTAKE -> intake;
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

    public PlasmaGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_GENERATOR.get(), pos, state);
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    /** Plasma can only be taken out, and only through output faces (and internal "no side" access). */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return sideMode(side) == SideMode.OUTPUT ? tank.extractOnly() : null;
    }

    public MachineFluidTank getTank() {
        return tank;
    }

    /** Sets the stored FE directly (creative/testing). */
    public void setEnergy(int amount) {
        energy.set(Math.clamp(amount, 0, ENERGY_CAPACITY));
    }

    public int getIntake() {
        return intake;
    }

    public int getRate() {
        return rate;
    }

    public boolean hasFilter() {
        return items.get(SLOT_FILTER).is(ModItems.AIR_FILTER.get());
    }

    /** Faces touching an air block. */
    static int countIntakes(ServerLevel level, BlockPos pos) {
        int open = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                open++;
            }
        }
        return open;
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
        intake = countIntakes(level, pos);
        rate = 0;
        if (!controls.canRun(level, pos)) {
            litCooldown = 0;
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }

        // Without a filter it only breathes every other tick.
        boolean filtered = hasFilter();
        int make = filtered || level.getGameTime() % 2 == 0 ? intake * PLASMA_PER_INTAKE : 0;
        if (make > 0 && tank.getSpace() >= make && energy.consume(make * ENERGY_PER_MB)) {
            tank.fill(FluidResource.of(ModFluids.PLASMA.get()), make);
            rate = make;
            litCooldown = LIT_COOLDOWN;
            if (filtered) {
                items.get(SLOT_FILTER).hurtAndBreak(make, level, null, item -> {});
            }
        }

        pushPlasma(level, pos);

        if (litCooldown > 0) {
            litCooldown--;
        }
        boolean lit = litCooldown > 0;
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** Offers plasma to every neighbour on an output face, up to {@link #MAX_PLASMA_OUTPUT} mB per tick in total. */
    private void pushPlasma(ServerLevel level, BlockPos pos) {
        int budget = Math.min(MAX_PLASMA_OUTPUT, tank.getAmount());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
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

    // ---- Wrench pickup: the item keeps the settings, stored FE and plasma (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        energy.saveTo(components);
        tank.saveTo(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        energy.loadFrom(components);
        tank.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        energy.deserialize(input);
        tank.deserialize(input.childOrEmpty("plasma"));
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        tank.serialize(output.child("plasma"));
        controls.save(output);
    }

    // ---- Container ----

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.plasma_generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PlasmaGeneratorMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
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
        return stack.is(ModItems.AIR_FILTER.get());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    // ---- Sided automation: input faces take filters ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return sideMode(side) == SideMode.INPUT ? SLOTS_FILTER : SLOTS_NONE;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
