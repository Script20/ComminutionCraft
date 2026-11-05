package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Creative Fluid Tank (creative only, no recipe): endless fluid of the kind picked in its GUI. It stays full, pushes
 * up to a full tank's worth out of every face each tick, and anything can drain it (it refills straight away). It
 * takes nothing in. Built on the Fluid Tank, so the same renderer draws its contents.
 */
public class CreativeFluidTankBlockEntity extends FluidTankBlockEntity implements MenuProvider {
    /** Index 0 in the GUI is "no fluid"; data slot: the picked fluid's position in {@link #fluids()} + 1, or 0. */
    public static final int DATA_FLUID = 0;
    public static final int DATA_COUNT = 1;

    private static @Nullable List<Fluid> fluids;
    private @Nullable Fluid fluid;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return fluid == null ? 0 : fluids().indexOf(fluid) + 1;
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public CreativeFluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_FLUID_TANK.get(), pos, state);
    }

    /** Every fluid that can be picked: the still form of every registered fluid, in registry order (same on both sides). */
    public static List<Fluid> fluids() {
        if (fluids == null) {
            fluids = BuiltInRegistries.FLUID.stream()
                    .filter(f -> f != Fluids.EMPTY && f.isSource(f.defaultFluidState()))
                    .toList();
        }
        return fluids;
    }

    public @Nullable Fluid getFluid() {
        return fluid;
    }

    /** Picks the fluid it makes (null = none) and fills (or empties) the tank to match. */
    public void setFluid(@Nullable Fluid picked) {
        fluid = picked;
        getTank().drain(CAPACITY);
        refill();
        setChanged();
    }

    private void refill() {
        if (fluid != null) {
            getTank().fill(FluidResource.of(fluid), CAPACITY);
        }
    }

    /** Every face (and "no side") can drain it; nothing goes in. */
    @Override
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return side == null ? getTank() : getTank().extractOnly();
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        refill();
        if (fluid != null) {
            for (Direction direction : Direction.values()) {
                ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());
                if (target != null) {
                    ResourceHandlerUtil.move(getTank(), target, resource -> true, CAPACITY, null);
                    refill();
                }
            }
        }
        super.serverTick(level, pos, state); // client sync, comparator
        refill();
    }

    // ---- Saving: which fluid it makes ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fluid = input.getString("creative_fluid")
                .map(Identifier::tryParse)
                .flatMap(id -> id == null ? java.util.Optional.empty() : BuiltInRegistries.FLUID.getOptional(id))
                .orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (fluid != null) {
            output.putString("creative_fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        }
    }

    // ---- GUI: the fluid picker ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CreativeFluidTankMenu(containerId, inventory, this, dataAccess);
    }
}
