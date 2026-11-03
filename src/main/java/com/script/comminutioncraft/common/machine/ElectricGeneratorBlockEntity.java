package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import org.jspecify.annotations.Nullable;

/**
 * Tier 2 generator: burns any furnace fuel into FE and pushes it out of every side
 * (into cables or straight into adjacent machines).
 * <p>
 * Makes 40 FE/t while burning. Fuel burns for half its furnace time, so one coal
 * (1600 ticks in a furnace) burns 800 ticks = 32,000 FE. It only lights new fuel when its buffer has room,
 * and pauses (keeping the current fuel) while the buffer is full.
 */
public class ElectricGeneratorBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, ControllableMachine {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_COUNT = 1;
    private static final int[] SLOTS_FUEL = {SLOT_FUEL};
    private static final int[] SLOTS_NONE = {};

    public static final int ENERGY_PER_TICK = 40;
    public static final double BURN_TIME_MULTIPLIER = 0.5;
    public static final int ENERGY_CAPACITY = 40_000;
    /** Max FE pushed out per tick, across all sides. */
    public static final int MAX_OUTPUT = 400;

    // ContainerData indices, synced to the open GUI (energy split in two, see EnergySync)
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_ENERGY_LOW = 2;
    public static final int DATA_ENERGY_HIGH = 3;
    public static final int DATA_CAPACITY_LOW = 4;
    public static final int DATA_CAPACITY_HIGH = 5;
    public static final int DATA_REDSTONE = 6;
    public static final int DATA_SIDES = 7;
    public static final int DATA_COUNT = 8;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    // Nothing can be inserted from outside; extraction is what cables and machines pull through.
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, 0, MAX_OUTPUT, this::setChanged);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private int litTimeRemaining;
    private int litTotalTime;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTimeRemaining;
                case DATA_LIT_DURATION -> litTotalTime;
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LIT_TIME -> litTimeRemaining = value;
                case DATA_LIT_DURATION -> litTotalTime = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ElectricGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_GENERATOR.get(), pos, state);
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    public boolean isLit() {
        return litTimeRemaining > 0;
    }

    /** Burn ticks left on the current fuel item (for tooltips like Jade). */
    public int getLitTimeRemaining() {
        return litTimeRemaining;
    }

    // ---- Ticking ----

    @Override
    public MachineControls getControls() {
        return controls;
    }

    private void onControlsChanged() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition); // hopper/pipe faces may have changed
        }
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        // Switched off by redstone: no burning and no power out (the fuel and stored FE are kept).
        if (!controls.canRun(level, pos)) {
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }
        boolean changed = false;

        if (isLit()) {
            // Hold the flame while the buffer is full, so no fuel is wasted.
            if (energy.getSpace() >= ENERGY_PER_TICK) {
                litTimeRemaining--;
                energy.generate(ENERGY_PER_TICK);
            }
        } else if (energy.getSpace() >= ENERGY_PER_TICK) {
            ItemStack fuel = items.get(SLOT_FUEL);
            int burnTime = fuel.isEmpty() ? 0
                    : (int) Math.round(fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues()) * BURN_TIME_MULTIPLIER);
            if (burnTime > 0) {
                litTimeRemaining = burnTime;
                litTotalTime = burnTime;
                MachineSlots.consumeFuel(items, SLOT_FUEL);
                changed = true;
            }
        }

        pushEnergy(level, pos);

        if (state.getValue(MachineBlock.LIT) != isLit()) {
            changed = true;
            level.setBlock(pos, state.setValue(MachineBlock.LIT, isLit()), Block.UPDATE_ALL);
        }
        if (changed) {
            setChanged();
        }
    }

    /** Offers energy to every neighbour that accepts FE, up to {@link #MAX_OUTPUT} per tick in total. */
    private void pushEnergy(ServerLevel level, BlockPos pos) {
        int budget = Math.min(MAX_OUTPUT, energy.getEnergy());
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

    private boolean isFuel(ItemStack stack) {
        return level != null && stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0;
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
        litTimeRemaining = input.getIntOr("lit_time_remaining", 0);
        litTotalTime = input.getIntOr("lit_total_time", 0);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        output.putInt("lit_time_remaining", litTimeRemaining);
        output.putInt("lit_total_time", litTotalTime);
        controls.save(output);
    }

    // ---- Container ----

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.electric_generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricGeneratorMenu(containerId, inventory, this, dataAccess);
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
        return isFuel(stack) || (stack.is(Items.BUCKET) && !items.get(SLOT_FUEL).is(Items.BUCKET));
    }

    // ---- Sided automation: input faces take fuel, output faces give back empty buckets ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return sideMode(side) == SideMode.NONE ? SLOTS_NONE : SLOTS_FUEL;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return sideMode(side) == SideMode.OUTPUT && stack.is(Items.BUCKET);
    }
}
