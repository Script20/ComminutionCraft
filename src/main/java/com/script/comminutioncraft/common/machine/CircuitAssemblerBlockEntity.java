package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
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
 * Tier 3 Circuit Assembler: stamps an Electronic Circuit into a Plasma Circuit ({@code comminutioncraft:assembling}
 * recipes). Each one takes 100 ticks at 20 FE/t (2,000 FE) and 100 mB of plasma.
 */
public class CircuitAssemblerBlockEntity extends PlasmaMachineBlockEntity {
    public static final int PROCESS_TIME = 100;
    public static final int ENERGY_PER_TICK = 20;
    public static final int PLASMA_PER_OPERATION = 100;

    private final RecipeManager.CachedCheck<SingleRecipeInput, AssemblingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.ASSEMBLING_TYPE.get());

    public CircuitAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CIRCUIT_ASSEMBLER.get(), pos, state);
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
    protected Optional<RecipeHolder<AssemblingRecipe>> findRecipe(ServerLevel level, ItemStack input) {
        return recipeCheck.getRecipeFor(new SingleRecipeInput(input), level);
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
