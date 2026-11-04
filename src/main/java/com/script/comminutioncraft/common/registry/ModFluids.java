package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Tier 3's fluids: Plasma (the Circuit Assembler uses it, the MHD Generator burns it for FE), Hydrogen (what plasma is
 * made from) and Oxygen (the Electrolyser's by-product, kept for later tiers); tier 4's Deuterium (spun out of water)
 * and Tritium (bred from deuterium), the fusion fuel. They only live in tanks, pipes and
 * machines (no bucket, no world block), so their properties don't set a bucket or block. Textures come from the
 * fluid models registered in ComminutionCraftClient.
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
    public static final DeferredHolder<FluidType, FluidType> HYDROGEN_TYPE = FLUID_TYPES.register("hydrogen", () -> gas("hydrogen"));
    public static final DeferredHolder<FluidType, FluidType> OXYGEN_TYPE = FLUID_TYPES.register("oxygen", () -> gas("oxygen"));
    public static final DeferredHolder<FluidType, FluidType> DEUTERIUM_TYPE = FLUID_TYPES.register("deuterium", () -> gas("deuterium"));
    public static final DeferredHolder<FluidType, FluidType> TRITIUM_TYPE = FLUID_TYPES.register("tritium",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.comminutioncraft.tritium")
                    .lightLevel(4) // tritium glows faintly green
                    .temperature(300)
                    .density(-500)
                    .viscosity(100)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> PLASMA =
            FLUIDS.register("plasma", () -> new BaseFlowingFluid.Source(plasmaProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_PLASMA =
            FLUIDS.register("flowing_plasma", () -> new BaseFlowingFluid.Flowing(plasmaProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> HYDROGEN =
            FLUIDS.register("hydrogen", () -> new BaseFlowingFluid.Source(hydrogenProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_HYDROGEN =
            FLUIDS.register("flowing_hydrogen", () -> new BaseFlowingFluid.Flowing(hydrogenProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> OXYGEN =
            FLUIDS.register("oxygen", () -> new BaseFlowingFluid.Source(oxygenProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_OXYGEN =
            FLUIDS.register("flowing_oxygen", () -> new BaseFlowingFluid.Flowing(oxygenProperties()));

    /** A light, cold-ish gas (lighter than air, so negative density, like Mekanism's gases). */
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> DEUTERIUM =
            FLUIDS.register("deuterium", () -> new BaseFlowingFluid.Source(deuteriumProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_DEUTERIUM =
            FLUIDS.register("flowing_deuterium", () -> new BaseFlowingFluid.Flowing(deuteriumProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> TRITIUM =
            FLUIDS.register("tritium", () -> new BaseFlowingFluid.Source(tritiumProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_TRITIUM =
            FLUIDS.register("flowing_tritium", () -> new BaseFlowingFluid.Flowing(tritiumProperties()));

    private static FluidType gas(String name) {
        return new FluidType(FluidType.Properties.create()
                .descriptionId("fluid_type.comminutioncraft." + name)
                .temperature(300)
                .density(-500)
                .viscosity(100));
    }

    private static BaseFlowingFluid.Properties plasmaProperties() {
        return properties(PLASMA_TYPE, PLASMA, FLOWING_PLASMA);
    }

    private static BaseFlowingFluid.Properties hydrogenProperties() {
        return properties(HYDROGEN_TYPE, HYDROGEN, FLOWING_HYDROGEN);
    }

    private static BaseFlowingFluid.Properties oxygenProperties() {
        return properties(OXYGEN_TYPE, OXYGEN, FLOWING_OXYGEN);
    }

    private static BaseFlowingFluid.Properties deuteriumProperties() {
        return properties(DEUTERIUM_TYPE, DEUTERIUM, FLOWING_DEUTERIUM);
    }

    private static BaseFlowingFluid.Properties tritiumProperties() {
        return properties(TRITIUM_TYPE, TRITIUM, FLOWING_TRITIUM);
    }

    private static BaseFlowingFluid.Properties properties(Supplier<FluidType> type, Supplier<? extends Fluid> still,
                                                          Supplier<? extends Fluid> flowing) {
        return new BaseFlowingFluid.Properties(type, still, flowing);
    }

    private ModFluids() {}
}
