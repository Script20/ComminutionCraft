package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.common.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGranulatorBlockEntity;
import com.script.comminutioncraft.common.recipe.GranulatingRecipe;
import com.script.comminutioncraft.common.registry.ModItems;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Raw ore -> granulated ore. Times come from the machines, so every recipe shows the same speeds. */
public class GranulatingCategory extends AbstractRecipeCategory<RecipeHolder<GranulatingRecipe>> {
    private static final int WIDTH = 82;
    private static final int HEIGHT = 38;

    public GranulatingCategory(IGuiHelper guiHelper) {
        super(ComminutionJeiPlugin.GRANULATING.get(), Component.translatable("jei.comminutioncraft.granulating"),
                guiHelper.createDrawableItemLike(ModItems.COAL_GRANULATOR.get()), WIDTH, HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GranulatingRecipe> recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 5)
                .setStandardSlotBackground()
                .add(recipe.value().getIngredient());
        builder.addOutputSlot(61, 5)
                .setOutputSlotBackground()
                .add(recipe.value().getResult());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<GranulatingRecipe> recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrowWidget(CoalGranulatorBlockEntity.PROCESS_TIME).setPosition(26, 5);
        String coal = seconds(CoalGranulatorBlockEntity.PROCESS_TIME);
        String electric = seconds(ElectricGranulatorBlockEntity.PROCESS_TIME);
        String plasma = seconds(PlasmaGranulatorBlockEntity.PROCESS_TIME);
        builder.addText(Component.literal(coal + " · " + electric + " · " + plasma), WIDTH, 10)
                .setPosition(0, 28)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setTooltip(List.of(
                        Component.translatable("jei.comminutioncraft.time.coal_granulator", coal),
                        Component.translatable("jei.comminutioncraft.time.electric_granulator", electric),
                        Component.translatable("jei.comminutioncraft.time.plasma_granulator", plasma)));
    }

    /** 150 -> "7.5s", 75 -> "3.75s" */
    private static String seconds(int ticks) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(ticks / 20.0) + "s";
    }
}
