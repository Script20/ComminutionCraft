package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

/**
 * A fluid machine's page (Isotope Separator, Tritium Breeder, Electrolyser, Plasma Generator, MHD Generator,
 * Atmospheric Water Generator, Annihilation Generator): what goes in and comes out per step, as fluid slots with their
 * amounts, centred, and one or two lines about the cost or rate under them. Generators show a red FE slot as their
 * output. One category per machine, so U on a machine shows just its own page.
 */
public class FluidConversionCategory extends AbstractRecipeCategory<FluidConversionCategory.Conversion> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 48;
    private static final int SLOT_Y = 5;
    private static final int ARROW_GAP = 4;
    private static final int ARROW_W = 24;
    private static final int TEXT_Y = 27;
    private static final int TEXT_COLOR = 0xFF808080;

    /** A fluid and how much of it (mB), with an optional tooltip line (e.g. "out of By-product faces"). */
    public record Amount(Fluid fluid, int millibuckets, @Nullable Component note) {
        public Amount(Fluid fluid, int millibuckets) {
            this(fluid, millibuckets, null);
        }
    }

    /** One step of the machine: fluids in, fluids out, a line or two about the cost or rate, and whether it makes FE. */
    public record Conversion(List<Amount> in, List<Amount> out, List<Component> lines, boolean makesEnergy) {
        public Conversion(List<Amount> in, List<Amount> out, Component line) {
            this(in, out, List.of(line), false);
        }
    }

    private final IDrawable energyIcon;

    public FluidConversionCategory(IGuiHelper guiHelper, IRecipeType<Conversion> type, ItemLike machine) {
        super(type, new ItemStack(machine).getHoverName(), guiHelper.createDrawableItemLike(machine), WIDTH, HEIGHT);
        energyIcon = guiHelper.drawableBuilder(ComminutionCraft.id("textures/gui/jei_energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    // ---- Layout: [inputs] -> [outputs or FE], centred ----

    private static int outputCount(Conversion recipe) {
        return recipe.out().size() + (recipe.makesEnergy() ? 1 : 0);
    }

    private static int inputX(Conversion recipe, int i) {
        int width = recipe.in().size() * 18 + ARROW_GAP + ARROW_W + ARROW_GAP + outputCount(recipe) * 18;
        return (WIDTH - width) / 2 + 1 + i * 18;
    }

    private static int arrowX(Conversion recipe) {
        return inputX(recipe, recipe.in().size()) - 1 + ARROW_GAP;
    }

    private static int outputX(Conversion recipe, int i) {
        return arrowX(recipe) + ARROW_W + ARROW_GAP + 1 + i * 18;
    }

    private static void slot(IRecipeSlotBuilder slot, Amount amount) {
        slot.setStandardSlotBackground()
                .setFluidRenderer(amount.millibuckets(), false, 16, 16)
                .add(amount.fluid(), amount.millibuckets());
        if (amount.note() != null) {
            Component note = amount.note();
            slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(note.copy().withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Conversion recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.in().size(); i++) {
            slot(builder.addSlot(RecipeIngredientRole.INPUT, inputX(recipe, i), SLOT_Y), recipe.in().get(i));
        }
        for (int i = 0; i < recipe.out().size(); i++) {
            slot(builder.addSlot(RecipeIngredientRole.OUTPUT, outputX(recipe, i), SLOT_Y), recipe.out().get(i));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Conversion recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(arrowX(recipe), SLOT_Y);
        for (int i = 0; i < recipe.in().size(); i++) {
            JeiAmounts.label(builder, inputX(recipe, i), SLOT_Y, recipe.in().get(i).millibuckets());
        }
        for (int i = 0; i < recipe.out().size(); i++) {
            JeiAmounts.label(builder, outputX(recipe, i), SLOT_Y, recipe.out().get(i).millibuckets());
        }
        if (recipe.makesEnergy()) {
            int x = outputX(recipe, recipe.out().size());
            builder.addDrawableWidget(energyIcon).setPosition(x, SLOT_Y);
            List<FormattedText> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("jei.comminutioncraft.energy_out").withStyle(ChatFormatting.RED));
            recipe.lines().forEach(line -> tooltip.add(line.copy().withStyle(ChatFormatting.GRAY)));
            builder.addTooltipArea(x, SLOT_Y, 16, 16).setTooltip(tooltip);
        }
        for (int i = 0; i < recipe.lines().size(); i++) {
            builder.addText(recipe.lines().get(i), WIDTH, 10)
                    .setPosition(0, TEXT_Y + i * 10)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(TEXT_COLOR);
        }
    }
}
