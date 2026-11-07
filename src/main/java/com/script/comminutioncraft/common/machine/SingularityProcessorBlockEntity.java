package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.SingularityRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier 6 Singularity Processor, the 5x step: raw ore or an ore block straight to 5 ingots
 * ({@code comminutioncraft:singularity_processing} recipes), {@link #PROCESS_TIME} ticks at {@link #ENERGY_PER_TICK}
 * FE/t, with rare by-products (each rolled on its own chance) in a second output slot. It uses the gravity of a black
 * hole: it only works while a running Event Horizon Reactor's middle is within {@link #REACTOR_RANGE} blocks (checked
 * every second).
 */
public class SingularityProcessorBlockEntity extends ElectricMachineBlockEntity {
    public static final ConfigInt PROCESS_TIME = MachineConfig.SINGULARITY_PROCESSOR_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.SINGULARITY_PROCESSOR_ENERGY_PER_TICK;
    public static final ConfigInt REACTOR_RANGE = MachineConfig.SINGULARITY_PROCESSOR_REACTOR_RANGE;

    public static final int SLOT_BYPRODUCT = SLOT_COUNT;
    private static final int[] SLOTS_OUTPUTS = {SLOT_OUTPUT, SLOT_BYPRODUCT};

    public static final int DATA_NEAR_HOLE = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_COUNT = DATA_NEAR_HOLE + 1;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SingularityRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.SINGULARITY_TYPE.get());
    private boolean nearHole;
    private int holeCheck;

    public SingularityProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SINGULARITY_PROCESSOR.get(), pos, state,
                MachineConfig.SINGULARITY_PROCESSOR_ENERGY_CAPACITY.get(), MachineConfig.SINGULARITY_PROCESSOR_MAX_ENERGY_INPUT.get());
        items = NonNullList.withSize(SLOT_COUNT + 1, ItemStack.EMPTY);
    }

    /** Whether a running Event Horizon Reactor was in range at the last check. */
    public boolean isNearHole() {
        return nearHole;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (holeCheck-- <= 0) {
            holeCheck = 20;
            nearHole = findHole(level, pos);
        }
        super.serverTick(level, pos, state);
        // The parent pushes when the main output has something; the by-products can be waiting on their own.
        if (items.get(SLOT_OUTPUT).isEmpty() && !items.get(SLOT_BYPRODUCT).isEmpty() && getControls().canRun(level, pos)) {
            pushItemsOut(level, pos);
        }
    }

    /** A lit (running) reactor controller whose black hole is within range, in chunks that are already loaded. */
    private static boolean findHole(ServerLevel level, BlockPos pos) {
        int range = REACTOR_RANGE.get();
        int reach = range + EventHorizonReactorBlockEntity.HALF + 1; // the controller sits a few blocks from the hole
        for (int cx = (pos.getX() - reach) >> 4; cx <= (pos.getX() + reach) >> 4; cx++) {
            for (int cz = (pos.getZ() - reach) >> 4; cz <= (pos.getZ() + reach) >> 4; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                for (BlockEntity be : level.getChunk(cx, cz).getBlockEntities().values()) {
                    if (be instanceof EventHorizonReactorBlockEntity reactor && reactor.isIgnited()
                            && reactor.getCentre().closerThan(pos, range)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean hasOperationResources() {
        return nearHole;
    }

    @Override
    protected void onOperationDone(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        if (!(recipe.value() instanceof SingularityRecipe singularity) || level == null) {
            return;
        }
        for (SingularityRecipe.Byproduct byproduct : singularity.getByproducts()) {
            if (level.getRandom().nextFloat() < byproduct.chance()) {
                ItemStack stack = byproduct.item().create();
                if (MachineSlots.canFit(items, SLOT_BYPRODUCT, stack, getMaxStackSize())) {
                    MachineSlots.addTo(items, SLOT_BYPRODUCT, stack);
                }
            }
        }
    }

    @Override
    protected int getProcessTime() {
        return PROCESS_TIME.get();
    }

    @Override
    protected int getEnergyPerTick() {
        return ENERGY_PER_TICK.get();
    }

    @Override
    protected Optional<RecipeHolder<SingularityRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected int getData(int index) {
        return index == DATA_NEAR_HOLE ? (nearHole ? 1 : 0) : super.getData(index);
    }

    @Override
    protected int getDataCount() {
        return DATA_COUNT;
    }

    // ---- Three slots: input, output, by-products ----

    @Override
    public int getContainerSize() {
        return SLOT_COUNT + 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != SLOT_BYPRODUCT && super.canPlaceItem(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return sideMode(side) == SideMode.OUTPUT ? SLOTS_OUTPUTS : super.getSlotsForFace(side);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return (slot == SLOT_OUTPUT || slot == SLOT_BYPRODUCT) && sideMode(side) == SideMode.OUTPUT;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.singularity_processor");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new SingularityProcessorMenu(containerId, inventory, this, dataAccess);
    }
}
