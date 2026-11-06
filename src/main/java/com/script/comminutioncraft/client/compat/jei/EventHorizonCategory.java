package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * How to build the Event Horizon Reactor (press U on its controller, frame or port): the bottom (= top) layer of the
 * 7x7x7 seen from above - frame round the edge, the controller in the middle of the front edge, an Input and an
 * Output port - two lines about the middle layers and the inside, then any item + antimatter -> FE.
 */
public class EventHorizonCategory extends AbstractRecipeCategory<EventHorizonCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "event_horizon", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    private static final int N = 7;
    private static final int WIDTH = 170;
    private static final int GRID_X = (WIDTH - N * 18) / 2;
    private static final int GRID_Y = 11;
    private static final int TEXT_Y = GRID_Y + N * 18 + 3;
    private static final int IO_Y = TEXT_Y + 22;
    private static final int HEIGHT = IO_Y + 19;
    private static final int IO_X = (WIDTH - (18 + 18 + 4 + 24 + 4 + 18)) / 2;
    private static final int ITEM_X = IO_X + 1;
    private static final int ANTIMATTER_X = IO_X + 19;
    private static final int ARROW_X = IO_X + 36 + 4;
    private static final int ENERGY_X = ARROW_X + 24 + 4 + 1;

    private final IGuiHelper guiHelper;
    private final IDrawable energyIcon;

    public EventHorizonCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.event_horizon"),
                guiHelper.createDrawableItemLike(ModItems.HORIZON_CONTROLLER.get()), WIDTH, HEIGHT);
        this.guiHelper = guiHelper;
        energyIcon = guiHelper.drawableBuilder(ComminutionCraft.id("textures/gui/jei_energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Structure recipe, IFocusGroup focuses) {
        int mid = N / 2;
        for (int z = 0; z < N; z++) {
            for (int x = 0; x < N; x++) {
                if (x != 0 && x != N - 1 && z != 0 && z != N - 1) {
                    continue; // the bottom face's opening: open, or anything
                }
                ItemLike block;
                String rule;
                if (x == mid && z == N - 1) {
                    block = ModItems.HORIZON_CONTROLLER.get();
                    rule = "controller";
                } else if (x == N - 1 && z == mid) {
                    block = ModItems.HORIZON_PORT.get();
                    rule = "input_port";
                } else if (x == 0 && z == mid) {
                    block = ModItems.HORIZON_PORT.get();
                    rule = "output_port";
                } else {
                    block = ModItems.CONTAINMENT_FRAME.get();
                    rule = "frame";
                }
                Component tip = Component.translatable("jei.comminutioncraft.event_horizon." + rule);
                builder.addSlot(RecipeIngredientRole.INPUT, GRID_X + 1 + x * 18, GRID_Y + 1 + z * 18)
                        .setStandardSlotBackground()
                        .add(block)
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(tip.copy().withStyle(ChatFormatting.GRAY)));
            }
        }
        builder.addSlot(RecipeIngredientRole.INPUT, ITEM_X, IO_Y + 1)
                .setStandardSlotBackground()
                .add(Items.COBBLESTONE)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.event_horizon.any_item").withStyle(ChatFormatting.GRAY)));
        int ignition = EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get();
        builder.addSlot(RecipeIngredientRole.INPUT, ANTIMATTER_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(ignition, false, 16, 16)
                .addFluidStack(ModFluids.ANTIMATTER.get(), ignition)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.event_horizon.ignite").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Structure recipe, IFocusGroup focuses) {
        builder.addText(Component.translatable("jei.comminutioncraft.event_horizon.layer"), WIDTH, 10)
                .setPosition(0, 0)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        String[] lines = {"middle", "inside"};
        for (int i = 0; i < lines.length; i++) {
            builder.addText(Component.translatable("jei.comminutioncraft.event_horizon." + lines[i]), WIDTH, 10)
                    .setPosition(0, TEXT_Y + i * 10)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFF808080);
        }
        builder.addRecipeArrow().setPosition(ARROW_X, IO_Y + 1);
        JeiAmounts.label(builder, ANTIMATTER_X, IO_Y + 1, EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get());
        builder.addDrawable(guiHelper.getSlotDrawable(), ENERGY_X - 1, IO_Y);
        builder.addDrawable(energyIcon, ENERGY_X, IO_Y + 1);
        int perMass = EventHorizonReactorBlockEntity.ENERGY_PER_MASS.get();
        builder.addTooltipArea(ENERGY_X, IO_Y + 1, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_out").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.event_horizon.energy", String.format(Locale.ROOT, "%,d", perMass),
                        String.format(Locale.ROOT, "%,d", perMass * EventHorizonReactorBlockEntity.MASS_PER_TICK.get()))
                        .withStyle(ChatFormatting.GRAY)));
    }
}
