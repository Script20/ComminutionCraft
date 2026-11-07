package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.SingularityProcessorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Singularity Processor GUI: the FE machine screen with an output and a by-products slot, and a warning without a black hole. */
public class SingularityProcessorScreen extends ElectricMachineScreen {
    private static final int WARNING_COLOR = 0xFFFFB04A;

    private final SingularityProcessorMenu processor;

    public SingularityProcessorScreen(SingularityProcessorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/singularity_processor.png"), Tier.SINGULARITY);
        this.processor = menu;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (!processor.isNearHole()) {
            graphics.text(font, Component.translatable("gui.comminutioncraft.processor.no_hole"), 28, 62, WARNING_COLOR, false);
        }
    }
}
