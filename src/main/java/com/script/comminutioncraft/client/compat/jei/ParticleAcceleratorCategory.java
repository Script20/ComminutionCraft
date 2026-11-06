package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/**
 * How to build the Particle Accelerator (press U on its controller or any accelerator block): a 5x5 ring seen from
 * above (controller at the bottom, a port on the right), then hydrogen in -> antimatter out and the numbers for a 7x7
 * ring. Every block's tooltip says what may go there.
 */
public class ParticleAcceleratorCategory extends AbstractRecipeCategory<ParticleAcceleratorCategory.Structure> {
    public static final IRecipeType<Structure> TYPE = IRecipeType.create(ComminutionCraft.MODID, "particle_accelerator", Structure.class);

    /** The one "recipe" this category shows. */
    public enum Structure { INSTANCE }

    private static final int SIDE = 5;
    private static final int WIDTH = 170;
    private static final int TEXT_Y = SIDE * 18 + 4;
    private static final int HEIGHT = TEXT_Y + 3 * 10;
    /** Right of the ring: [hydrogen / FE] -> [antimatter], centred in the free area beside it. */
    private static final int RING_W = SIDE * 18;
    private static final int IO_X = RING_W + (WIDTH - RING_W - (18 + 4 + 24 + 4 + 18)) / 2;
    private static final int ARROW_X = IO_X + 18 + 4;
    private static final int OUTPUT_X = ARROW_X + 24 + 4 + 1;
    private static final int HYDROGEN_Y = RING_W / 2 - 18;
    private static final int ENERGY_Y = HYDROGEN_Y + 20;
    private static final int OUTPUT_Y = RING_W / 2 - 8;

    private final IGuiHelper guiHelper;
    private final mezz.jei.api.gui.drawable.IDrawable energyIcon;

    public ParticleAcceleratorCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.particle_accelerator"),
                guiHelper.createDrawableItemLike(ModItems.ACCELERATOR_CONTROLLER.get()), WIDTH, HEIGHT);
        this.guiHelper = guiHelper;
        energyIcon = guiHelper.drawableBuilder(ComminutionCraft.id("textures/gui/jei_energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Structure recipe, IFocusGroup focuses) {
        int mid = SIDE / 2;
        for (int z = 0; z < SIDE; z++) {
            for (int x = 0; x < SIDE; x++) {
                if (x != 0 && x != SIDE - 1 && z != 0 && z != SIDE - 1) {
                    continue; // inside the ring
                }
                ItemLike block;
                String rule;
                if (x == mid && z == SIDE - 1) {
                    block = ModItems.ACCELERATOR_CONTROLLER.get();
                    rule = "controller";
                } else if (x == SIDE - 1 && z == mid) {
                    block = ModItems.ACCELERATOR_PORT.get();
                    rule = "port";
                } else if (x == 0 && z == mid) {
                    block = ModItems.ACCELERATOR_PORT.get();
                    rule = "output_port";
                } else {
                    block = ModItems.ACCELERATOR_SEGMENT.get();
                    rule = "segment";
                }
                Component tip = Component.translatable("jei.comminutioncraft.particle_accelerator." + rule);
                builder.addSlot(RecipeIngredientRole.INPUT, 1 + x * 18, 1 + z * 18)
                        .setStandardSlotBackground()
                        .add(block)
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(tip.copy().withStyle(ChatFormatting.GRAY)));
            }
        }
        int hydrogen = ParticleAcceleratorBlockEntity.HYDROGEN_PER_MB.get();
        builder.addSlot(RecipeIngredientRole.INPUT, IO_X + 1, HYDROGEN_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(hydrogen, false, 16, 16)
                .addFluidStack(ModFluids.HYDROGEN.get(), hydrogen)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.particle_accelerator.hydrogen").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(1, false, 16, 16)
                .addFluidStack(ModFluids.ANTIMATTER.get(), 1);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Structure recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(ARROW_X, OUTPUT_Y);
        JeiAmounts.label(builder, IO_X + 1, HYDROGEN_Y + 1, ParticleAcceleratorBlockEntity.HYDROGEN_PER_MB.get());
        JeiAmounts.label(builder, OUTPUT_X, OUTPUT_Y, 1);
        int blocks = ParticleAcceleratorBlockEntity.ringBlocks(7);
        String fe = String.format(Locale.ROOT, "%,d", blocks * ParticleAcceleratorBlockEntity.ENERGY_PER_RING_BLOCK.get());
        // FE goes in too: the red energy icon under the hydrogen
        builder.addDrawable(guiHelper.getSlotDrawable(), IO_X, ENERGY_Y);
        builder.addDrawable(energyIcon, IO_X + 1, ENERGY_Y + 1);
        builder.addTooltipArea(IO_X + 1, ENERGY_Y + 1, 16, 16).setTooltip(List.of(
                Component.translatable("jei.comminutioncraft.energy_in").withStyle(ChatFormatting.RED),
                Component.translatable("jei.comminutioncraft.particle_accelerator.energy",
                        ParticleAcceleratorBlockEntity.ENERGY_PER_RING_BLOCK.get(), fe).withStyle(ChatFormatting.GRAY)));
        String rate = String.format(Locale.ROOT, "%.3f", (float) blocks / Math.max(1, ParticleAcceleratorBlockEntity.RING_TICKS_PER_MB.get()))
                .replaceAll("0+$", "").replaceAll("\\.$", "");
        Component[] lines = {
                Component.translatable("jei.comminutioncraft.particle_accelerator.sizes",
                        ParticleAcceleratorBlockEntity.MIN_RING_SIDE.get(), ParticleAcceleratorBlockEntity.MAX_RING_SIDE.get()),
                Component.translatable("jei.comminutioncraft.particle_accelerator.example", fe, rate),
                Component.translatable("jei.comminutioncraft.particle_accelerator.bigger")};
        for (int i = 0; i < lines.length; i++) {
            builder.addText(lines[i], WIDTH, 10)
                    .setPosition(0, TEXT_Y + i * 10)
                    .setColor(0xFF808080);
        }
    }
}
