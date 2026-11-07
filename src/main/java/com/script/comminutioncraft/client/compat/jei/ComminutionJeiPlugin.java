package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.ClientRecipes;
import com.script.comminutioncraft.client.gui.CoalGranulatorScreen;
import com.script.comminutioncraft.client.gui.CoalSmelterScreen;
import com.script.comminutioncraft.client.gui.ElectricGeneratorScreen;
import com.script.comminutioncraft.client.gui.ElectricMachineScreen;
import com.script.comminutioncraft.client.gui.MachineScreen;
import com.script.comminutioncraft.common.data.ProcessedMetal;
import com.script.comminutioncraft.common.recipe.AssemblingRecipe;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.recipe.PressingRecipe;
import com.script.comminutioncraft.common.recipe.PurifyingRecipe;
import com.script.comminutioncraft.common.recipe.SingularityRecipe;
import com.script.comminutioncraft.common.machine.OrePurifierBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressBlockEntity;
import com.script.comminutioncraft.common.recipe.WashingRecipe;
import com.script.comminutioncraft.client.gui.PlasmaMachineScreen;
import com.script.comminutioncraft.common.machine.CircuitAssemblerBlockEntity;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.OreWasherBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlockEntity;
import net.minecraft.network.chat.Component;
import com.script.comminutioncraft.common.registry.ModItems;
import com.script.comminutioncraft.common.registry.ModMenus;
import com.script.comminutioncraft.common.registry.ModRecipes;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.machine.ElectrolyserBlockEntity;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlockEntity;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.TritiumBreederBlockEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * JEI: a Granulating category (both granulators), our smelters and the generator listed as smelting / fuel
 * stations, and clickable progress arrows and flames in our GUIs. Found by JEI through {@link JeiPlugin};
 * JEI is compileOnly, so none of this loads without it.
 */
@JeiPlugin
public class ComminutionJeiPlugin implements IModPlugin {
    public static final Supplier<IRecipeHolderType<GranulatingRecipe>> GRANULATING =
            IRecipeHolderType.createDeferred(ModRecipes.GRANULATING_TYPE::get);
    public static final Supplier<IRecipeHolderType<AssemblingRecipe>> ASSEMBLING =
            IRecipeHolderType.createDeferred(ModRecipes.ASSEMBLING_TYPE::get);
    public static final Supplier<IRecipeHolderType<WashingRecipe>> WASHING =
            IRecipeHolderType.createDeferred(ModRecipes.WASHING_TYPE::get);
    public static final Supplier<IRecipeHolderType<com.script.comminutioncraft.common.recipe.FlotationRecipe>> FLOTATION =
            IRecipeHolderType.createDeferred(ModRecipes.FLOTATION_TYPE::get);
    public static final Supplier<IRecipeHolderType<com.script.comminutioncraft.common.recipe.SingularityRecipe>> SINGULARITY =
            IRecipeHolderType.createDeferred(ModRecipes.SINGULARITY_TYPE::get);
    public static final Supplier<IRecipeHolderType<PressingRecipe>> PRESSING =
            IRecipeHolderType.createDeferred(ModRecipes.PRESSING_TYPE::get);
    public static final Supplier<IRecipeHolderType<PurifyingRecipe>> PURIFYING =
            IRecipeHolderType.createDeferred(ModRecipes.PURIFYING_TYPE::get);

    // Positions match the GUI textures (see MachineScreen)
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;

    @Override
    public Identifier getPluginUid() {
        return ComminutionCraft.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ProcessingCategory<>(gui, GRANULATING.get(), Component.translatable("jei.comminutioncraft.granulating"),
                        ModItems.COAL_GRANULATOR.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.coal_granulator", CoalGranulatorBlockEntity.PROCESS_TIME.get()),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.electric_granulator", ElectricGranulatorBlockEntity.PROCESS_TIME.get()),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.plasma_granulator", PlasmaGranulatorBlockEntity.PROCESS_TIME.get()),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.antimatter_granulator",
                                com.script.comminutioncraft.common.machine.AntimatterGranulatorBlockEntity.PROCESS_TIME.get())),
                        List.of(), recipe -> recipe.getMinTier() >= 2
                                ? List.of(Component.translatable("jei.comminutioncraft.electric_granulators_only"))
                                : List.of()),
                new ProcessingCategory<>(gui, ASSEMBLING.get(), Component.translatable("jei.comminutioncraft.assembling"),
                        ModItems.CIRCUIT_ASSEMBLER.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.circuit_assembler", CircuitAssemblerBlockEntity.PROCESS_TIME.get())),
                        List.of()).withFluids(
                        new ProcessingCategory.FluidCost(ModFluids.PLASMA.get(), CircuitAssemblerBlockEntity.PLASMA_PER_OPERATION.get()))
                        .withRecipeFluids(recipe -> recipe.getAntimatter() > 0
                                ? List.of(new ProcessingCategory.FluidCost(ModFluids.ANTIMATTER.get(), recipe.getAntimatter()))
                                : List.of()),
                new ProcessingCategory<>(gui, WASHING.get(), Component.translatable("jei.comminutioncraft.washing"),
                        ModItems.ORE_WASHER.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.ore_washer", OreWasherBlockEntity.PROCESS_TIME.get()),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.antimatter_washer",
                                com.script.comminutioncraft.common.machine.AntimatterWasherBlockEntity.PROCESS_TIME.get())),
                        List.of()).withFluids(new ProcessingCategory.FluidCost(Fluids.WATER, OreWasherBlockEntity.WATER_PER_OPERATION.get())),
                new ProcessingCategory<>(gui, PRESSING.get(), Component.translatable("jei.comminutioncraft.pressing"),
                        ModItems.PELLET_PRESS.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.pellet_press", PelletPressBlockEntity.PROCESS_TIME.get())),
                        List.of()).withFluids(
                        new ProcessingCategory.FluidCost(ModFluids.DEUTERIUM.get(), PelletPressBlockEntity.DEUTERIUM_PER_PELLET.get()),
                        new ProcessingCategory.FluidCost(ModFluids.TRITIUM.get(), PelletPressBlockEntity.TRITIUM_PER_PELLET.get())),
                new ProcessingCategory<>(gui, PURIFYING.get(), Component.translatable("jei.comminutioncraft.purifying"),
                        ModItems.ORE_PURIFIER.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.ore_purifier", OrePurifierBlockEntity.PROCESS_TIME.get()),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.antimatter_purifier",
                                com.script.comminutioncraft.common.machine.AntimatterPurifierBlockEntity.PROCESS_TIME.get())),
                        List.of(), recipe -> List.of(),
                        new ProcessingCategory.SecondOutput<>(PurifyingRecipe::getByproduct, PurifyingRecipe::getChance)).withFluids(
                        new ProcessingCategory.FluidCost(ModFluids.OXYGEN.get(), OrePurifierBlockEntity.OXYGEN_PER_OPERATION.get())),
                new ProcessingCategory<>(gui, FLOTATION.get(), Component.translatable("jei.comminutioncraft.flotation"),
                        ModItems.FROTH_FLOTATION_CELL.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.froth_flotation_cell",
                                com.script.comminutioncraft.common.machine.FrothFlotationCellBlockEntity.PROCESS_TIME.get())),
                        List.of()).withFluids(
                        new ProcessingCategory.FluidCost(Fluids.WATER, com.script.comminutioncraft.common.machine.FrothFlotationCellBlockEntity.WATER_PER_OPERATION.get()),
                        new ProcessingCategory.FluidCost(ModFluids.OXYGEN.get(), com.script.comminutioncraft.common.machine.FrothFlotationCellBlockEntity.OXYGEN_PER_OPERATION.get())),
                new ProcessingCategory<>(gui, SINGULARITY.get(), Component.translatable("jei.comminutioncraft.singularity"),
                        ModItems.SINGULARITY_PROCESSOR.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.singularity_processor",
                                com.script.comminutioncraft.common.machine.SingularityProcessorBlockEntity.PROCESS_TIME.get())),
                        List.of(Component.translatable("jei.comminutioncraft.singularity.needs_hole",
                                com.script.comminutioncraft.common.machine.SingularityProcessorBlockEntity.REACTOR_RANGE.get())),
                        recipe -> recipe.getByproducts().stream().map(b -> (Component) Component.translatable(
                                "jei.comminutioncraft.singularity.byproduct", b.item().create().getHoverName(),
                                ProcessingCategory.percent(b.chance()))).toList(),
                        new ProcessingCategory.SecondOutput<SingularityRecipe>(recipe -> recipe.getByproducts().stream()
                                .map(b -> new ProcessingCategory.Chance(b.item(), b.chance())).toList())),
                new FusionReactorCategory(gui),
                new ParticleAcceleratorCategory(gui),
                new EventHorizonCategory(gui));
        for (FluidMachinePage page : fluidMachinePages()) {
            registration.addRecipeCategories(new FluidConversionCategory(gui, page.type(), page.machine()));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GRANULATING.get(), ClientRecipes.granulating());
        registration.addRecipes(ASSEMBLING.get(), ClientRecipes.assembling());
        registration.addRecipes(WASHING.get(), ClientRecipes.washing());
        registration.addRecipes(PRESSING.get(), ClientRecipes.pressing());
        registration.addRecipes(FLOTATION.get(), ClientRecipes.flotation());
        registration.addRecipes(SINGULARITY.get(), ClientRecipes.singularity());
        registration.addRecipes(PURIFYING.get(), ClientRecipes.purifying());
        registration.addRecipes(FusionReactorCategory.TYPE, List.of(FusionReactorCategory.Structure.INSTANCE));
        registration.addRecipes(ParticleAcceleratorCategory.TYPE, List.of(ParticleAcceleratorCategory.Structure.INSTANCE));
        registration.addRecipes(EventHorizonCategory.TYPE, List.of(EventHorizonCategory.Structure.INSTANCE));
        for (FluidMachinePage page : fluidMachinePages()) {
            registration.addRecipes(page.type(), List.of(page.conversion()));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(GRANULATING.get(),
                ModItems.COAL_GRANULATOR.get(), ModItems.ELECTRIC_GRANULATOR.get(), ModItems.PLASMA_GRANULATOR.get(),
                ModItems.ANTIMATTER_GRANULATOR.get());
        registration.addCraftingStation(RecipeTypes.SMELTING,
                ModItems.COAL_SMELTER.get(), ModItems.ELECTRIC_SMELTER.get(), ModItems.PLASMA_SMELTER.get(),
                ModItems.ANTIMATTER_SMELTER.get());
        registration.addCraftingStation(ASSEMBLING.get(), ModItems.CIRCUIT_ASSEMBLER.get());
        registration.addCraftingStation(WASHING.get(), ModItems.ORE_WASHER.get(), ModItems.ANTIMATTER_WASHER.get());
        registration.addCraftingStation(FLOTATION.get(), ModItems.FROTH_FLOTATION_CELL.get());
        registration.addCraftingStation(SINGULARITY.get(), ModItems.SINGULARITY_PROCESSOR.get());
        registration.addCraftingStation(PRESSING.get(), ModItems.PELLET_PRESS.get());
        registration.addCraftingStation(PURIFYING.get(), ModItems.ORE_PURIFIER.get(), ModItems.ANTIMATTER_PURIFIER.get());
        registration.addCraftingStation(FusionReactorCategory.TYPE, ModItems.REACTOR_CONTROLLER.get());
        registration.addCraftingStation(EventHorizonCategory.TYPE,
                ModItems.HORIZON_CONTROLLER.get(), ModItems.CONTAINMENT_FRAME.get(), ModItems.HORIZON_PORT.get());
        registration.addCraftingStation(ParticleAcceleratorCategory.TYPE,
                ModItems.ACCELERATOR_CONTROLLER.get(), ModItems.ACCELERATOR_SEGMENT.get(), ModItems.ACCELERATOR_PORT.get());
        for (FluidMachinePage page : fluidMachinePages()) {
            registration.addCraftingStation(page.type(), page.machine());
        }
        // Everything that burns furnace fuel
        registration.addCraftingStation(RecipeTypes.SMELTING_FUEL,
                ModItems.COAL_GRANULATOR.get(), ModItems.COAL_SMELTER.get(), ModItems.ELECTRIC_GENERATOR.get());
    }

    // ---- The fluid machines' pages (no recipes of their own: what they turn into what, per step) ----

    private record FluidMachinePage(IRecipeType<FluidConversionCategory.Conversion> type, ItemLike machine,
                                    FluidConversionCategory.Conversion conversion) {}

    private static final java.util.Map<String, IRecipeType<FluidConversionCategory.Conversion>> PAGE_TYPES = new java.util.HashMap<>();

    private static IRecipeType<FluidConversionCategory.Conversion> pageType(String name) {
        return PAGE_TYPES.computeIfAbsent(name, n -> IRecipeType.create(ComminutionCraft.MODID, n, FluidConversionCategory.Conversion.class));
    }

    private static FluidMachinePage page(String name, ItemLike machine, List<FluidConversionCategory.Amount> in,
                                         List<FluidConversionCategory.Amount> out, Component line) {
        return new FluidMachinePage(pageType(name), machine, new FluidConversionCategory.Conversion(in, out, line));
    }

    /** A generator's page: fluid in, FE out, with "X FE per mB" and its rate on two lines. */
    private static FluidMachinePage generatorPage(String name, ItemLike machine, List<FluidConversionCategory.Amount> in,
                                                  int energyPerMb, Component rate) {
        Component perMb = Component.translatable("jei.comminutioncraft.generator.per_mb", String.format(java.util.Locale.ROOT, "%,d", energyPerMb));
        return new FluidMachinePage(pageType(name), machine, new FluidConversionCategory.Conversion(in, List.of(), List.of(perMb, rate), true));
    }

    private static List<FluidMachinePage> fluidMachinePages() {
        int half = ElectrolyserBlockEntity.WATER_PER_HYDROGEN.get();
        return List.of(
                page("atmospheric_water_generator", ModItems.ATMOSPHERIC_WATER_GENERATOR.get(), List.of(),
                        List.of(new FluidConversionCategory.Amount(Fluids.WATER, 1,
                                Component.translatable("jei.comminutioncraft.water_generator.note"))),
                        Component.translatable("jei.comminutioncraft.water_generator.line")),
                page("electrolyser", ModItems.ELECTROLYSER.get(),
                        List.of(new FluidConversionCategory.Amount(Fluids.WATER, half * 2)),
                        List.of(new FluidConversionCategory.Amount(ModFluids.HYDROGEN.get(), 2),
                                new FluidConversionCategory.Amount(ModFluids.OXYGEN.get(), 1,
                                        Component.translatable("jei.comminutioncraft.byproduct_face"))),
                        Component.translatable("jei.comminutioncraft.fe_per_mb", ElectrolyserBlockEntity.ENERGY_PER_HYDROGEN.get(),
                                ElectrolyserBlockEntity.MAX_HYDROGEN_PER_TICK.get())),
                page("plasma_generator", ModItems.PLASMA_GENERATOR.get(),
                        List.of(new FluidConversionCategory.Amount(ModFluids.HYDROGEN.get(), 1)),
                        List.of(new FluidConversionCategory.Amount(ModFluids.PLASMA.get(), 1)),
                        Component.translatable("jei.comminutioncraft.fe_per_mb", PlasmaGeneratorBlockEntity.ENERGY_PER_MB.get(),
                                PlasmaGeneratorBlockEntity.MAX_PLASMA_PER_TICK.get())),
                generatorPage("mhd_generator", ModItems.MHD_GENERATOR.get(),
                        List.of(new FluidConversionCategory.Amount(ModFluids.PLASMA.get(), 1)), MhdGeneratorBlockEntity.ENERGY_PER_MB.get(),
                        Component.translatable("jei.comminutioncraft.generator.up_to", String.format(java.util.Locale.ROOT, "%,d",
                                MhdGeneratorBlockEntity.ENERGY_PER_MB.get() * MhdGeneratorBlockEntity.MAX_PLASMA_PER_TICK.get()))),
                generatorPage("annihilation_generator", ModItems.ANNIHILATION_GENERATOR.get(),
                        List.of(new FluidConversionCategory.Amount(ModFluids.ANTIMATTER.get(), 1)),
                        com.script.comminutioncraft.common.machine.AnnihilationGeneratorBlockEntity.ENERGY_PER_MB.get(),
                        Component.translatable("jei.comminutioncraft.generator.while_burning", String.format(java.util.Locale.ROOT, "%,d",
                                com.script.comminutioncraft.common.machine.AnnihilationGeneratorBlockEntity.energyPerTick()))),
                page("isotope_separator", ModItems.ISOTOPE_SEPARATOR.get(),
                        List.of(new FluidConversionCategory.Amount(Fluids.WATER, IsotopeSeparatorBlockEntity.WATER_PER_MB.get())),
                        List.of(new FluidConversionCategory.Amount(ModFluids.DEUTERIUM.get(), 1)),
                        Component.translatable("jei.comminutioncraft.fe_per_mb", IsotopeSeparatorBlockEntity.ENERGY_PER_MB.get(),
                                IsotopeSeparatorBlockEntity.MAX_DEUTERIUM_PER_TICK.get())),
                page("tritium_breeder", ModItems.TRITIUM_BREEDER.get(),
                        List.of(new FluidConversionCategory.Amount(ModFluids.DEUTERIUM.get(), 1)),
                        List.of(new FluidConversionCategory.Amount(ModFluids.TRITIUM.get(), 1)),
                        Component.translatable("jei.comminutioncraft.fe_per_mb", TritiumBreederBlockEntity.ENERGY_PER_MB.get(),
                                TritiumBreederBlockEntity.MAX_TRITIUM_PER_TICK.get())));
    }

    /** Granulated and washed forms of other mods' metals that aren't in this pack stay out of the ingredient list. */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        List<ItemStack> unused = ProcessedMetal.ALL.stream()
                .filter(metal -> !metal.isAvailable())
                .flatMap(metal -> Stream.of(new ItemStack(metal.granulated().get()), new ItemStack(metal.washed().get()),
                        new ItemStack(metal.concentrate().get()),
                        new ItemStack(metal.purified().get())))
                .toList();
        if (!unused.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, unused);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CoalGranulatorScreen.class, ARROW_X, ARROW_Y, ARROW_W, ARROW_H, GRANULATING.get());
        registration.addRecipeClickArea(CoalSmelterScreen.class, ARROW_X, ARROW_Y, ARROW_W, ARROW_H, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(CoalGranulatorScreen.class, 56, 36, 14, 14, RecipeTypes.SMELTING_FUEL);
        registration.addRecipeClickArea(CoalSmelterScreen.class, 56, 36, 14, 14, RecipeTypes.SMELTING_FUEL);
        registration.addRecipeClickArea(ElectricGeneratorScreen.class, 81, 34, 14, 14, RecipeTypes.SMELTING_FUEL);

        // The redstone / side config tabs stick out of the GUI's right edge.
        registration.addGenericGuiContainerHandler(MachineScreen.class, new IGuiContainerHandler<MachineScreen<?>>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(MachineScreen<?> screen) {
                return screen.getExtraAreas();
            }
        });

        // The FE machines (both granulators, both smelters, the washer) share one screen class, so pick the category from the menu type.
        registration.addGuiContainerHandler(ElectricMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(ElectricMachineScreen screen, double mouseX, double mouseY) {
                var menuType = screen.getMenu().getType();
                IRecipeType<?> type;
                if (menuType == ModMenus.ELECTRIC_GRANULATOR.get() || menuType == ModMenus.PLASMA_GRANULATOR.get()
                        || menuType == ModMenus.ANTIMATTER_GRANULATOR.get()) {
                    type = GRANULATING.get();
                } else if (menuType == ModMenus.SINGULARITY_PROCESSOR.get()) {
                    type = SINGULARITY.get();
                } else if (menuType == ModMenus.FROTH_FLOTATION_CELL.get()) {
                    type = FLOTATION.get();
                } else if (menuType == ModMenus.ORE_WASHER.get() || menuType == ModMenus.ANTIMATTER_WASHER.get()) {
                    type = WASHING.get();
                } else if (menuType == ModMenus.PELLET_PRESS.get()) {
                    type = PRESSING.get();
                } else if (menuType == ModMenus.ORE_PURIFIER.get() || menuType == ModMenus.ANTIMATTER_PURIFIER.get()) {
                    type = PURIFYING.get();
                } else {
                    type = RecipeTypes.SMELTING;
                }
                return List.of(IGuiClickableArea.createBasic(ARROW_X, ARROW_Y, ARROW_W, ARROW_H, type));
            }
        });

        // The Circuit Assembler's screen (the only one with a plasma gauge).
        registration.addGuiContainerHandler(PlasmaMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(PlasmaMachineScreen screen, double mouseX, double mouseY) {
                return List.of(IGuiClickableArea.createBasic(ARROW_X, ARROW_Y, ARROW_W, ARROW_H, ASSEMBLING.get()));
            }
        });
    }
}
