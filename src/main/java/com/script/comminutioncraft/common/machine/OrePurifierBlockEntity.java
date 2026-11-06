package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Optional;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 4 Ore Purifier: blasts Washed ore with oxygen into Purified ore ({@code comminutioncraft:purifying} recipes),
 * 60 ticks at 100 FE/t and {@link #OXYGEN_PER_OPERATION} mB of oxygen each, and with the recipe's chance (20%) also
 * gives a by-product (the metal real ores of that kind carry) in its second output slot. Oxygen comes in through
 * Input faces (from an Electrolyser's By-product face); both outputs go out of Output faces.
 */
public class OrePurifierBlockEntity extends ElectricMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.ORE_PURIFIER_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.ORE_PURIFIER_ENERGY_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt OXYGEN_PER_OPERATION = com.script.comminutioncraft.common.config.MachineConfig.ORE_PURIFIER_OXYGEN_PER_OPERATION;
    public static final com.script.comminutioncraft.common.config.ConfigInt OXYGEN_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ORE_PURIFIER_OXYGEN_CAPACITY;

    public static final int SLOT_BYPRODUCT = SLOT_COUNT;
    private static final int[] SLOTS_OUTPUTS = {SLOT_OUTPUT, SLOT_BYPRODUCT};

    public static final int DATA_OXYGEN = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_OXYGEN_CAPACITY = DATA_OXYGEN + 1;
    public static final int DATA_COUNT = DATA_OXYGEN_CAPACITY + 1;

    private final MachineFluidTank oxygen = new MachineFluidTank(OXYGEN_CAPACITY.get(),
            resource -> resource.is(ModFluids.OXYGEN_TYPE.get()), this::setChanged);

    private final RecipeManager.CachedCheck<SingleRecipeInput, PurifyingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.PURIFYING_TYPE.get());

    public OrePurifierBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ORE_PURIFIER.get(), pos, state, TIER3_ENERGY_CAPACITY.get(), TIER3_MAX_ENERGY_INPUT.get());
    }

    /** For faster purifiers (the Antimatter Purifier): their own type and FE buffer. */
    protected OrePurifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyCapacity, int maxEnergyInput) {
        super(type, pos, state, energyCapacity, maxEnergyInput);
        items = NonNullList.withSize(SLOT_COUNT + 1, ItemStack.EMPTY);
    }

    /** mB of oxygen one operation uses. */
    protected int oxygenPerOperation() {
        return OXYGEN_PER_OPERATION.get();
    }

    protected MenuType<OrePurifierMenu> menuType() {
        return ModMenus.ORE_PURIFIER.get();
    }

    public MachineFluidTank getOxygen() {
        return oxygen;
    }

    /** The fluid capability on a face: oxygen goes in through Input faces; nothing comes out. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return oxygen;
        }
        return sideMode(side) == SideMode.INPUT ? oxygen.insertOnly() : null;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);
        // The parent pushes when the main output has something; the by-product can be waiting on its own.
        if (items.get(SLOT_OUTPUT).isEmpty() && !items.get(SLOT_BYPRODUCT).isEmpty() && getControls().canRun(level, pos)) {
            pushItemsOut(level, pos);
        }
    }

    @Override
    protected boolean hasOperationResources() {
        return oxygen.getAmount() >= oxygenPerOperation();
    }

    @Override
    protected void onOperationDone(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        oxygen.drain(oxygenPerOperation());
        if (recipe.value() instanceof PurifyingRecipe purifying && level != null && level.getRandom().nextFloat() < purifying.getChance()) {
            ItemStack byproduct = purifying.getByproduct().create();
            if (MachineSlots.canFit(items, SLOT_BYPRODUCT, byproduct, getMaxStackSize())) {
                MachineSlots.addTo(items, SLOT_BYPRODUCT, byproduct);
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
    protected Optional<RecipeHolder<PurifyingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_OXYGEN -> oxygen.getAmount();
            case DATA_OXYGEN_CAPACITY -> oxygen.getCapacity();
            default -> super.getData(index);
        };
    }

    @Override
    protected int getDataCount() {
        return DATA_COUNT;
    }

    // ---- Three slots: input, output, by-product ----

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

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        oxygen.deserialize(input.childOrEmpty("oxygen"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        oxygen.serialize(output.child("oxygen"));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new OrePurifierMenu(menuType(), containerId, inventory, this, dataAccess);
    }
}
