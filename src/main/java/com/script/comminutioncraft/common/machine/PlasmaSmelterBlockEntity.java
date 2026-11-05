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
 * Tier 3 smelter: runs normal furnace (smelting) recipes on FE alone.
 * <p>
 * Twice as fast as the Electric Smelter (25 ticks vs 50) at 80 FE/t = 2,000 FE per item.
 */
public class PlasmaSmelterBlockEntity extends ElectricMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_SMELTER_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_SMELTER_ENERGY_PER_TICK;

    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> recipeCheck =
            RecipeManager.createCheck(RecipeType.SMELTING);

    public PlasmaSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLASMA_SMELTER.get(), pos, state, TIER3_ENERGY_CAPACITY.get(), TIER3_MAX_ENERGY_INPUT.get());
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
    protected Optional<RecipeHolder<SmeltingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.plasma_smelter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ElectricMachineMenu(ModMenus.PLASMA_SMELTER.get(), containerId, inventory, this, dataAccess);
    }
}
