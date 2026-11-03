package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the Plasma Generator: energy bar (left), plasma gauge (right), the Air Filter slot, and an air -> plasma
 * strip whose three intake dots light up with the open faces (one dot per two faces) and whose plasma dot lights
 * while it's making plasma. Sheet layout: see tools/gui_dark_chassis.py (plasma fill at (186,31)).
 */
public class PlasmaGeneratorScreen extends MachineScreen<PlasmaGeneratorMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/plasma_generator.png");
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int WARNING_COLOR = 0xFFFFB04A;
    private static final int AIR_COLOR = 0xFFC4DAEC;
    private static final int PLASMA_COLOR = 0xFFFF50D2;

    public PlasmaGeneratorScreen(PlasmaGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.PLASMA);
    }

    @Override
    protected boolean isActive() {
        return menu.getRate() > 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawPlasmaGauge(graphics, menu.getPlasma(), menu.getPlasmaCapacity());
        int litDots = Math.round(menu.getIntake() / 2.0F);
        for (int i = 0; i < 3; i++) {
            if (i < litDots) {
                graphics.fill(leftPos + 67 + i * 8, topPos + 31, leftPos + 69 + i * 8, topPos + 33, AIR_COLOR);
            }
        }
        if (menu.getRate() > 0) {
            graphics.fill(leftPos + 103, topPos + 31, leftPos + 105, topPos + 33, PLASMA_COLOR);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, Component.translatable("gui.comminutioncraft.intake", menu.getIntake()), 30, 20, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.plasma_rate", menu.getRate()), 110, 20, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.generating",
                menu.getRate() * PlasmaGeneratorBlockEntity.ENERGY_PER_MB), 30, 40, LABEL_COLOR, false);
        if (!menu.hasFilter()) {
            graphics.text(font, Component.translatable("gui.comminutioncraft.no_filter"), 102, 57, WARNING_COLOR, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        plasmaTooltip(graphics, mouseX, mouseY, menu.getPlasma(), menu.getPlasmaCapacity());
        if (!menu.hasFilter() && isHovering(79, 52, 18, 18, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.no_filter.desc"), mouseX, mouseY);
        }
    }
}
