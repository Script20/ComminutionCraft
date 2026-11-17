package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.multiblock.MultiblockStructure;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/**
 * The Event Horizon Reactor's page (press U on its controller, frame or port): the 7x7x7 cage as a 3D model with
 * layer buttons and the blocks needed (see {@link MultiblockCategory}), then any item + antimatter to ignite -> FE.
 */
public class EventHorizonCategory extends MultiblockCategory<EventHorizonCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "event_horizon", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    /** [any item][antimatter] -> [FE], centred. */
    private static final int IO_X = (WIDTH - (18 + 18 + 4 + 24 + 4 + 18)) / 2;
    private static final int ITEM_X = IO_X + 1;
    private static final int ANTIMATTER_X = IO_X + 19;
    private static final int ARROW_X = IO_X + 36 + 4;
    private static final int ENERGY_X = ARROW_X + 24 + 4 + 1;

    private final IGuiHelper guiHelper;
    private final IDrawable energyIcon;

    public EventHorizonCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.event_horizon"),
                guiHelper.createDrawableItemLike(ModItems.HORIZON_CONTROLLER.get()), MultiblockStructure.EVENT_HORIZON.get());
        this.guiHelper = guiHelper;
        energyIcon = energyIcon(guiHelper);
    }


    @Override
    protected void addIoSlots(IRecipeLayoutBuilder builder) {
        builder.addSlot(RecipeIngredientRole.INPUT, ITEM_X, IO_Y + 1)
                .setStandardSlotBackground()
                .add(Items.COBBLESTONE)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.event_horizon.any_item").withStyle(ChatFormatting.GRAY)));
        int ignition = EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get();
        builder.addSlot(RecipeIngredientRole.INPUT, ANTIMATTER_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(ignition, false, 16, 16)
                .add(ModFluids.ANTIMATTER.get(), ignition)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.event_horizon.ignite").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void addIoExtras(IRecipeExtrasBuilder builder) {
        builder.addRecipeArrowWidget().setPosition(ARROW_X, IO_Y + 1);
        JeiAmounts.label(builder, ANTIMATTER_X, IO_Y + 1, EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get());
        builder.addDrawableWidget(guiHelper.getSlotDrawable()).setPosition(ENERGY_X - 1, IO_Y);
        builder.addDrawableWidget(energyIcon).setPosition(ENERGY_X, IO_Y + 1);
        int perMass = EventHorizonReactorBlockEntity.ENERGY_PER_MASS.get();
        builder.addTooltipArea(ENERGY_X, IO_Y + 1, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_out").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.event_horizon.per_mass", String.format(Locale.ROOT, "%,d", perMass))
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("jei.comminutioncraft.event_horizon.max",
                        String.format(Locale.ROOT, "%,d", perMass * EventHorizonReactorBlockEntity.MASS_PER_TICK.get()))
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("jei.comminutioncraft.event_horizon.inside").withStyle(ChatFormatting.DARK_GRAY)));
    }
}
