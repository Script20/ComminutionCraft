package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModRecipes;
import java.util.Optional;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 3 Ore Washer, the 3x path: raw ore or an ore block -> 3 granulated ore ({@code comminutioncraft:washing}
 * recipes). Each one takes 60 ticks at 80 FE/t (4,800 FE) and 250 mB of water.
 * <p>
 * Water comes in through Input faces (pipes, tanks, an Atmospheric Water Generator), or straight from any water
 * source block touching it: {@link #WATER_PER_SOURCE} mB/t each, without using the source up.
 */
public class OreWasherBlockEntity extends ElectricMachineBlockEntity {
    public static final com.script.comminutioncraft.common.config.ConfigInt PROCESS_TIME = com.script.comminutioncraft.common.config.MachineConfig.ORE_WASHER_PROCESS_TIME;
    public static final com.script.comminutioncraft.common.config.ConfigInt ENERGY_PER_TICK = com.script.comminutioncraft.common.config.MachineConfig.ORE_WASHER_ENERGY_PER_TICK;
    public static final com.script.comminutioncraft.common.config.ConfigInt WATER_PER_OPERATION = com.script.comminutioncraft.common.config.MachineConfig.ORE_WASHER_WATER_PER_OPERATION;
    public static final com.script.comminutioncraft.common.config.ConfigInt WATER_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ORE_WASHER_WATER_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt WATER_PER_SOURCE = com.script.comminutioncraft.common.config.MachineConfig.ORE_WASHER_WATER_PER_SOURCE;

    public static final int DATA_WATER = ElectricMachineBlockEntity.DATA_COUNT;
    public static final int DATA_WATER_CAPACITY = DATA_WATER + 1;
    public static final int DATA_COUNT = DATA_WATER_CAPACITY + 1;

    private final MachineFluidTank water = new MachineFluidTank(WATER_CAPACITY.get(),
            resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), this::setChanged);

    private final RecipeManager.CachedCheck<SingleRecipeInput, WashingRecipe> recipeCheck =
            RecipeManager.createCheck(ModRecipes.WASHING_TYPE.get());

    public OreWasherBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ORE_WASHER.get(), pos, state, TIER3_ENERGY_CAPACITY.get(), TIER3_MAX_ENERGY_INPUT.get());
    }

    /** For faster washers (the Antimatter Washer): their own type and FE buffer. */
    protected OreWasherBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyCapacity, int maxEnergyInput) {
        super(type, pos, state, energyCapacity, maxEnergyInput);
    }

    /** mB of water one operation uses. */
    protected int waterPerOperation() {
        return WATER_PER_OPERATION.get();
    }

    protected MenuType<OreWasherMenu> menuType() {
        return ModMenus.ORE_WASHER.get();
    }

    public MachineFluidTank getWater() {
        return water;
    }

    /** The fluid capability on a face: water goes in through Input faces; nothing comes out. */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return water;
        }
        return sideMode(side) == SideMode.INPUT ? water.insertOnly() : null;
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            if (water.getSpace() <= 0) {
                break;
            }
            if (level.getFluidState(pos.relative(direction)).isSourceOfType(Fluids.WATER)) {
                water.fill(FluidResource.of(Fluids.WATER), WATER_PER_SOURCE.get());
            }
        }
        super.serverTick(level, pos, state);
    }

    @Override
    protected boolean hasOperationResources() {
        return water.getAmount() >= waterPerOperation();
    }

    @Override
    protected void onOperationDone() {
        water.drain(waterPerOperation());
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

    // ---- Wrench pickup keeps the water too ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        water.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        water.loadFrom(components);
    }

    // ---- Saving ----

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
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new OreWasherMenu(menuType(), containerId, inventory, this, dataAccess);
    }
}
