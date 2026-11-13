package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier 3 Circuit Assembler: stamps an Electronic Circuit into a Plasma Circuit ({@code comminutioncraft:assembling}
 * recipes). Each one takes 100 ticks at 20 FE/t (2,000 FE) and 100 mB of plasma.
 */
public class CircuitAssemblerBlockEntity extends PlasmaMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.CIRCUIT_ASSEMBLER_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.CIRCUIT_ASSEMBLER_ENERGY_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt PLASMA_PER_OPERATION = com.script.comminutioncraft.common.config.MachineConfig.CIRCUIT_ASSEMBLER_PLASMA_PER_OPERATION;
    public static final com.script.comminutioncraft.common.config.ConfigInt ANTIMATTER_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.CIRCUIT_ASSEMBLER_ANTIMATTER_CAPACITY;

    // ContainerData indices after the plasma machine's
    public static final int DATA_ANTIMATTER = PlasmaMachineBlockEntity.DATA_COUNT;
    public static final int DATA_ANTIMATTER_CAPACITY = DATA_ANTIMATTER + 1;
    public static final int DATA_COUNT = DATA_ANTIMATTER_CAPACITY + 1;

    /** Antimatter for recipes that ask for it (the Antimatter Circuit); fills through Input faces like the plasma. */
    private final MachineFluidTank antimatter = new MachineFluidTank(ANTIMATTER_CAPACITY.get(), ModFluids::isAntimatter, this::setChanged);
    private final ResourceHandler<FluidResource> inputFaces = new CombinedResourceHandler<>(getTank().insertOnly(), antimatter.insertOnly());
    private final ResourceHandler<FluidResource> allTanks = new CombinedResourceHandler<>(getTank(), antimatter);

    private final RecipeManager.CachedCheck<SingleRecipeInput, AssemblingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.ASSEMBLING_TYPE.get());

    public CircuitAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CIRCUIT_ASSEMBLER.get(), pos, state);
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
    protected int getPlasmaPerOperation() {
        return PLASMA_PER_OPERATION.get();
    }

    @Override
    protected Optional<RecipeHolder<AssemblingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    public MachineFluidTank getAntimatter() {
        return antimatter;
    }

    private static int antimatterFor(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        return recipe != null && recipe.value() instanceof AssemblingRecipe assembling ? assembling.getAntimatter() : 0;
    }

    @Override
    protected java.util.Collection<net.minecraft.world.level.material.Fluid> consumedFluids() {
        return java.util.List.of(com.script.comminutioncraft.common.registry.ModFluids.PLASMA.get(), com.script.comminutioncraft.common.registry.ModFluids.ANTIMATTER.get());
    }

    @Override
    protected boolean hasOperationResources(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        return hasOperationResources() && antimatter.getAmount() >= fluidCost(com.script.comminutioncraft.common.registry.ModFluids.ANTIMATTER.get(), antimatterFor(recipe));
    }

    @Override
    protected void onOperationDone(RecipeHolder<? extends Recipe<SingleRecipeInput>> recipe) {
        super.onOperationDone(recipe);
        int used = fluidCost(com.script.comminutioncraft.common.registry.ModFluids.ANTIMATTER.get(), antimatterFor(recipe));
        if (used > 0) {
            antimatter.drain(used);
        }
    }

    /** Plasma and antimatter both go in through Input faces. */
    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return allTanks;
        }
        return sideMode(side) == SideMode.INPUT ? inputFaces : null;
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_ANTIMATTER -> antimatter.getAmount();
            case DATA_ANTIMATTER_CAPACITY -> antimatter.getCapacity();
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
        antimatter.deserialize(input.childOrEmpty("antimatter"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        antimatter.serialize(output.child("antimatter"));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.circuit_assembler");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PlasmaMachineMenu(ModMenus.CIRCUIT_ASSEMBLER.get(), containerId, inventory, this, dataAccess);
    }
}
