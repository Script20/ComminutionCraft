package com.script.comminutioncraft.client.compat.jei;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
import com.script.comminutioncraft.common.registry.ModFluids;
import com.script.comminutioncraft.common.registry.ModItems;
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
    private static final int INFO_X = 96;
    private static final int IO_Y = 2;

    public ParticleAcceleratorCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("jei.comminutioncraft.particle_accelerator"),
                guiHelper.createDrawableItemLike(ModItems.ACCELERATOR_CONTROLLER.get()), WIDTH, HEIGHT);
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
        builder.addSlot(RecipeIngredientRole.INPUT, INFO_X + 1, IO_Y + 1)
                .setStandardSlotBackground()
                .setFluidRenderer(hydrogen, false, 16, 16)
                .addFluidStack(ModFluids.HYDROGEN.get(), hydrogen)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.comminutioncraft.particle_accelerator.hydrogen").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 17, IO_Y + 1)
                .setOutputSlotBackground()
                .setFluidRenderer(1, false, 16, 16)
                .addFluidStack(ModFluids.ANTIMATTER.get(), 1);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Structure recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(INFO_X + 24, IO_Y + 1);
        JeiAmounts.label(builder, INFO_X + 1, IO_Y + 1, ParticleAcceleratorBlockEntity.HYDROGEN_PER_MB.get());
        JeiAmounts.label(builder, WIDTH - 17, IO_Y + 1, 1);
        int blocks = ParticleAcceleratorBlockEntity.ringBlocks(7);
        String fe = String.format(Locale.ROOT, "%,d", blocks * ParticleAcceleratorBlockEntity.ENERGY_PER_RING_BLOCK.get());
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
