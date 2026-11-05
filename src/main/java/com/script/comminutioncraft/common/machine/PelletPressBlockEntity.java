package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
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
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 4 Pellet Press: squeezes {@link #DEUTERIUM_PER_PELLET} mB of deuterium and {@link #TRITIUM_PER_PELLET} mB of
 * tritium into a casing ({@code comminutioncraft:pressing} recipes: an iron nugget -> a Fusion Fuel Pellet), 100 ticks
 * at 40 FE/t. Both gases come in through Input faces; pellets go out of Output faces like any machine's results.
 */
public class PelletPressBlockEntity extends ElectricMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.PELLET_PRESS_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.PELLET_PRESS_ENERGY_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt DEUTERIUM_PER_PELLET = com.script.comminutioncraft.common.config.MachineConfig.PELLET_PRESS_DEUTERIUM_PER_PELLET;
    public static final com.script.comminutioncraft.common.config.ConfigInt TRITIUM_PER_PELLET = com.script.comminutioncraft.common.config.MachineConfig.PELLET_PRESS_TRITIUM_PER_PELLET;
    public static final com.script.comminutioncraft.common.config.ConfigInt TANK_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.PELLET_PRESS_TANK_CAPACITY;

    public static final int DATA_DEUTERIUM = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_DEUTERIUM_CAPACITY = DATA_DEUTERIUM + 1;
    public static final int DATA_TRITIUM = DATA_DEUTERIUM_CAPACITY + 1;
    public static final int DATA_TRITIUM_CAPACITY = DATA_TRITIUM + 1;
    public static final int DATA_COUNT = DATA_TRITIUM_CAPACITY + 1;

    private final MachineFluidTank deuterium = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(ModFluids.DEUTERIUM_TYPE.get()), this::setChanged);
    private final MachineFluidTank tritium = new MachineFluidTank(TANK_CAPACITY.get(),
            resource -> resource.is(ModFluids.TRITIUM_TYPE.get()), this::setChanged);
    private final ResourceHandler<FluidResource> tanks = new CombinedResourceHandler<>(deuterium, tritium);
    private final ResourceHandler<FluidResource> inputs = new CombinedResourceHandler<>(deuterium.insertOnly(), tritium.insertOnly());

    private final RecipeManager.CachedCheck<SingleRecipeInput, PressingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.PRESSING_TYPE.get());

    public PelletPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PELLET_PRESS.get(), pos, state, TIER3_ENERGY_CAPACITY.get(), TIER3_MAX_ENERGY_INPUT.get());
    }

    public MachineFluidTank getDeuterium() {
        return deuterium;
    }

    public MachineFluidTank getTritium() {
        return tritium;
    }

    /** The fluid capability on a face: deuterium and tritium go in through Input faces; nothing comes out. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tanks;
        }
        return sideMode(side) == SideMode.INPUT ? inputs : null;
    }

    @Override
    protected boolean hasOperationResources() {
        return deuterium.getAmount() >= DEUTERIUM_PER_PELLET.get() && tritium.getAmount() >= TRITIUM_PER_PELLET.get();
    }

    @Override
    protected void onOperationDone() {
        deuterium.drain(DEUTERIUM_PER_PELLET.get());
        tritium.drain(TRITIUM_PER_PELLET.get());
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
    protected Optional<RecipeHolder<PressingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected int getData(int index) {
        return switch (index) {
            case DATA_DEUTERIUM -> deuterium.getAmount();
            case DATA_DEUTERIUM_CAPACITY -> deuterium.getCapacity();
            case DATA_TRITIUM -> tritium.getAmount();
            case DATA_TRITIUM_CAPACITY -> tritium.getCapacity();
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
        deuterium.deserialize(input.childOrEmpty("deuterium"));
        tritium.deserialize(input.childOrEmpty("tritium"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        deuterium.serialize(output.child("deuterium"));
        tritium.serialize(output.child("tritium"));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.pellet_press");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PelletPressMenu(containerId, inventory, this, dataAccess);
    }
}
