package com.script.comminutioncraft.client;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.machine.ElectricMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GUI for tier-2 processing machines: energy bar, input, progress arrow, output. */
public class ElectricMachineScreen extends MachineScreen<ElectricMachineMenu> {
    public ElectricMachineScreen(ElectricMachineMenu menu, Inventory inventory, Component title, Identifier texture) {
        super(menu, inventory, title, texture, Tier.ELECTRIC);
    }

    public static ElectricMachineScreen granulator(ElectricMachineMenu menu, Inventory inventory, Component title) {
        return new ElectricMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/electric_granulator.png"));
    }

    public static ElectricMachineScreen smelter(ElectricMachineMenu menu, Inventory inventory, Component title) {
        return new ElectricMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/electric_smelter.png"));
    }

    @Override
    protected boolean isActive() {
        return menu.isWorking();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawArrow(graphics, 79, 34, menu.getProgress());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
    }
}
