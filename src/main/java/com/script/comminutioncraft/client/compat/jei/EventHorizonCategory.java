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
    private static final int GRID_X = 0;
    private static final int GRID_Y = 0;
    private static final int HEIGHT = N * 18;
    /** Right of the grid: [item][antimatter], a down arrow, [FE], and a [?] with the building notes. */
    private static final int COLUMN_X = N * 18 + 4;
    private static final int COLUMN_W = WIDTH - COLUMN_X;
    private static final int ITEM_X = COLUMN_X + (COLUMN_W - 36) / 2 + 1;
    private static final int ANTIMATTER_X = ITEM_X + 18;
    private static final int IO_Y = 24;
    private static final int ENERGY_X = COLUMN_X + (COLUMN_W - 18) / 2 + 1;
    private static final int ENERGY_Y = IO_Y + 36;

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
        JeiAmounts.label(builder, ANTIMATTER_X, IO_Y + 1, EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get());
        builder.addText(Component.literal("↓"), COLUMN_W, 10)
                .setPosition(COLUMN_X, IO_Y + 22)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        builder.addDrawable(guiHelper.getSlotDrawable(), ENERGY_X - 1, ENERGY_Y - 1);
        builder.addDrawable(energyIcon, ENERGY_X, ENERGY_Y);
        // How to build it, on hover (the grid itself shows the bottom / top layer)
        builder.addText(Component.literal("[?]"), COLUMN_W, 10)
                .setPosition(COLUMN_X, HEIGHT - 12)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFFB08A30)
                .setTooltip(List.of(
                        Component.translatable("jei.comminutioncraft.event_horizon.layer").withStyle(ChatFormatting.GOLD),
                        Component.translatable("jei.comminutioncraft.event_horizon.middle").withStyle(ChatFormatting.GRAY),
                        Component.translatable("jei.comminutioncraft.event_horizon.inside").withStyle(ChatFormatting.GRAY),
                        Component.translatable("jei.comminutioncraft.event_horizon.sides").withStyle(ChatFormatting.GRAY)));
        int perMass = EventHorizonReactorBlockEntity.ENERGY_PER_MASS.get();
        builder.addTooltipArea(ENERGY_X, ENERGY_Y, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_out").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.event_horizon.per_mass", String.format(Locale.ROOT, "%,d", perMass))
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("jei.comminutioncraft.event_horizon.max",
                        String.format(Locale.ROOT, "%,d", perMass * EventHorizonReactorBlockEntity.MASS_PER_TICK.get()))
                        .withStyle(ChatFormatting.GRAY)));
    }
}
