package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Plasma: tier 3's resource. It only lives in tanks, pipes and machines (no bucket, no world block), so its
 * properties don't set a bucket or block. Textures come from the fluid model registered in ComminutionCraftClient.
 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ComminutionCraft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ComminutionCraft.MODID);

    public static final DeferredHolder<FluidType, FluidType> PLASMA_TYPE = FLUID_TYPES.register("plasma",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.comminutioncraft.plasma")
                    .lightLevel(15)
                    .temperature(10_000)
                    .density(-1000)
                    .viscosity(200)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> PLASMA =
            FLUIDS.register("plasma", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_PLASMA =
            FLUIDS.register("flowing_plasma", () -> new BaseFlowingFluid.Flowing(properties()));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(PLASMA_TYPE, PLASMA, FLOWING_PLASMA);
    }

    private ModFluids() {}
}
