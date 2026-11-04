package com.script.comminutioncraft.common.machine;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Shared logic for every tier-1 (fuel-burning) machine: one input slot, one fuel slot,
 * one output slot. Works like a vanilla furnace, but the processing time and fuel
 * efficiency are set per machine instead of per recipe.
 */
public abstract class FuelMachineBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, MachineExperience.Holder, ControllableMachine {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    // Faces set to input accept ores and fuel; output faces give results (and empty buckets from the fuel slot).
    private static final int[] SLOTS_INPUT_FACE = {SLOT_INPUT, SLOT_FUEL};
    private static final int[] SLOTS_OUTPUT_FACE = {SLOT_OUTPUT, SLOT_FUEL};
    private static final int[] SLOTS_NONE = {};

    // ContainerData indices, synced to the open GUI
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_PROGRESS_TOTAL = 3;
    public static final int DATA_REDSTONE = 4;
    public static final int DATA_SIDES = 5;
    public static final int DATA_COUNT = 6;

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int litTimeRemaining;
    private int litTotalTime;
    private int progress;
    private @Nullable Item lastInputItem;
    private final MachineExperience experience = new MachineExperience();
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTimeRemaining;
                case DATA_LIT_DURATION -> litTotalTime;
                case DATA_PROGRESS -> progress;
                case DATA_PROGRESS_TOTAL -> getProcessTime();
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
                case DATA_PROGRESS -> progress = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected FuelMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ---- Per-machine tuning ----

    /** Ticks needed to process one item. Vanilla furnace = 200. */
    protected abstract int getProcessTime();

    /**
     * Multiplier applied to an item's normal furnace burn time.
     * Combined with {@link #getProcessTime()} this sets how many operations one fuel item gives.
     */
    protected abstract double getBurnTimeMultiplier();

    /** Recipe type passed to NeoForge's fuel lookup (lets datapacks set per-machine burn times). */
    protected abstract RecipeType<?> getFuelRecipeType();

    /** The recipe this machine would run on this input, or empty if it can't process it. */
    protected abstract Optional<? extends RecipeHolder<? extends Recipe<SingleRecipeInput>>> findRecipe(ServerLevel level, ItemStack input);

    // ---- Ticking ----

    public boolean isLit() {
        return litTimeRemaining > 0;
    }

    /** Ticks of work done on the current item (for tooltips like Jade). */
    public int getProgress() {
        return progress;
    }

    /** Ticks one item takes (for tooltips like Jade). */
    public int getTotalProcessTime() {
        return getProcessTime();
    }

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
        // Switched off by redstone: everything pauses (fuel doesn't burn down, progress is kept).
        if (!controls.canRun(level, pos)) {
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }
        boolean changed = false;

        if (isLit()) {
            litTimeRemaining--;
        }

        ItemStack input = items.get(SLOT_INPUT);
        ItemStack fuel = items.get(SLOT_FUEL);

        // Swapping the input item restarts progress, like a vanilla furnace.
        Item inputItem = input.isEmpty() ? null : input.getItem();
        if (inputItem != lastInputItem) {
            lastInputItem = inputItem;
            progress = 0;
        }

        boolean hasInput = !input.isEmpty();
        boolean hasFuel = !fuel.isEmpty();

        if (isLit() || (hasFuel && hasInput)) {
            RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe = hasInput ? findRecipe(level, input).orElse(null) : null;
            ItemStack result = recipe != null ? recipe.value().assemble(new SingleRecipeInput(input)) : ItemStack.EMPTY;
            boolean canProcess = !result.isEmpty() && MachineSlots.canFit(items, SLOT_OUTPUT, result, getMaxStackSize());

            // Light a new piece of fuel only when there's work to do.
            if (!isLit() && canProcess) {
                int burnTime = getScaledBurnTime(level, fuel);
                litTimeRemaining = burnTime;
                litTotalTime = burnTime;
                if (isLit()) {
                    changed = true;
                    MachineSlots.consumeFuel(items, SLOT_FUEL);
                }
            }

            if (isLit() && canProcess) {
                progress++;
                if (progress >= getProcessTime()) {
                    progress = 0;
                    MachineSlots.addTo(items, SLOT_OUTPUT, result);
                    input.shrink(1);
                    experience.recordRecipe(recipe);
                    changed = true;
                }
            } else {
                progress = 0;
            }
        } else if (progress > 0) {
            // Out of fuel: progress drains back down, like a vanilla furnace.
            progress = Math.max(0, progress - 2);
        }

        if (state.getValue(MachineBlock.LIT) != isLit()) {
            changed = true;
            level.setBlock(pos, state.setValue(MachineBlock.LIT, isLit()), Block.UPDATE_ALL);
        }

        if (changed) {
            setChanged();
        }
    }

    private int getScaledBurnTime(ServerLevel level, ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        int vanillaBurnTime = fuel.getBurnTime(getFuelRecipeType(), level.fuelValues());
        return (int) Math.round(vanillaBurnTime * getBurnTimeMultiplier());
    }

    protected boolean isFuel(ItemStack stack) {
        return level != null && stack.getBurnTime(getFuelRecipeType(), level.fuelValues()) > 0;
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
        }
    }

    // ---- Wrench pickup: the item keeps the settings (see WrenchItem) ----

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

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        litTimeRemaining = input.getIntOr("lit_time_remaining", 0);
        litTotalTime = input.getIntOr("lit_total_time", 0);
        progress = input.getIntOr("progress", 0);
        lastInputItem = items.get(SLOT_INPUT).isEmpty() ? null : items.get(SLOT_INPUT).getItem();
        experience.load(input);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("lit_time_remaining", litTimeRemaining);
        output.putInt("lit_total_time", litTotalTime);
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
        return switch (slot) {
            case SLOT_OUTPUT -> false;
            case SLOT_FUEL -> isFuel(stack) || (stack.is(Items.BUCKET) && !items.get(SLOT_FUEL).is(Items.BUCKET));
            default -> !(level instanceof ServerLevel serverLevel) || findRecipe(serverLevel, stack).isPresent();
        };
    }

    // ---- Sided automation (hoppers, pipes): each face follows the side configuration ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (sideMode(side)) {
            case INPUT -> SLOTS_INPUT_FACE;
            case OUTPUT -> SLOTS_OUTPUT_FACE;
            case NONE, BYPRODUCT -> SLOTS_NONE;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot != SLOT_OUTPUT && sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        // Output faces give results, plus empty buckets left over in the fuel slot.
        return sideMode(side) == SideMode.OUTPUT && (slot == SLOT_OUTPUT || (slot == SLOT_FUEL && stack.is(Items.BUCKET)));
    }
}
