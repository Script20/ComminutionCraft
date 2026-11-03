package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.common.recipe.ItemProcessingRecipe;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
 * One item in -> a stack out (granulating, assembling, washing). Times and costs come from the machines, so every
 * recipe in a category shows the same line: each machine's time, plus any extra cost (plasma, water) in the tooltip.
 */
public class ProcessingCategory<R extends ItemProcessingRecipe> extends AbstractRecipeCategory<RecipeHolder<R>> {
    private static final int WIDTH = 82;
    private static final int HEIGHT = 38;

    /** A machine that runs this category's recipes: its time line's translation key and its process time. */
    public record Station(String timeKey, int ticks) {}

    private final List<Station> stations;
    private final List<Component> notes;

    public ProcessingCategory(IGuiHelper guiHelper, IRecipeHolderType<R> type, Component title, ItemLike icon,
                              List<Station> stations, List<Component> notes) {
        super(type, title, guiHelper.createDrawableItemLike(icon), WIDTH, HEIGHT);
        this.stations = stations;
        this.notes = notes;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 5)
                .setStandardSlotBackground()
                .add(recipe.value().getIngredient());
        builder.addOutputSlot(61, 5)
                .setOutputSlotBackground()
                .add(recipe.value().getResult());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<R> recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(stations.getFirst().ticks()).setPosition(26, 5);
        List<Component> tooltip = new ArrayList<>();
        for (Station station : stations) {
            tooltip.add(Component.translatable(station.timeKey(), seconds(station.ticks())));
        }
        tooltip.addAll(notes);
        String times = stations.stream().map(station -> seconds(station.ticks())).collect(Collectors.joining(" · "));
        builder.addText(Component.literal(times), WIDTH, 10)
                .setPosition(0, 28)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setTooltip(tooltip);
    }

    /** 150 -> "7.5s", 75 -> "3.75s" */
    private static String seconds(int ticks) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(ticks / 20.0) + "s";
    }
}
