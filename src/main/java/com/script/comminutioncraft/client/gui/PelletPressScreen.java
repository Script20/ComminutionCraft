package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.PelletPressMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Pellet Press GUI: the FE machine screen plus deuterium (x 141) and tritium (x 155) gauges on the right. */
public class PelletPressScreen extends ElectricMachineScreen {
    private static final int DEUTERIUM_X = 141;

    private final PelletPressMenu press;

    public PelletPressScreen(PelletPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/pellet_press.png"), Tier.FUSION);
        this.press = menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawGauge(graphics, DEUTERIUM_X, DEUTERIUM_U, press.getDeuterium(), press.getDeuteriumCapacity());
        drawGauge(graphics, PLASMA_X, TRITIUM_U, press.getTritium(), press.getTritiumCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        gaugeTooltip(graphics, mouseX, mouseY, DEUTERIUM_X, "gui.comminutioncraft.deuterium", press.getDeuterium(), press.getDeuteriumCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.tritium", press.getTritium(), press.getTritiumCapacity());
    }
}
