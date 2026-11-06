package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.registry.ModRecipes;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
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

/** Tier 5 granulator: every granulating recipe (raw ore -> 2, washed / purified ore -> 1), on FE alone, the fastest. */
public class AntimatterGranulatorBlockEntity extends ElectricMachineBlockEntity {
    public static final ConfigInt PROCESS_TIME = MachineConfig.ANTIMATTER_GRANULATOR_PROCESS_TIME;
    public static final ConfigInt ENERGY_PER_TICK = MachineConfig.ANTIMATTER_GRANULATOR_ENERGY_PER_TICK;

    private final RecipeManager.CachedCheck<SingleRecipeInput, GranulatingRecipe> recipeCheck = RecipeManager.createCheck(ModRecipes.GRANULATING_TYPE.get());

    public AntimatterGranulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANTIMATTER_GRANULATOR.get(), pos, state,
                MachineConfig.ANTIMATTER_MACHINES_ENERGY_CAPACITY.get(), MachineConfig.ANTIMATTER_MACHINES_MAX_ENERGY_INPUT.get());
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
        return Component.translatable("block.comminutioncraft.antimatter_granulator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.ANTIMATTER_GRANULATOR.get(), containerId, inventory, this, dataAccess);
    }
}
