package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.energy.EnergySync;
import com.script.comminutioncraft.energy.MachineEnergyStorage;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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
 * Shared logic for every tier-2 (electric) processing machine: one input slot, one output slot and an FE buffer.
 * Each tick of work costs {@link #getEnergyPerTick()} FE; without power the machine pauses (progress is kept).
 */
public abstract class ElectricMachineBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, MachineExperience.Holder {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private static final int[] SLOTS_INPUT = {SLOT_INPUT};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};

    // ContainerData indices, synced to the open GUI (energy split in two, see EnergySync)
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_PROGRESS_TOTAL = 1;
    public static final int DATA_ENERGY_LOW = 2;
    public static final int DATA_ENERGY_HIGH = 3;
    public static final int DATA_CAPACITY_LOW = 4;
    public static final int DATA_CAPACITY_HIGH = 5;
    public static final int DATA_COUNT = 6;

    public static final int ENERGY_CAPACITY = 20_000;
    public static final int MAX_ENERGY_INPUT = 200;
    /** How long the front stays lit after work stops, so a machine on a thin power supply doesn't flicker. */
    private static final int LIT_COOLDOWN = 10;

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_INPUT, 0, this::setChanged);
    private int progress;
    private int litCooldown;
    private @Nullable Item lastInputItem;
    private final MachineExperience experience = new MachineExperience();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_PROGRESS_TOTAL -> getProcessTime();
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROGRESS) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ---- Per-machine tuning ----

    /** Ticks needed to process one item. Vanilla furnace = 200. */
    protected abstract int getProcessTime();

    /** FE used per tick of work. One operation costs process time x this. */
    protected abstract int getEnergyPerTick();

    /** The recipe this machine would run on this input, or empty if it can't process it. */
    protected abstract Optional<? extends RecipeHolder<? extends Recipe<SingleRecipeInput>>> findRecipe(ServerLevel level, ItemStack input);

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    // ---- Ticking ----

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
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
        } else if (energy.consume(getEnergyPerTick())) {
            litCooldown = LIT_COOLDOWN;
            progress++;
            if (progress >= getProcessTime()) {
                progress = 0;
                MachineSlots.addTo(items, SLOT_OUTPUT, result);
                input.shrink(1);
                experience.recordRecipe(recipe);
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
        }
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        energy.deserialize(input);
        progress = input.getIntOr("progress", 0);
        lastInputItem = items.get(SLOT_INPUT).isEmpty() ? null : items.get(SLOT_INPUT).getItem();
        experience.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        output.putInt("progress", progress);
        experience.save(output);
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

    // ---- Sided automation (hoppers, pipes): bottom = output, every other side = input ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? SLOTS_OUTPUT : SLOTS_INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT;
    }
}
