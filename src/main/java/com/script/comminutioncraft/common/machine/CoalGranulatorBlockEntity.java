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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier 1 granulator: raw ore -> 2 granulated ore.
 * <p>
 * 33% faster than a vanilla furnace (150 ticks vs 200) and uses normal furnace burn
 * times, so one coal lasts 1600 / 150 = 10.67 operations (vs 8 in a furnace), which is
 * also 33% more efficient. Speed and efficiency move together at one flat rate.
 */
public class CoalGranulatorBlockEntity extends FuelMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.COAL_GRANULATOR_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigDouble BURN_TIME_MULTIPLIER = com.script.comminutioncraft.common.config.MachineConfig.COAL_GRANULATOR_BURN_TIME_MULTIPLIER;

    private final RecipeManager.CachedCheck<SingleRecipeInput, GranulatingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.GRANULATING_TYPE.get());

    public CoalGranulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GRANULATOR.get(), pos, state);
    }

    @Override
    protected int getProcessTime() {
        return PROCESS_TIME.get();
    }

    @Override
    protected double getBurnTimeMultiplier() {
        return BURN_TIME_MULTIPLIER.get();
    }

    @Override
    protected RecipeType<?> getFuelRecipeType() {
        return ModRecipes.GRANULATING_TYPE.get();
    }

    @Override
    protected Optional<RecipeHolder<GranulatingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        // Washed ore recipes are for the Electric and Plasma Granulators.
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level).filter(recipe -> recipe.value().getMinTier() <= 1);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.coal_granulator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FuelMachineMenu(ModMenus.COAL_GRANULATOR.get(), containerId, inventory, this, dataAccess, getFuelRecipeType());
    }
}
