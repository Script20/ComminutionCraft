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
import java.util.List;
import java.util.function.Supplier;
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
                        new ProcessingCategory.Station("jei.comminutioncraft.time.coal_granulator", CoalGranulatorBlockEntity.PROCESS_TIME),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.electric_granulator", ElectricGranulatorBlockEntity.PROCESS_TIME),
                        new ProcessingCategory.Station("jei.comminutioncraft.time.plasma_granulator", PlasmaGranulatorBlockEntity.PROCESS_TIME)),
                        List.of()),
                new ProcessingCategory<>(gui, ASSEMBLING.get(), Component.translatable("jei.comminutioncraft.assembling"),
                        ModItems.CIRCUIT_ASSEMBLER.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.circuit_assembler", CircuitAssemblerBlockEntity.PROCESS_TIME)),
                        List.of(Component.translatable("jei.comminutioncraft.uses_plasma", CircuitAssemblerBlockEntity.PLASMA_PER_OPERATION))),
                new ProcessingCategory<>(gui, WASHING.get(), Component.translatable("jei.comminutioncraft.washing"),
                        ModItems.ORE_WASHER.get(), List.of(
                        new ProcessingCategory.Station("jei.comminutioncraft.time.ore_washer", OreWasherBlockEntity.PROCESS_TIME)),
                        List.of(Component.translatable("jei.comminutioncraft.uses_water_plasma",
                                OreWasherBlockEntity.WATER_PER_OPERATION, OreWasherBlockEntity.PLASMA_PER_OPERATION))));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(GRANULATING.get(), ClientRecipes.granulating());
        registration.addRecipes(ASSEMBLING.get(), ClientRecipes.assembling());
        registration.addRecipes(WASHING.get(), ClientRecipes.washing());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(GRANULATING.get(),
                ModItems.COAL_GRANULATOR.get(), ModItems.ELECTRIC_GRANULATOR.get(), ModItems.PLASMA_GRANULATOR.get());
        registration.addCraftingStation(RecipeTypes.SMELTING,
                ModItems.COAL_SMELTER.get(), ModItems.ELECTRIC_SMELTER.get(), ModItems.PLASMA_SMELTER.get());
        registration.addCraftingStation(ASSEMBLING.get(), ModItems.CIRCUIT_ASSEMBLER.get());
        registration.addCraftingStation(WASHING.get(), ModItems.ORE_WASHER.get());
        // Everything that burns furnace fuel
        registration.addCraftingStation(RecipeTypes.SMELTING_FUEL,
                ModItems.COAL_GRANULATOR.get(), ModItems.COAL_SMELTER.get(), ModItems.ELECTRIC_GENERATOR.get());
    }

    /** Granulated forms of other mods' metals that aren't in this pack stay out of the ingredient list. */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        List<ItemStack> unused = ProcessedMetal.ALL.stream()
                .filter(metal -> !metal.isAvailable())
                .map(metal -> new ItemStack(metal.granulated().get()))
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

        // Both electric machines share one screen class, so pick the category from the menu type.
        registration.addGuiContainerHandler(ElectricMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(ElectricMachineScreen screen, double mouseX, double mouseY) {
                var type = screen.getMenu().getType() == ModMenus.ELECTRIC_GRANULATOR.get() ? GRANULATING.get() : RecipeTypes.SMELTING;
                return List.of(IGuiClickableArea.createBasic(ARROW_X, ARROW_Y, ARROW_W, ARROW_H, type));
            }
        });

        // Tier-3 machines share one screen class too.
        registration.addGuiContainerHandler(PlasmaMachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<IGuiClickableArea> getGuiClickableAreas(PlasmaMachineScreen screen, double mouseX, double mouseY) {
                var menuType = screen.getMenu().getType();
                IRecipeType<?> type;
                if (menuType == ModMenus.PLASMA_GRANULATOR.get()) {
                    type = GRANULATING.get();
                } else if (menuType == ModMenus.CIRCUIT_ASSEMBLER.get()) {
                    type = ASSEMBLING.get();
                } else if (menuType == ModMenus.ORE_WASHER.get()) {
                    type = WASHING.get();
                } else {
                    type = RecipeTypes.SMELTING;
                }
                return List.of(IGuiClickableArea.createBasic(ARROW_X, ARROW_Y, ARROW_W, ARROW_H, type));
            }
        });
    }
}
