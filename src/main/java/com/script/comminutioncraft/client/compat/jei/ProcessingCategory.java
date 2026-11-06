package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.common.recipe.ItemProcessingRecipe;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;
import java.util.stream.Collectors;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

/**
 * One item in -> a stack out (granulating, assembling, washing, ...). Times and costs come from the machines, so every
 * recipe in a category shows the same line: each machine's time, plus any extra cost (plasma, water) in the tooltip.
 * A category can also show a {@link SecondOutput} that only comes some of the time (the Ore Purifier's by-product),
 * and the fluids each operation uses ({@link #withFluids}) as fluid input slots under the item input.
 */
public class ProcessingCategory<R extends ItemProcessingRecipe> extends AbstractRecipeCategory<RecipeHolder<R>> {
    private static final int WIDTH = 82;
    /** Tall enough for a row of fluid slots under the item input (JEI fixes the height per category type). */
    private static final int HEIGHT = 42;

    /** A machine that runs this category's recipes: its time line's translation key and its process time. */
    public record Station(String timeKey, int ticks) {}

    /** A second result each recipe gives only some of the time: what it is and the chance (0-1) of getting it. */
    public record SecondOutput<R>(Function<R, ItemStackTemplate> stack, Function<R, Float> chance) {}

    /** A fluid each operation uses, and how much (mB). */
    public record FluidCost(Fluid fluid, int amount) {}

    private static final int SECOND_OUTPUT_X = 83;
    private static final int FLUID_Y = 24;

    private final List<Station> stations;
    private final List<Component> notes;
    private final Function<R, List<Component>> recipeNotes;
    private final @Nullable SecondOutput<R> secondOutput;
    private List<FluidCost> fluids = List.of();
    private Function<R, List<FluidCost>> recipeFluids = recipe -> List.of();

    public ProcessingCategory(IGuiHelper guiHelper, IRecipeHolderType<R> type, Component title, ItemLike icon,
                              List<Station> stations, List<Component> notes) {
        this(guiHelper, type, title, icon, stations, notes, recipe -> List.of());
    }

    /** {@code recipeNotes} adds tooltip lines for particular recipes (e.g. "Electric or Plasma Granulator only"). */
    public ProcessingCategory(IGuiHelper guiHelper, IRecipeHolderType<R> type, Component title, ItemLike icon,
                              List<Station> stations, List<Component> notes, Function<R, List<Component>> recipeNotes) {
        this(guiHelper, type, title, icon, stations, notes, recipeNotes, null);
    }

    /** With a {@code secondOutput}, the recipe gets a second output slot (to the right) with its chance under it. */
    public ProcessingCategory(IGuiHelper guiHelper, IRecipeHolderType<R> type, Component title, ItemLike icon,
                              List<Station> stations, List<Component> notes, Function<R, List<Component>> recipeNotes,
                              @Nullable SecondOutput<R> secondOutput) {
        super(type, title, guiHelper.createDrawableItemLike(icon), secondOutput == null ? WIDTH : WIDTH + 22, HEIGHT);
        this.stations = stations;
        this.notes = notes;
        this.recipeNotes = recipeNotes;
        this.secondOutput = secondOutput;
    }

    /** Shows these fluids as input slots under the item input (they replace a "Uses X mB" note). */
    public ProcessingCategory<R> withFluids(FluidCost... costs) {
        fluids = List.of(costs);
        return this;
    }

    /** Adds fluid slots that only some recipes use (the Antimatter Circuit's antimatter), after the shared ones. */
    public ProcessingCategory<R> withRecipeFluids(Function<R, List<FluidCost>> costs) {
        recipeFluids = costs;
        return this;
    }

    private List<FluidCost> fluidsFor(R recipe) {
        List<FluidCost> own = recipeFluids.apply(recipe);
        if (own.isEmpty()) {
            return fluids;
        }
        List<FluidCost> all = new ArrayList<>(fluids);
        all.addAll(own);
        return all;
    }

    private static Component chanceText(float chance) {
        return Component.translatable("jei.comminutioncraft.chance", Math.round(chance * 100));
    }

    private static Component shortChanceText(float chance) {
        return Component.literal(Math.round(chance * 100) + "%");
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 5)
                .setStandardSlotBackground()
                .add(recipe.value().getIngredient());
        builder.addOutputSlot(61, 5)
                .setOutputSlotBackground()
                .add(recipe.value().getResult());
        List<FluidCost> fluids = fluidsFor(recipe.value());
        for (int i = 0; i < fluids.size(); i++) {
            FluidCost cost = fluids.get(i);
            builder.addInputSlot(1 + i * 18, FLUID_Y)
                    .setStandardSlotBackground()
                    .setFluidRenderer(cost.amount(), false, 16, 16)
                    .addFluidStack(cost.fluid(), cost.amount());
        }
        if (secondOutput != null) {
            float chance = secondOutput.chance().apply(recipe.value());
            builder.addOutputSlot(SECOND_OUTPUT_X, 5)
                    .setOutputSlotBackground()
                    .add(secondOutput.stack().apply(recipe.value()))
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(chanceText(chance).copy().withStyle(ChatFormatting.GOLD)));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<R> recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(stations.getFirst().ticks()).setPosition(26, 5);
        List<Component> tooltip = new ArrayList<>();
        for (Station station : stations) {
            tooltip.add(Component.translatable(station.timeKey(), seconds(station.ticks())));
        }
        tooltip.addAll(notes);
        tooltip.addAll(recipeNotes.apply(recipe.value()));
        List<FluidCost> fluids = fluidsFor(recipe.value());
        String times = stations.stream().map(station -> seconds(station.ticks())).collect(Collectors.joining(" · "));
        int textX = fluids.size() * 18;  // beside the fluid slots, if there are any
        builder.addText(Component.literal(times), WIDTH - textX, 10)
                .setPosition(textX, fluids.isEmpty() ? 28 : FLUID_Y + 4)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setTooltip(tooltip);
        for (int i = 0; i < fluids.size(); i++) {
            JeiAmounts.label(builder, 1 + i * 18, FLUID_Y, fluids.get(i).amount());
        }
        if (secondOutput != null) {
            builder.addText(shortChanceText(secondOutput.chance().apply(recipe.value())), 18, 10)
                    .setPosition(SECOND_OUTPUT_X, fluids.isEmpty() ? 28 : FLUID_Y + 4)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFFC08A20);
        }
    }

    /** 150 -> "7.5s", 75 -> "3.75s" */
    private static String seconds(int ticks) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(ticks / 20.0) + "s";
    }
}
