package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.advancement.MachineEventTrigger;
import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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
    public static final ConfigInt PROCESS_TIME = MachineConfig.ORE_PURIFIER_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.ORE_PURIFIER_ENERGY_PER_TICK;
    public static final ConfigInt OXYGEN_PER_OPERATION = MachineConfig.ORE_PURIFIER_OXYGEN_PER_OPERATION;
    public static final ConfigInt OXYGEN_CAPACITY = MachineConfig.ORE_PURIFIER_OXYGEN_CAPACITY;

    /** The by-product slot of a plain (one-lane) purifier; a factory's comes after all its lanes ({@link #extraSlot()}). */
    public static final int SLOT_BYPRODUCT = SLOT_COUNT;

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

    /** A factory version: its own block entity type, {@code lanes} lanes. */
    public OrePurifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int lanes) {
        this(type, pos, state, TIER3_ENERGY_CAPACITY.get(), TIER3_MAX_ENERGY_INPUT.get(), lanes);
    }

    /** For faster purifiers (the Antimatter Purifier): their own type and FE buffer. */
    protected OrePurifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyCapacity, int maxEnergyInput) {
        this(type, pos, state, energyCapacity, maxEnergyInput, 1);
    }

    protected OrePurifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyCapacity, int maxEnergyInput,
                                     int lanes) {
        super(type, pos, state, energyCapacity, maxEnergyInput, lanes);
    }

    @Override
    protected int factoryFluid() {
        return 2;
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

    /** The by-product can be waiting on its own (finished items always go out, even when switched off). */
    @Override
    protected boolean hasItemsToPush() {
        return super.hasItemsToPush() || !items.get(extraSlot()).isEmpty();
    }

    @Override
    protected Collection<Fluid> consumedFluids() {
        return List.of(ModFluids.OXYGEN.get());
    }

    @Override
    protected boolean hasOperationResources() {
        return oxygen.getAmount() >= fluidCost(ModFluids.OXYGEN.get(), oxygenPerOperation());
    }

    @Override
    protected void onOperationDone(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        oxygen.drain(fluidCost(ModFluids.OXYGEN.get(), oxygenPerOperation()));
        if (recipe.value() instanceof PurifyingRecipe purifying && level != null && level.getRandom().nextFloat() < purifying.getChance()) {
            ItemStack byproduct = purifying.getByproduct().create();
            if (MachineSlots.canFit(items, extraSlot(), byproduct, getMaxStackSize())) {
                MachineSlots.addTo(items, extraSlot(), byproduct);
            }
            if (level instanceof ServerLevel server) {
                MachineEventTrigger.fireNear(server, worldPosition, MachineEventTrigger.PURIFIER_BYPRODUCT, 16);
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

    /** The oxygen goes along too (the wrench doesn't keep it, so it isn't in the item components). */
    @Override
    public void moveInto(ElectricMachineBlockEntity factory) {
        super.moveInto(factory);
        if (factory instanceof OrePurifierBlockEntity purifier && oxygen.getAmount() > 0) {
            purifier.oxygen.fill(oxygen.getFluid(), oxygen.getAmount());
        }
    }

    // ---- The lanes' inputs and outputs, then the by-product ----

    @Override
    public int getContainerSize() {
        return 2 * lanes() + 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != extraSlot() && super.canPlaceItem(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (sideMode(side) != SideMode.OUTPUT) {
            return super.getSlotsForFace(side);
        }
        int[] outputs = new int[lanes() + 1];
        for (int lane = 0; lane < lanes(); lane++) {
            outputs[lane] = outputSlot(lane);
        }
        outputs[lanes()] = extraSlot();
        return outputs;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= lanes() && slot <= extraSlot() && sideMode(side) == SideMode.OUTPUT;
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
