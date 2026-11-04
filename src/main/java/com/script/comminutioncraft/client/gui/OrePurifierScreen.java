package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.OrePurifierMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Ore Purifier GUI: the FE machine screen with an output and a by-product slot, and an oxygen gauge on the right. */
public class OrePurifierScreen extends ElectricMachineScreen {
    private final OrePurifierMenu purifier;

    public OrePurifierScreen(OrePurifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/ore_purifier.png"), Tier.FUSION);
        this.purifier = menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawGauge(graphics, PLASMA_X, OXYGEN_U, purifier.getOxygen(), purifier.getOxygenCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.oxygen", purifier.getOxygen(), purifier.getOxygenCapacity());
    }
}
