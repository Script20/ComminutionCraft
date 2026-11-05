package com.script.comminutioncraft.client.compat.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

/**
 * A fluid machine's page (Isotope Separator, Tritium Breeder, Electrolyser, Plasma Generator, MHD Generator,
 * Atmospheric Water Generator): what goes in and comes out per step, as fluid slots with their amounts, and the cost
 * or rate under it. One category per machine, so U on a machine shows just its own page.
 */
public class FluidConversionCategory extends AbstractRecipeCategory<FluidConversionCategory.Conversion> {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int OUTPUT_X = 66;

    /** A fluid and how much of it (mB), with an optional tooltip line (e.g. "out of By-product faces"). */
    public record Amount(Fluid fluid, int millibuckets, @Nullable Component note) {
        public Amount(Fluid fluid, int millibuckets) {
            this(fluid, millibuckets, null);
        }
    }

    /** One step of the machine: fluids in, fluids out, and a line about the cost or rate. */
    public record Conversion(List<Amount> in, List<Amount> out, Component line) {}

    public FluidConversionCategory(IGuiHelper guiHelper, IRecipeType<Conversion> type, ItemLike machine) {
        super(type, new net.minecraft.world.item.ItemStack(machine).getHoverName(), guiHelper.createDrawableItemLike(machine), WIDTH, HEIGHT);
    }

    private static void slot(IRecipeSlotBuilder slot, Amount amount) {
        slot.setStandardSlotBackground()
                .setFluidRenderer(amount.millibuckets(), false, 16, 16)
                .addFluidStack(amount.fluid(), amount.millibuckets());
        if (amount.note() != null) {
            Component note = amount.note();
            slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(note.copy().withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Conversion recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.in().size(); i++) {
            slot(builder.addSlot(RecipeIngredientRole.INPUT, 1 + i * 18, 5), recipe.in().get(i));
        }
        for (int i = 0; i < recipe.out().size(); i++) {
            slot(builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + i * 18, 5), recipe.out().get(i));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Conversion recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(38, 5);
        for (int i = 0; i < recipe.in().size(); i++) {
            JeiAmounts.label(builder, 1 + i * 18, 5, recipe.in().get(i).millibuckets());
        }
        for (int i = 0; i < recipe.out().size(); i++) {
            JeiAmounts.label(builder, OUTPUT_X + i * 18, 5, recipe.out().get(i).millibuckets());
        }
        builder.addText(recipe.line(), WIDTH, 10)
                .setPosition(0, 28)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
