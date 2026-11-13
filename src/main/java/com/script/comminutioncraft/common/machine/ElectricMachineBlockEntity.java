package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

/**
 * Shared logic for every FE processing machine (tier 2, and tier 3 via {@link PlasmaMachineBlockEntity}): one input
 * slot, one output slot and an FE buffer. Each tick of work costs {@link #getEnergyPerTick()} FE; without power (or
 * whatever else {@link #hasOperationResources()} asks for) the machine pauses (progress is kept). Finished items are
 * pushed out of Output faces into the block there (e.g. the next machine's Input face).
 */
public abstract class ElectricMachineBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, MachineExperience.Holder, ControllableMachine,
        com.script.comminutioncraft.common.machine.upgrade.UpgradeHolder {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private static final int[] SLOTS_INPUT = {SLOT_INPUT};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};
    private static final int[] SLOTS_NONE = {};

    // ContainerData indices, synced to the open GUI (energy split in two, see EnergySync)
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_PROGRESS_TOTAL = 1;
    public static final int DATA_ENERGY_LOW = 2;
    public static final int DATA_ENERGY_HIGH = 3;
    public static final int DATA_CAPACITY_LOW = 4;
    public static final int DATA_CAPACITY_HIGH = 5;
    public static final int DATA_REDSTONE = 6;
    public static final int DATA_SIDES = 7;
    public static final int DATA_COUNT = 8;

    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ELECTRIC_MACHINES_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.ELECTRIC_MACHINES_MAX_ENERGY_INPUT;
    /** Tier-3 machines (Plasma Granulator / Smelter, Ore Washer, Circuit Assembler) hold more and take FE faster. */
    public static final com.script.comminutioncraft.common.config.ConfigInt TIER3_ENERGY_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_MACHINES_ENERGY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt TIER3_MAX_ENERGY_INPUT = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_MACHINES_MAX_ENERGY_INPUT;
    /** How long the front stays lit after work stops, so a machine on a thin power supply doesn't flicker. */
    private static final int LIT_COOLDOWN = 10;

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final MachineEnergyStorage energy;
    private int progress;
    private int litCooldown;
    private @Nullable Item lastInputItem;
    private final MachineExperience experience = new MachineExperience();
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private final com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades upgrades =
            new com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades(this::onUpgradesChanged);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return getData(index);
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROGRESS) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return getDataCount();
        }
    };

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get());
    }

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         int energyCapacity, int maxEnergyInput) {
        super(type, pos, state);
        energy = new MachineEnergyStorage(energyCapacity, maxEnergyInput, 0, this::setChanged);
    }

    /** One synced GUI value (see the DATA_ constants); subclasses add theirs after {@link #DATA_COUNT}. */
    protected int getData(int index) {
        return switch (index) {
            case DATA_PROGRESS -> progress;
            case DATA_PROGRESS_TOTAL -> effectiveProcessTime();
            case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
            case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
            case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
            case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
            case DATA_REDSTONE -> controls.redstone().ordinal();
            case DATA_SIDES -> controls.packSides();
            default -> 0;
        };
    }

    protected int getDataCount() {
        return DATA_COUNT;
    }

    // ---- Per-machine tuning ----

    /** Ticks needed to process one item. Vanilla furnace = 200. */
    protected abstract int getProcessTime();

    /** FE used per tick of work. One operation costs process time x this. */
    protected abstract int getEnergyPerTick();

    /** Anything besides FE a tick of work needs (tier 3: enough plasma for the operation). */
    protected boolean hasOperationResources() {
        return true;
    }

    /** Like {@link #hasOperationResources()}, for this recipe (the Circuit Assembler checks a recipe's antimatter). */
    protected boolean hasOperationResources(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        return hasOperationResources();
    }

    /** Called when an operation finishes (tier 3 uses up its plasma here). */
    protected void onOperationDone() {}

    /** Called when an operation finishes, with the recipe it ran (the Ore Purifier rolls its by-product here). */
    protected void onOperationDone(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        onOperationDone();
    }

    /** The recipe this machine would run on this input, or empty if it can't process it. */
    protected abstract Optional<? extends RecipeHolder<? extends Recipe<SingleRecipeInput>>> findRecipe(ServerLevel level, ItemStack input);

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    // ---- Upgrades (Speed, Energy, Gas, Liquid) ----

    @Override
    public com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades getUpgrades() {
        return upgrades;
    }

    private void onUpgradesChanged() {
        if (energy != null) {
            energy.scale(upgrades.bufferSize(), upgrades.energyPerTick());
        }
        setChanged();
    }

    /** Ticks per operation after Speed upgrades. */
    protected int effectiveProcessTime() {
        return upgrades.processTime(getProcessTime());
    }

    /** FE per tick of work after Speed and Energy upgrades. */
    protected int effectiveEnergyPerTick() {
        return upgrades.energyPerTick(getEnergyPerTick());
    }

    /** A per-operation fluid cost after Gas / Liquid upgrades. */
    protected int fluidCost(net.minecraft.world.level.material.Fluid fluid, int amount) {
        return upgrades.fluidCost(fluid, amount);
    }

    /** Ticks of work done on the current item (for tooltips like Jade). */
    public int getProgress() {
        return progress;
    }

    /** Ticks one item takes (for tooltips like Jade). */
    public int getTotalProcessTime() {
        return effectiveProcessTime();
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
        // Switched off by redstone: pauses (progress kept); it still accepts power.
        if (!controls.canRun(level, pos)) {
            litCooldown = 0;
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }
        boolean changed = false;
        ItemStack input = items.get(SLOT_INPUT);

        // Swapping the input item restarts progress, like a vanilla furnace.
        Item inputItem = input.isEmpty() ? null : input.getItem();
        if (inputItem != lastInputItem) {
            lastInputItem = inputItem;
            progress = 0;
        }

        RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe = input.isEmpty() ? null : findRecipe(level, input).orElse(null);
        ItemStack result = recipe != null ? recipe.value().assemble(new SingleRecipeInput(input)) : ItemStack.EMPTY;
        boolean canProcess = !result.isEmpty() && MachineSlots.canFit(items, SLOT_OUTPUT, result, getMaxStackSize());

        if (!canProcess) {
            progress = 0;
        } else if (hasOperationResources(recipe) && energy.consume(effectiveEnergyPerTick())) {
            litCooldown = LIT_COOLDOWN;
            progress++;
            if (progress >= effectiveProcessTime()) {
                progress = 0;
                MachineSlots.addTo(items, SLOT_OUTPUT, result);
                input.shrink(1);
                experience.recordRecipe(recipe);
                onOperationDone(recipe);
                changed = true;
            }
        }

        if (litCooldown > 0) {
            litCooldown--;
        }
        boolean lit = litCooldown > 0;
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }

        if (!items.get(SLOT_OUTPUT).isEmpty()) {
            pushItemsOut(level, pos);
        }

        if (changed) {
            setChanged();
        }
    }

    // ---- Experience ----

    @Override
    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        experience.award(player, items);
    }

    /** Breaking the machine drops any XP still stored in it. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            experience.pop(serverLevel, Vec3.atCenterOf(pos));
            net.minecraft.world.Containers.dropContents(serverLevel, pos, upgrades); // (the wrench empties them first)
        }
    }

    // ---- Wrench pickup: the item keeps the settings and stored FE (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        energy.saveTo(components);
        controls.saveTo(components);
        upgrades.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        upgrades.loadFrom(components);
        energy.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        upgrades.load(input); // first: it sets the buffer size the FE is loaded into
        energy.deserialize(input);
        progress = input.getIntOr("progress", 0);
        lastInputItem = items.get(SLOT_INPUT).isEmpty() ? null : items.get(SLOT_INPUT).getItem();
        experience.load(input);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        upgrades.save(output);
        output.putInt("progress", progress);
        experience.save(output);
        controls.save(output);
    }

    // ---- Container ----

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
        if (slot == SLOT_OUTPUT) {
            return false;
        }
        return !(level instanceof ServerLevel serverLevel) || findRecipe(serverLevel, stack).isPresent();
    }

    // ---- Sided automation (hoppers, pipes): each face follows the side configuration ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (sideMode(side)) {
            case INPUT -> SLOTS_INPUT;
            case OUTPUT -> SLOTS_OUTPUT;
            case NONE, BYPRODUCT -> SLOTS_NONE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT && sideMode(side) == SideMode.OUTPUT;
    }
}
