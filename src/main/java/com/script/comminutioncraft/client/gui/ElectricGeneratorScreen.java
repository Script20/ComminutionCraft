package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GUI for the Electric Generator: energy bar, flame over the fuel slot, and the output rate. */
public class ElectricGeneratorScreen extends MachineScreen<ElectricGeneratorMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/electric_generator.png");
    private static final int RATE_COLOR = 0xFF9EA4B0;

    public ElectricGeneratorScreen(ElectricGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.ELECTRIC);
    }

    @Override
    protected boolean isActive() {
        return menu.isLit();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        if (menu.isLit()) {
            drawFlame(graphics, 81, 34, menu.getLitProgress());
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Component rate = Component.translatable("gui.comminutioncraft.generating",
                menu.isLit() ? ElectricGeneratorBlockEntity.ENERGY_PER_TICK.get() : 0);
        graphics.text(font, rate, 104, 39, RATE_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
    }
}
