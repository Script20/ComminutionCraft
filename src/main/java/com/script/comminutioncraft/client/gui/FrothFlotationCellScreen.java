package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FrothFlotationCellMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Froth Flotation Cell GUI: the FE machine screen plus water (x 141) and oxygen (x 155) gauges on the right. */
public class FrothFlotationCellScreen extends ElectricMachineScreen {
    private static final int WATER_X = 141;

    private final FrothFlotationCellMenu cell;

    public FrothFlotationCellScreen(FrothFlotationCellMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/froth_flotation_cell.png"), Tier.ANTIMATTER);
        this.cell = menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawGauge(graphics, WATER_X, WATER_U, cell.getWater(), cell.getWaterCapacity());
        drawGauge(graphics, PLASMA_X, OXYGEN_U, cell.getOxygen(), cell.getOxygenCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        gaugeTooltip(graphics, mouseX, mouseY, WATER_X, "gui.comminutioncraft.water", cell.getWater(), cell.getWaterCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.oxygen", cell.getOxygen(), cell.getOxygenCapacity());
    }
}
