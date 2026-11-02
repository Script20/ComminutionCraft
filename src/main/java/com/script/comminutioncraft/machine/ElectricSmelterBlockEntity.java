package com.script.comminutioncraft.machine;

import com.script.comminutioncraft.registry.ModBlockEntities;
import com.script.comminutioncraft.registry.ModMenus;
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
 * Tier 2 smelter: runs normal furnace (smelting) recipes on FE.
 * <p>
 * Twice as fast as the Coal Smelter (50 ticks vs 100) at 40 FE/t = 2000 FE per item, so one coal
 * burned in an Electric Generator (32,000 FE) smelts 16 items (vs 10 in tier 1).
 */
public class ElectricSmelterBlockEntity extends ElectricMachineBlockEntity {
    public static final int PROCESS_TIME = 50;
    public static final int ENERGY_PER_TICK = 40;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> recipeCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public ElectricSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_SMELTER.get(), pos, state);
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
    protected Optional<RecipeHolder<SmeltingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.electric_smelter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.ELECTRIC_SMELTER.get(), containerId, inventory, this, dataAccess);
    }
}
