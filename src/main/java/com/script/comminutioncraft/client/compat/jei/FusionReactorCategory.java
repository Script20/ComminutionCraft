package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
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
import net.minecraft.world.level.ItemLike;

/**
 * The Fusion Reactor's page (press U on its controller or any reactor block): the 3x3x3 as a 3D model with layer
 * buttons and the blocks needed (see {@link MultiblockCategory}), then pellets + plasma to ignite -> FE + tritium.
 */
public class FusionReactorCategory extends MultiblockCategory<FusionReactorCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "fusion_reactor", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    /** [pellet][plasma] -> [FE][tritium], centred. */
    private static final int IO_X = (WIDTH - (18 + 18 + 4 + 24 + 4 + 18 + 18)) / 2;
    private static final int PELLET_X = IO_X + 1;
    private static final int PLASMA_X = IO_X + 19;
    private static final int ARROW_X = IO_X + 36 + 4;
    private static final int ENERGY_X = ARROW_X + 24 + 4 + 1;
    private static final int TRITIUM_X = ENERGY_X + 18;

    private final IGuiHelper guiHelper;
    private final IDrawable energyIcon;

    public FusionReactorCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.fusion_reactor"),
                guiHelper.createDrawableItemLike(ModItems.REACTOR_CONTROLLER.get()), 3, 3, 3, blocks());
        this.guiHelper = guiHelper;
        energyIcon = energyIcon(guiHelper);
        rule(ModItems.REACTOR_CASING.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.frame"));
        rule(ModItems.REACTOR_GLASS.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.face_centre"));
        rule(ModItems.REACTOR_PORT.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.port"));
        rule(ModItems.REACTOR_CONTROLLER.get(), Component.translatable("jei.comminutioncraft.fusion_reactor.controller"));
    }

    /** The example reactor: casing frame, glass in the face centres, a port under the core, the controller in front. */
    private static Map<BlockPos, ItemLike> blocks() {
        Map<BlockPos, ItemLike> blocks = structure();
        for (int y = 0; y < 3; y++) {
            for (int z = 0; z < 3; z++) {
                for (int x = 0; x < 3; x++) {
                    int centred = (x == 1 ? 1 : 0) + (y == 1 ? 1 : 0) + (z == 1 ? 1 : 0);
                    if (centred == 3) {
                        continue; // the core stays empty
                    }
                    ItemLike block;
                    if (x == 1 && y == 1 && z == 2) {
                        block = ModItems.REACTOR_CONTROLLER.get();
                    } else if (x == 1 && y == 0 && z == 1) {
                        block = ModItems.REACTOR_PORT.get();
                    } else if (centred == 2) {
                        block = ModItems.REACTOR_GLASS.get();
                    } else {
                        block = ModItems.REACTOR_CASING.get();
                    }
                    blocks.put(new BlockPos(x, y, z), block);
                }
            }
        }
        return blocks;
    }

    @Override
    protected void addIoSlots(IRecipeLayoutBuilder builder) {
        builder.addSlot(RecipeIngredientRole.INPUT, PELLET_X, IO_Y + 1)
                .setStandardSlotBackground()
                .add(ModItems.FUSION_FUEL_PELLET.get());
        builder.addSlot(RecipeIngredientRole.INPUT, PLASMA_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(1_000, false, 16, 16)
                .add(ModFluids.PLASMA.get(), 1_000)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.fusion_reactor.ignite").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, TRITIUM_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(FusionReactorBlockEntity.TRITIUM_PER_PELLET.get(), false, 16, 16)
                .add(ModFluids.TRITIUM.get(), FusionReactorBlockEntity.TRITIUM_PER_PELLET.get())
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.fusion_reactor.tritium").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void addIoExtras(IRecipeExtrasBuilder builder) {
        builder.addRecipeArrowWidget().setPosition(ARROW_X, IO_Y + 1);
        builder.addDrawableWidget(guiHelper.getSlotDrawable()).setPosition(ENERGY_X - 1, IO_Y);
        builder.addDrawableWidget(energyIcon).setPosition(ENERGY_X, IO_Y + 1);
        builder.addTooltipArea(ENERGY_X, IO_Y + 1, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_out").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.fusion_reactor.output",
                        String.format(Locale.ROOT, "%,d", FusionReactorBlockEntity.ENERGY_PER_TICK.get())).withStyle(ChatFormatting.GRAY)));
        JeiAmounts.label(builder, PLASMA_X, IO_Y + 1, 1_000);
        JeiAmounts.label(builder, TRITIUM_X, IO_Y + 1, FusionReactorBlockEntity.TRITIUM_PER_PELLET.get());
    }
}
