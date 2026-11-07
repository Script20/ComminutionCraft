package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * The Event Horizon Reactor's page (press U on its controller, frame or port): the 7x7x7 cage as a 3D model with
 * layer buttons and the blocks needed (see {@link MultiblockCategory}), then any item + antimatter to ignite -> FE.
 */
public class EventHorizonCategory extends MultiblockCategory<EventHorizonCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "event_horizon", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    private static final int N = 7;
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
                guiHelper.createDrawableItemLike(ModItems.HORIZON_CONTROLLER.get()), N, N, N, blocks());
        this.guiHelper = guiHelper;
        energyIcon = energyIcon(guiHelper);
        rule(ModItems.CONTAINMENT_FRAME.get(), Component.translatable("jei.comminutioncraft.event_horizon.frame"));
        rule(ModItems.HORIZON_PORT.get(), Component.translatable("jei.comminutioncraft.multiblock.port_rule"));
        rule(ModItems.HORIZON_CONTROLLER.get(), Component.translatable("jei.comminutioncraft.event_horizon.controller"));
    }

    /** The 7x7x7 cage: frame on every edge, the controller in the middle of the front bottom edge, two ports. */
    private static Map<BlockPos, ItemLike> blocks() {
        Map<BlockPos, ItemLike> blocks = structure();
        int mid = N / 2;
        for (int y = 0; y < N; y++) {
            for (int z = 0; z < N; z++) {
                for (int x = 0; x < N; x++) {
                    int outer = (x == 0 || x == N - 1 ? 1 : 0) + (y == 0 || y == N - 1 ? 1 : 0) + (z == 0 || z == N - 1 ? 1 : 0);
                    if (outer < 2) {
                        continue; // the inside and the faces' openings
                    }
                    ItemLike block = x == mid && y == 0 && z == N - 1 ? ModItems.HORIZON_CONTROLLER.get()
                            : (x == N - 1 || x == 0) && y == 0 && z == mid ? ModItems.HORIZON_PORT.get()
                            : ModItems.CONTAINMENT_FRAME.get();
                    blocks.put(new BlockPos(x, y, z), block);
                }
            }
        }
        return blocks;
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
                .addFluidStack(ModFluids.ANTIMATTER.get(), ignition)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.event_horizon.ignite").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void addIoExtras(IRecipeExtrasBuilder builder) {
        builder.addRecipeArrow().setPosition(ARROW_X, IO_Y + 1);
        JeiAmounts.label(builder, ANTIMATTER_X, IO_Y + 1, EventHorizonReactorBlockEntity.IGNITION_ANTIMATTER.get());
        builder.addDrawable(guiHelper.getSlotDrawable(), ENERGY_X - 1, IO_Y);
        builder.addDrawable(energyIcon, ENERGY_X, IO_Y + 1);
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
