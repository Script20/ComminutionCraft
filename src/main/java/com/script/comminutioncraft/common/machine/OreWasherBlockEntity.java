package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 3 Ore Washer, the 3x path: raw ore or an ore block -> 3 granulated ore ({@code comminutioncraft:washing}
 * recipes). Each one takes 60 ticks at 80 FE/t (4,800 FE), 250 mB of water and 5 mB of plasma.
 * <p>
 * Water comes in like plasma (pipes, tanks, buckets into a tank) through Input faces, or straight from any water
 * source block touching it: {@link #WATER_PER_SOURCE} mB/t each, without using the source up.
 */
public class OreWasherBlockEntity extends PlasmaMachineBlockEntity {
    public static final int PROCESS_TIME = 60;
    public static final int ENERGY_PER_TICK = 80;
    public static final int PLASMA_PER_OPERATION = 5;
    public static final int WATER_PER_OPERATION = 250;
    public static final int WATER_CAPACITY = 8_000;
    public static final int WATER_PER_SOURCE = 50;

    public static final int DATA_WATER = PlasmaMachineBlockEntity.DATA_COUNT;
    public static final int DATA_WATER_CAPACITY = DATA_WATER + 1;
    public static final int DATA_COUNT = DATA_WATER_CAPACITY + 1;

    private final MachineFluidTank water = new MachineFluidTank(WATER_CAPACITY,
            resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), this::setChanged);
    /** Plasma and water together, so one face takes both (each goes to its own tank). */
    private final ResourceHandler<FluidResource> tanks = new CombinedResourceHandler<>(getTank(), water);
    private final ResourceHandler<FluidResource> inputs = new CombinedResourceHandler<>(getTank().insertOnly(), water.insertOnly());

    private final RecipeManager.CachedCheck<SingleRecipeInput, WashingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.WASHING_TYPE.get());

    public OreWasherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORE_WASHER.get(), pos, state);
    }

    public MachineFluidTank getWater() {
        return water;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tanks;
        }
        return sideMode(side) == SideMode.INPUT ? inputs : null;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            if (water.getSpace() <= 0) {
                break;
            }
            if (level.getFluidState(pos.relative(direction)).isSourceOfType(Fluids.WATER)) {
                water.fill(FluidResource.of(Fluids.WATER), WATER_PER_SOURCE);
            }
        }
        super.serverTick(level, pos, state);
    }

    @Override
    protected boolean hasOperationResources() {
        return super.hasOperationResources() && water.getAmount() >= WATER_PER_OPERATION;
    }

    @Override
    protected void onOperationDone() {
        super.onOperationDone();
        water.drain(WATER_PER_OPERATION);
    }

    @Override
    protected int getProcessTime() {
        return PROCESS_TIME;
    }

    @Override
    protected int getEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    @Override
    protected int getPlasmaPerOperation() {
        return PLASMA_PER_OPERATION;
    }

    @Override
    protected Optional<RecipeHolder<WashingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_WATER -> water.getAmount();
            case DATA_WATER_CAPACITY -> water.getCapacity();
            default -> super.getData(index);
        };
    }

    @Override
    protected int getDataCount() {
        return DATA_COUNT;
    }

    // ---- Saving (a wrench pickup keeps the plasma, not the water) ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        water.deserialize(input.childOrEmpty("water"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        water.serialize(output.child("water"));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.ore_washer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new OreWasherMenu(containerId, inventory, this, dataAccess);
    }
}
