package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.PlasmaMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GUI for tier-3 machines that use plasma (the Circuit Assembler): energy bar, input, progress arrow, output, plasma gauge. */
public class PlasmaMachineScreen extends MachineScreen<PlasmaMachineMenu> {
    /** The Circuit Assembler's antimatter tube, left of the plasma one. */
    private static final int ANTIMATTER_X = 141;

    public PlasmaMachineScreen(PlasmaMachineMenu menu, Inventory inventory, Component title, Identifier texture) {
        super(menu, inventory, title, texture, Tier.PLASMA);
    }

    public static PlasmaMachineScreen assembler(PlasmaMachineMenu menu, Inventory inventory, Component title) {
        return new PlasmaMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/circuit_assembler.png"));
    }

    @Override
    protected boolean isActive() {
        return menu.isWorking();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawPlasmaGauge(graphics, menu.getPlasma(), menu.getPlasmaCapacity());
        drawGauge(graphics, ANTIMATTER_X, ANTIMATTER_U, menu.getAntimatter(), menu.getAntimatterCapacity());
        drawArrow(graphics, 79, 34, menu.getProgress());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        plasmaTooltip(graphics, mouseX, mouseY, menu.getPlasma(), menu.getPlasmaCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, ANTIMATTER_X, "gui.comminutioncraft.antimatter", menu.getAntimatter(), menu.getAntimatterCapacity());
    }
}
