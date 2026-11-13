package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.FlotationRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 5 Froth Flotation Cell, the 4x step: oxygen bubbled through water floats the metal out of raw ore (or an ore
 * block) as 4 Ore Concentrate ({@code comminutioncraft:flotation} recipes), which an Ore Washer turns 1 -> 1 into
 * Washed ore. Each operation takes {@link #PROCESS_TIME} ticks at {@link #ENERGY_PER_TICK} FE/t,
 * {@link #WATER_PER_OPERATION} mB of water and {@link #OXYGEN_PER_OPERATION} mB of oxygen. Both fluids come in through
 * Input faces.
 */
public class FrothFlotationCellBlockEntity extends ElectricMachineBlockEntity {
    public static final ConfigInt PROCESS_TIME = MachineConfig.FROTH_FLOTATION_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.FROTH_FLOTATION_ENERGY_PER_TICK;
    public static final ConfigInt WATER_PER_OPERATION = MachineConfig.FROTH_FLOTATION_WATER_PER_OPERATION;
    public static final ConfigInt OXYGEN_PER_OPERATION = MachineConfig.FROTH_FLOTATION_OXYGEN_PER_OPERATION;
    public static final ConfigInt TANK_CAPACITY = MachineConfig.FROTH_FLOTATION_TANK_CAPACITY;

    public static final int DATA_WATER = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_WATER_CAPACITY = DATA_WATER + 1;
    public static final int DATA_OXYGEN = DATA_WATER_CAPACITY + 1;
    public static final int DATA_OXYGEN_CAPACITY = DATA_OXYGEN + 1;
    public static final int DATA_COUNT = DATA_OXYGEN_CAPACITY + 1;

    private final MachineFluidTank water = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), this::setChanged);
    private final MachineFluidTank oxygen = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(ModFluids.OXYGEN_TYPE.get()), this::setChanged);
    private final ResourceHandler<FluidResource> tanks = new CombinedResourceHandler<>(water, oxygen);
    private final ResourceHandler<FluidResource> inputs = new CombinedResourceHandler<>(water.insertOnly(), oxygen.insertOnly());

    private final RecipeManager.CachedCheck<SingleRecipeInput, FlotationRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.FLOTATION_TYPE.get());

    public FrothFlotationCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FROTH_FLOTATION_CELL.get(), pos, state,
                MachineConfig.ANTIMATTER_MACHINES_ENERGY_CAPACITY.get(), MachineConfig.ANTIMATTER_MACHINES_MAX_ENERGY_INPUT.get());
    }

    public MachineFluidTank getWater() {
        return water;
    }

    public MachineFluidTank getOxygen() {
        return oxygen;
    }

    /** The fluid capability on a face: water and oxygen go in through Input faces; nothing comes out. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tanks;
        }
        return sideMode(side) == SideMode.INPUT ? inputs : null;
    }

    @Override
    protected java.util.Collection<net.minecraft.world.level.material.Fluid> consumedFluids() {
        return java.util.List.of(net.minecraft.world.level.material.Fluids.WATER, com.script.comminutioncraft.common.registry.ModFluids.OXYGEN.get());
    }

    @Override
    protected boolean hasOperationResources() {
        return water.getAmount() >= fluidCost(net.minecraft.world.level.material.Fluids.WATER, WATER_PER_OPERATION.get())
                && oxygen.getAmount() >= fluidCost(com.script.comminutioncraft.common.registry.ModFluids.OXYGEN.get(), OXYGEN_PER_OPERATION.get());
    }

    @Override
    protected void onOperationDone() {
        water.drain(fluidCost(net.minecraft.world.level.material.Fluids.WATER, WATER_PER_OPERATION.get()));
        oxygen.drain(fluidCost(com.script.comminutioncraft.common.registry.ModFluids.OXYGEN.get(), OXYGEN_PER_OPERATION.get()));
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
    protected Optional<RecipeHolder<FlotationRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_WATER -> water.getAmount();
            case DATA_WATER_CAPACITY -> water.getCapacity();
            case DATA_OXYGEN -> oxygen.getAmount();
            case DATA_OXYGEN_CAPACITY -> oxygen.getCapacity();
            default -> super.getData(index);
        };
    }

    @Override
    protected int getDataCount() {
        return DATA_COUNT;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        water.deserialize(input.childOrEmpty("water"));
        oxygen.deserialize(input.childOrEmpty("oxygen"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        water.serialize(output.child("water"));
        oxygen.serialize(output.child("oxygen"));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.froth_flotation_cell");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FrothFlotationCellMenu(containerId, inventory, this, dataAccess);
    }
}
