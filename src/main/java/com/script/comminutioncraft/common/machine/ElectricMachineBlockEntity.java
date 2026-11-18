package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeHolder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraft.world.level.material.Fluid;
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
 * <p>
 * Factories are the same machines with several {@link #lanes() lanes} working side by side: slots are the lanes'
 * inputs, then their outputs, then anything extra (a by-product). One lane = the usual input 0, output 1. Inputs
 * piped in are spread over the lanes, and a lane that runs dry takes half of the fullest one.
 */
public abstract class ElectricMachineBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, MachineExperience.Holder, ControllableMachine,
        UpgradeHolder {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

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
    /** Which upgrades the machine takes (bit per UpgradeType), for the Upgrades panel. */
    public static final int DATA_UPGRADES = 8;
    public static final int DATA_COUNT = 9;

    public static final ConfigInt ENERGY_CAPACITY = MachineConfig.ELECTRIC_MACHINES_ENERGY_CAPACITY;
    public static final ConfigInt MAX_ENERGY_INPUT = MachineConfig.ELECTRIC_MACHINES_MAX_ENERGY_INPUT;
    /** Tier-3 machines (Plasma Granulator / Smelter, Ore Washer, Circuit Assembler) hold more and take FE faster. */
    public static final ConfigInt TIER3_ENERGY_CAPACITY = MachineConfig.PLASMA_MACHINES_ENERGY_CAPACITY;
    public static final ConfigInt TIER3_MAX_ENERGY_INPUT = MachineConfig.PLASMA_MACHINES_MAX_ENERGY_INPUT;
    /** How long the front stays lit after work stops, so a machine on a thin power supply doesn't flicker. */
    private static final int LIT_COOLDOWN = 10;

    private final int lanes;
    private final int[] inputSlots;
    private final int[] outputSlots;
    protected NonNullList<ItemStack> items;
    private final MachineEnergyStorage energy;
    /** Ticks of work done on each lane's current item. */
    private final int[] progress;
    private int litCooldown;
    private final @Nullable Item[] lastInputItem;
    private final MachineExperience experience = new MachineExperience();
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private final MachineUpgrades upgrades =
            new MachineUpgrades(this::onUpgradesChanged, this::consumedFluids);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            // after the machine's own values: the progress of lanes 1, 2, ... (lane 0 is DATA_PROGRESS)
            return index >= getDataCount() ? progress[index - getDataCount() + 1] : getData(index);
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROGRESS) {
                progress[0] = value;
            }
        }

        @Override
        public int getCount() {
            return getDataCount() + lanes - 1;
        }
    };

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, ENERGY_CAPACITY.get(), MAX_ENERGY_INPUT.get());
    }

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         int energyCapacity, int maxEnergyInput) {
        this(type, pos, state, energyCapacity, maxEnergyInput, 1);
    }

    /** A factory: {@code lanes} inputs and outputs working side by side; its FE buffer and intake scale with them. */
    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         int energyCapacity, int maxEnergyInput, int lanes) {
        super(type, pos, state);
        this.lanes = lanes;
        inputSlots = new int[lanes];
        outputSlots = new int[lanes];
        for (int i = 0; i < lanes; i++) {
            inputSlots[i] = i;
            outputSlots[i] = lanes + i;
        }
        progress = new int[lanes];
        lastInputItem = new Item[lanes];
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        energy = new MachineEnergyStorage(energyCapacity * lanes, maxEnergyInput * lanes, 0, this::setChanged);
    }

    /** How many lanes work side by side (1 for a plain machine, 2-4 for a factory). */
    public int lanes() {
        return lanes;
    }

    public boolean isFactory() {
        return lanes > 1;
    }

    /** A lane's input and output slots. */
    public int inputSlot(int lane) {
        return lane;
    }

    public int outputSlot(int lane) {
        return lanes + lane;
    }

    /** The first slot after the lanes (a purifier's by-product). */
    protected int extraSlot() {
        return 2 * lanes;
    }

    /** What a factory's GUI needs to lay itself out (sent when it opens): lanes, fluid gauge, by-product slot, block id. */
    public void writeFactoryLayout(RegistryFriendlyByteBuf buffer) {
        FactoryMenu.Layout layout = factoryLayout();
        buffer.writeVarInt(layout.lanes());
        buffer.writeVarInt(layout.fluid());
        buffer.writeBoolean(layout.byproduct());
        buffer.writeUtf(layout.block());
    }

    private FactoryMenu.Layout factoryLayout() {
        return new FactoryMenu.Layout(lanes, factoryFluid(), getContainerSize() > 2 * lanes,
                BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath());
    }

    /** Which fluid gauge a factory shows: 0 none, 1 water, 2 oxygen (their amounts are at DATA_COUNT, +1 = capacity). */
    protected int factoryFluid() {
        return 0;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!isFactory()) {
            return super.createMenu(containerId, inventory, player);
        }
        return canOpen(player) ? new FactoryMenu(containerId, inventory, this, dataAccess, factoryLayout()) : null;
    }

    /** One synced GUI value (see the DATA_ constants); subclasses add theirs after {@link #DATA_COUNT}. */
    protected int getData(int index) {
        return switch (index) {
            case DATA_PROGRESS -> progress[0];
            case DATA_PROGRESS_TOTAL -> effectiveProcessTime();
            case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
            case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
            case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
            case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
            case DATA_REDSTONE -> controls.redstone().ordinal();
            case DATA_SIDES -> controls.packSides();
            case DATA_UPGRADES -> upgrades.allowedMask();
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
    public MachineUpgrades getUpgrades() {
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

    /** The fluids an operation uses up (decides whether Gas / Liquid upgrades fit); none by default. */
    protected Collection<Fluid> consumedFluids() {
        return List.of();
    }

    /** A per-operation fluid cost after Gas / Liquid upgrades. */
    protected int fluidCost(Fluid fluid, int amount) {
        return upgrades.fluidCost(fluid, amount);
    }

    /** Ticks of work done on the current item (for tooltips like Jade; a factory's first lane). */
    public int getProgress() {
        return progress[0];
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
        // Switched off by redstone: the work pauses (progress kept); it still accepts power and pushes finished items out.
        boolean changed = false;
        if (controls.canRun(level, pos)) {
            changed = process(level);
        } else {
            litCooldown = 0;
        }

        if (litCooldown > 0) {
            litCooldown--;
        }
        boolean lit = litCooldown > 0;
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }

        if (hasItemsToPush()) {
            pushItemsOut(level, pos);
        }
        if (isFactory() && spreadInputs()) {
            changed = true;
        }

        if (changed) {
            setChanged();
        }
    }

    /** Whether something is waiting to go out of Output faces. */
    protected boolean hasItemsToPush() {
        for (int slot : outputSlots) {
            if (!items.get(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** A factory lane that's run dry takes half of the fullest lane's input, so one stack keeps every lane busy. */
    private boolean spreadInputs() {
        for (int empty = 0; empty < lanes; empty++) {
            if (!items.get(inputSlot(empty)).isEmpty()) {
                continue;
            }
            int fullest = -1;
            for (int lane = 0; lane < lanes; lane++) {
                ItemStack stack = items.get(inputSlot(lane));
                if (stack.getCount() >= 2 && (fullest < 0 || stack.getCount() > items.get(inputSlot(fullest)).getCount())) {
                    fullest = lane;
                }
            }
            if (fullest < 0) {
                return false;
            }
            ItemStack from = items.get(inputSlot(fullest));
            items.set(inputSlot(empty), from.split(from.getCount() / 2));
            return true;
        }
        return false;
    }

    /** One tick of work on every lane; true if the slots changed. */
    private boolean process(ServerLevel level) {
        boolean changed = false;
        for (int lane = 0; lane < lanes; lane++) {
            changed |= processLane(level, lane);
        }
        return changed;
    }

    private boolean processLane(ServerLevel level, int lane) {
        ItemStack input = items.get(inputSlot(lane));

        // Swapping the input item restarts progress, like a vanilla furnace.
        Item inputItem = input.isEmpty() ? null : input.getItem();
        if (inputItem != lastInputItem[lane]) {
            lastInputItem[lane] = inputItem;
            progress[lane] = 0;
        }

        RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe = input.isEmpty() ? null : findRecipe(level, input).orElse(null);
        ItemStack result = recipe != null ? recipe.value().assemble(new SingleRecipeInput(input)) : ItemStack.EMPTY;
        boolean canProcess = !result.isEmpty() && MachineSlots.canFit(items, outputSlot(lane), result, getMaxStackSize());

        if (!canProcess) {
            progress[lane] = 0;
        } else if (hasOperationResources(recipe) && energy.consume(effectiveEnergyPerTick())) {
            litCooldown = LIT_COOLDOWN;
            progress[lane]++;
            if (progress[lane] >= effectiveProcessTime()) {
                progress[lane] = 0;
                MachineSlots.addTo(items, outputSlot(lane), result);
                input.shrink(1);
                experience.recordRecipe(recipe);
                onOperationDone(recipe);
                return true;
            }
        }
        return false;
    }

    /** Ticks of work done on one lane's item (factory GUIs). */
    public int getLaneProgress(int lane) {
        return progress[lane];
    }

    // ---- Factory Upgrade Kit: this machine becomes its factory ----

    /**
     * Hands everything to the factory replacing this machine: FE, settings, upgrades and the tanks the wrench keeps (the
     * item components), the input and output into its first lane, a by-product into its by-product slot.
     */
    public void moveInto(ElectricMachineBlockEntity factory) {
        ItemStack carrier = new ItemStack(factory.getBlockState().getBlock());
        carrier.applyComponents(collectComponents().filter(type -> type != DataComponents.CONTAINER));
        factory.applyComponentsFromItemStack(carrier);
        upgrades.clearContent();
        factory.items.set(factory.inputSlot(0), removeItemNoUpdate(inputSlot(0)));
        factory.items.set(factory.outputSlot(0), removeItemNoUpdate(outputSlot(0)));
        if (getContainerSize() > 2 * lanes && factory.getContainerSize() > 2 * factory.lanes) {
            factory.items.set(factory.extraSlot(), removeItemNoUpdate(extraSlot()));
        }
        factory.progress[0] = progress[0];
        factory.setChanged();
    }

    /** A factory is named after its block (the machine classes name themselves after the plain machine). */
    @Override
    public Component getName() {
        return isFactory() && getCustomName() == null ? getBlockState().getBlock().getName() : super.getName();
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
            Containers.dropContents(serverLevel, pos, upgrades); // (the wrench empties them first)
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
        int[] saved = input.getIntArray("lane_progress").orElse(new int[] {input.getIntOr("progress", 0)});
        for (int lane = 0; lane < lanes; lane++) {
            progress[lane] = lane < saved.length ? saved[lane] : 0;
            ItemStack laneInput = items.get(inputSlot(lane));
            lastInputItem[lane] = laneInput.isEmpty() ? null : laneInput.getItem();
        }
        experience.load(input);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        upgrades.save(output);
        output.putInt("progress", progress[0]);
        output.putIntArray("lane_progress", progress);
        experience.save(output);
        controls.save(output);
    }

    // ---- Container ----

    @Override
    public int getContainerSize() {
        return 2 * lanes;
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
        if (slot >= lanes) {
            return false; // outputs (and anything after them)
        }
        return !(level instanceof ServerLevel serverLevel) || findRecipe(serverLevel, stack).isPresent();
    }

    // ---- Sided automation (hoppers, pipes): each face follows the side configuration ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (sideMode(side)) {
            case INPUT -> inputSlots;
            case OUTPUT -> outputSlots;
            case NONE, BYPRODUCT -> SLOTS_NONE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack) && isLeastFull(slot, stack);
    }

    /**
     * A factory spreads what's piped in: a lane only takes more when no other lane that could hold the item has less
     * (an empty lane counts as nothing).
     */
    private boolean isLeastFull(int slot, ItemStack stack) {
        int count = items.get(slot).getCount();
        for (int lane = 0; lane < lanes; lane++) {
            ItemStack other = items.get(inputSlot(lane));
            if ((other.isEmpty() || ItemStack.isSameItemSameComponents(other, stack)) && other.getCount() < count) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= lanes && slot < 2 * lanes && sideMode(side) == SideMode.OUTPUT;
    }
}
