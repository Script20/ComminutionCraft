package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.multiblock.MultiblockStructure;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
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

/**
 * The Particle Accelerator's page (press U on its controller, a segment or a port): a 7x7 ring as a 3D model with the
 * blocks needed (see {@link MultiblockCategory}), then hydrogen + FE -> antimatter, with the 7x7 numbers on hover.
 */
public class ParticleAcceleratorCategory extends MultiblockCategory<ParticleAcceleratorCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "particle_accelerator", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    private static final int SIDE = 7;
    /** [hydrogen][FE] -> [antimatter], centred. */
    private static final int IO_X = (WIDTH - (18 + 18 + 4 + 24 + 4 + 18)) / 2;
    private static final int HYDROGEN_X = IO_X + 1;
    private static final int ENERGY_X = IO_X + 19;
    private static final int ARROW_X = IO_X + 36 + 4;
    private static final int OUTPUT_X = ARROW_X + 24 + 4 + 1;

    private final IGuiHelper guiHelper;
    private final IDrawable energyIcon;

    public ParticleAcceleratorCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.particle_accelerator"),
                guiHelper.createDrawableItemLike(ModItems.ACCELERATOR_CONTROLLER.get()), MultiblockStructure.PARTICLE_ACCELERATOR.get());
        this.guiHelper = guiHelper;
        energyIcon = energyIcon(guiHelper);
    }


    @Override
    protected void addIoSlots(IRecipeLayoutBuilder builder) {
        int hydrogen = ParticleAcceleratorBlockEntity.HYDROGEN_PER_MB.get();
        builder.addSlot(RecipeIngredientRole.INPUT, HYDROGEN_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(hydrogen, false, 16, 16)
                .add(ModFluids.HYDROGEN.get(), hydrogen)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.particle_accelerator.hydrogen").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(1, false, 16, 16)
                .add(ModFluids.ANTIMATTER.get(), 1);
    }

    @Override
    protected void addIoExtras(IRecipeExtrasBuilder builder) {
        builder.addRecipeArrowWidget().setPosition(ARROW_X, IO_Y + 1);
        JeiAmounts.label(builder, HYDROGEN_X, IO_Y + 1, ParticleAcceleratorBlockEntity.HYDROGEN_PER_MB.get());
        JeiAmounts.label(builder, OUTPUT_X, IO_Y + 1, 1);
        int blocks = ParticleAcceleratorBlockEntity.ringBlocks(SIDE);
        String fe = String.format(Locale.ROOT, "%,d", blocks * ParticleAcceleratorBlockEntity.ENERGY_PER_RING_BLOCK.get());
        String rate = String.format(Locale.ROOT, "%.3f", (float) blocks / Math.max(1, ParticleAcceleratorBlockEntity.RING_TICKS_PER_MB.get()))
                .replaceAll("0+$", "").replaceAll("\\.$", "");
        builder.addDrawableWidget(guiHelper.getSlotDrawable()).setPosition(ENERGY_X - 1, IO_Y);
        builder.addDrawableWidget(energyIcon).setPosition(ENERGY_X, IO_Y + 1);
        builder.addTooltipArea(ENERGY_X, IO_Y + 1, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_in").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.particle_accelerator.example", fe, rate).withStyle(ChatFormatting.GRAY),
                Component.translatable("jei.comminutioncraft.particle_accelerator.bigger").withStyle(ChatFormatting.GRAY)));
    }
}
