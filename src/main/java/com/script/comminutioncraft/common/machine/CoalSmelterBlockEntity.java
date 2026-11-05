package com.script.comminutioncraft.common.machine;

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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier 1 smelter: runs normal furnace (smelting) recipes, including granulated ore -> ingot.
 * <p>
 * Twice as fast as a vanilla furnace (100 ticks vs 200). Fuel burns at 0.625x its normal
 * duration, so one coal = 1600 * 0.625 = 1000 ticks = 10 items (vs 8 in a furnace).
 */
public class CoalSmelterBlockEntity extends FuelMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.COAL_SMELTER_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigDouble BURN_TIME_MULTIPLIER = com.script.comminutioncraft.common.config.MachineConfig.COAL_SMELTER_BURN_TIME_MULTIPLIER;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> recipeCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public CoalSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_SMELTER.get(), pos, state);
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
        return RecipeType.SMELTING;
    }

    @Override
    protected Optional<RecipeHolder<SmeltingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.coal_smelter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FuelMachineMenu(ModMenus.COAL_SMELTER.get(), containerId, inventory, this, dataAccess, getFuelRecipeType());
    }
}
