package com.script.comminutioncraft.machine;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
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
public abstract class FuelMachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private static final int[] SLOTS_TOP = {SLOT_INPUT};
    private static final int[] SLOTS_BOTTOM = {SLOT_OUTPUT, SLOT_FUEL};
    private static final int[] SLOTS_SIDES = {SLOT_FUEL};

    // ContainerData indices, synced to the open GUI
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_PROGRESS_TOTAL = 3;
    public static final int DATA_COUNT = 4;

    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC =
            Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int litTimeRemaining;
    private int litTotalTime;
    private int progress;
    private @Nullable Item lastInputItem;
    /** Recipes finished since a player last emptied the output; turned into XP like a vanilla furnace. */
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTimeRemaining;
                case DATA_LIT_DURATION -> litTotalTime;
                case DATA_PROGRESS -> progress;
                case DATA_PROGRESS_TOTAL -> getProcessTime();
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, FuelMachineBlockEntity machine) {
        ServerLevel serverLevel = (ServerLevel) level;
        boolean wasLit = machine.isLit();
        boolean changed = false;

        if (machine.isLit()) {
            machine.litTimeRemaining--;
        }

        ItemStack input = machine.items.get(SLOT_INPUT);
        ItemStack fuel = machine.items.get(SLOT_FUEL);

        // Swapping the input item restarts progress, like a vanilla furnace.
        Item inputItem = input.isEmpty() ? null : input.getItem();
        if (inputItem != machine.lastInputItem) {
            machine.lastInputItem = inputItem;
            machine.progress = 0;
        }

        boolean hasInput = !input.isEmpty();
        boolean hasFuel = !fuel.isEmpty();

        if (machine.isLit() || (hasFuel && hasInput)) {
            RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe = hasInput ? machine.findRecipe(serverLevel, input).orElse(null) : null;
            ItemStack result = recipe != null ? recipe.value().assemble(new SingleRecipeInput(input)) : ItemStack.EMPTY;
            boolean canProcess = !result.isEmpty() && machine.canFitOutput(result);

            // Light a new piece of fuel only when there's work to do.
            if (!machine.isLit() && canProcess) {
                int burnTime = machine.getScaledBurnTime(serverLevel, fuel);
                machine.litTimeRemaining = burnTime;
                machine.litTotalTime = burnTime;
                if (machine.isLit()) {
                    changed = true;
                    consumeFuel(machine.items, fuel);
                }
            }

            if (machine.isLit() && canProcess) {
                machine.progress++;
                if (machine.progress >= machine.getProcessTime()) {
                    machine.progress = 0;
                    machine.finishProcessing(input, result);
                    machine.recipesUsed.addTo(recipe.id(), 1);
                    changed = true;
                }
            } else {
                machine.progress = 0;
            }
        } else if (machine.progress > 0) {
            // Out of fuel: progress drains back down, like a vanilla furnace.
            machine.progress = Math.max(0, machine.progress - 2);
        }

        if (wasLit != machine.isLit()) {
            changed = true;
            state = state.setValue(FuelMachineBlock.LIT, machine.isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }

        if (changed) {
            machine.setChanged();
        }
    }

    private int getScaledBurnTime(ServerLevel level, ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        int vanillaBurnTime = fuel.getBurnTime(getFuelRecipeType(), level.fuelValues());
        return (int) Math.round(vanillaBurnTime * getBurnTimeMultiplier());
    }

    private static void consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        // Grab the remainder (e.g. empty bucket from a lava bucket) before shrinking.
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(SLOT_FUEL, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    private boolean canFitOutput(ItemStack result) {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        int max = Math.min(getMaxStackSize(output), output.getMaxStackSize());
        return output.getCount() + result.getCount() <= max;
    }

    private void finishProcessing(ItemStack input, ItemStack result) {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.set(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }
        input.shrink(1);
    }

    protected boolean isFuel(ItemStack stack) {
        return level != null && stack.getBurnTime(getFuelRecipeType(), level.fuelValues()) > 0;
    }

    // ---- Experience ----

    /** Called when a player takes from the output slot: unlocks the recipes and drops their XP at the player. */
    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> recipes = popExperience(player.level(), player.position());
        player.awardRecipes(recipes);
        for (RecipeHolder<?> recipe : recipes) {
            player.triggerRecipeCrafted(recipe, items);
        }
    }

    /** Drops XP for every recipe finished since the last pop. Only cooking recipes carry XP. */
    private List<RecipeHolder<?>> popExperience(ServerLevel level, Vec3 position) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry : recipesUsed.reference2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                recipes.add(recipe);
                if (recipe.value() instanceof AbstractCookingRecipe cooking) {
                    awardExperience(level, position, entry.getIntValue(), cooking.experience());
                }
            });
        }
        recipesUsed.clear();
        return recipes;
    }

    private static void awardExperience(ServerLevel level, Vec3 position, int count, float perItem) {
        float total = count * perItem;
        int xp = Mth.floor(total);
        // Fractional XP becomes a chance at one more point, like a vanilla furnace.
        if (level.getRandom().nextFloat() < Mth.frac(total)) {
            xp++;
        }
        ExperienceOrb.award(level, position, xp);
    }

    /** Breaking the machine drops any XP still stored in it. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            popExperience(serverLevel, Vec3.atCenterOf(pos));
        }
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
        recipesUsed.clear();
        recipesUsed.putAll(input.read("recipes_used", RECIPES_USED_CODEC).orElse(Map.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("lit_time_remaining", litTimeRemaining);
        output.putInt("lit_total_time", litTotalTime);
        output.putInt("progress", progress);
        output.store("recipes_used", RECIPES_USED_CODEC, recipesUsed);
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

    // ---- Sided automation (hoppers, pipes) ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> SLOTS_BOTTOM;
            case UP -> SLOTS_TOP;
            default -> SLOTS_SIDES;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        // From the bottom you can take outputs, plus empty buckets left over in the fuel slot.
        return slot == SLOT_OUTPUT || (slot == SLOT_FUEL && stack.is(Items.BUCKET));
    }
}
