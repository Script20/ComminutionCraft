package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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
 * Tier 3 granulator: raw ore -> 2 granulated ore (still 2x; the Ore Washer is the 3x path), on FE alone.
 * <p>
 * Almost twice as fast as the Electric Granulator (40 ticks vs 75) at 60 FE/t = 2,400 FE per operation.
 */
public class PlasmaGranulatorBlockEntity extends ElectricMachineBlockEntity {
    public static final int PROCESS_TIME = 40;
    public static final int ENERGY_PER_TICK = 60;

    private final RecipeManager.CachedCheck<SingleRecipeInput, GranulatingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.GRANULATING_TYPE.get());

    public PlasmaGranulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_GRANULATOR.get(), pos, state, TIER3_ENERGY_CAPACITY, TIER3_MAX_ENERGY_INPUT);
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
    protected Optional<RecipeHolder<GranulatingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.plasma_granulator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.PLASMA_GRANULATOR.get(), containerId, inventory, this, dataAccess);
    }
}
