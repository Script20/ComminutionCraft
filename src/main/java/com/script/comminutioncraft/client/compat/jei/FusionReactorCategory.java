package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * How to build the Fusion Reactor (press U on its controller or any reactor block): the 3x3x3 shown as its bottom,
 * middle and top layers (seen from above, the controller's face at the bottom of each grid), then what goes in
 * (pellets, plasma to ignite) and what comes out (FE, tritium). Every block's tooltip says what may go there.
 */
public class FusionReactorCategory extends AbstractRecipeCategory<FusionReactorCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "fusion_reactor", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    private static final int WIDTH = 170;
    private static final int HEIGHT = 112;
    private static final int GRID_Y = 12;
    private static final int LAYER_STEP = 58;
    private static final int IO_Y = 84;

    public FusionReactorCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.fusion_reactor"),
                guiHelper.createDrawableItemLike(ModItems.REACTOR_CONTROLLER.get()), WIDTH, HEIGHT);
    }

    /** What the example reactor has at (x, z) on a layer (0 bottom .. 2 top); z = 2 is the controller's face. */
    private static @Nullable ItemLike block(int layer, int x, int z) {
        boolean centre = x == 1 && z == 1;
        boolean edge = (x == 1) != (z == 1); // the middle of a side of the grid
        if (layer == 1) {
            if (centre) {
                return null; // the core
            }
            if (edge) {
                return x == 1 && z == 2 ? ModItems.REACTOR_CONTROLLER.get() : ModItems.REACTOR_GLASS.get();
            }
            return ModItems.REACTOR_CASING.get();
        }
        if (centre) {
            return layer == 0 ? ModItems.REACTOR_PORT.get() : ModItems.REACTOR_GLASS.get();
        }
        return ModItems.REACTOR_CASING.get();
    }

    /** What may go at a spot: face centres take casing, glass or ports; edges and corners casing or ports. */
    private static Component rule(int layer, int x, int z) {
        int centred = (x == 1 ? 1 : 0) + (layer == 1 ? 1 : 0) + (z == 1 ? 1 : 0);
        if (layer == 1 && x == 1 && z == 2) {
            return Component.translatable("jei.comminutioncraft.fusion_reactor.controller");
        }
        return Component.translatable(centred == 2 ? "jei.comminutioncraft.fusion_reactor.face_centre"
                : "jei.comminutioncraft.fusion_reactor.frame");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Structure recipe, IFocusGroup focuses) {
        for (int layer = 0; layer < 3; layer++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    ItemLike block = block(layer, x, z);
                    int sx = 1 + layer * LAYER_STEP + x * 18;
                    int sy = GRID_Y + 1 + z * 18;
                    if (block == null) {
                        continue; // the core stays empty (drawn as a gap)
                    }
                    Component rule = rule(layer, x, z);
                    builder.addSlot(RecipeIngredientRole.INPUT, sx, sy)
                            .setStandardSlotBackground()
                            .add(block)
                            .addRichTooltipCallback((view, tooltip) -> tooltip.add(rule.copy().withStyle(ChatFormatting.GRAY)));
                }
            }
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 1, IO_Y + 1)
                .setStandardSlotBackground()
                .add(ModItems.FUSION_FUEL_PELLET.get());
        builder.addSlot(RecipeIngredientRole.INPUT, 21, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(1_000, false, 16, 16)
                .addFluidStack(ModFluids.PLASMA.get(), 1_000)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.fusion_reactor.ignite").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 19, IO_Y + 1)
                .setOutputSlotBackground()
                .setFluidRenderer(FusionReactorBlockEntity.TRITIUM_PER_PELLET.get(), false, 16, 16)
                .addFluidStack(ModFluids.TRITIUM.get(), FusionReactorBlockEntity.TRITIUM_PER_PELLET.get())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.fusion_reactor.tritium").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Structure recipe, IFocusGroup focuses) {
        String[] layers = {"bottom", "middle", "top"};
        for (int layer = 0; layer < 3; layer++) {
            builder.addText(Component.translatable("jei.comminutioncraft.fusion_reactor." + layers[layer]), 54, 10)
                    .setPosition(layer * LAYER_STEP, 0)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFF808080);
        }
        builder.addText(Component.translatable("jei.comminutioncraft.fusion_reactor.core"), 54, 10)
                .setPosition(LAYER_STEP, GRID_Y + 19 + 4)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF60FF80);
        builder.addText(Component.translatable("jei.comminutioncraft.fusion_reactor.output",
                        String.format(java.util.Locale.ROOT, "%,d", FusionReactorBlockEntity.ENERGY_PER_TICK.get())), WIDTH - 62, 10)
                .setPosition(42, IO_Y + 5)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        JeiAmounts.label(builder, 21, IO_Y + 1, 1_000);
        JeiAmounts.label(builder, WIDTH - 19, IO_Y + 1, FusionReactorBlockEntity.TRITIUM_PER_PELLET.get());
        builder.addText(Component.translatable("jei.comminutioncraft.fusion_reactor.hint"), WIDTH, 10)
                .setPosition(0, HEIGHT - 10)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF606060);
    }
}
