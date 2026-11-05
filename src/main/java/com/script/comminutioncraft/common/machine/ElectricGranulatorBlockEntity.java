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
 * Tier 2 granulator: raw ore -> 2 granulated ore, on FE.
 * <p>
 * Twice as fast as the Coal Granulator (75 ticks vs 150) at 27 FE/t = 2025 FE per operation, so one coal
 * burned in an Electric Generator (32,000 FE) granulates ~15.8 raw ores (vs 10.67 in tier 1).
 */
public class ElectricGranulatorBlockEntity extends ElectricMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.ELECTRIC_GRANULATOR_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.ELECTRIC_GRANULATOR_ENERGY_PER_TICK;

    private final RecipeManager.CachedCheck<SingleRecipeInput, GranulatingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.GRANULATING_TYPE.get());

    public ElectricGranulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_GRANULATOR.get(), pos, state);
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
    protected Optional<RecipeHolder<GranulatingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.electric_granulator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.ELECTRIC_GRANULATOR.get(), containerId, inventory, this, dataAccess);
    }
}
