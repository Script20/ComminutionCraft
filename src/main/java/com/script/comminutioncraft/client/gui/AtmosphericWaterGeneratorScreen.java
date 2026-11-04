package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorMenu;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the Atmospheric Water Generator: water gauge (right), the Air Filter slot, and an air -> water strip whose
 * three intake dots light up with the open faces (one dot per two faces) and whose water dot lights while it's
 * making water. Intake, humidity and rate are written above. Sheet layout: see tools/gui_dark_chassis.py.
 */
public class AtmosphericWaterGeneratorScreen extends MachineScreen<AtmosphericWaterGeneratorMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/atmospheric_water_generator.png");
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int WARNING_COLOR = 0xFFFFB04A;
    private static final int AIR_COLOR = 0xFFC4DAEC;
    private static final int WATER_COLOR = 0xFF5696E4;

    public AtmosphericWaterGeneratorScreen(AtmosphericWaterGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.PLASMA);
    }

    @Override
    protected boolean isActive() {
        return menu.getRateTenths() > 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawGauge(graphics, PLASMA_X, WATER_U, menu.getWater(), menu.getWaterCapacity());
        int litDots = Math.round(menu.getIntake() / 2.0F);
        for (int i = 0; i < 3; i++) {
            if (i < litDots) {
                graphics.fill(leftPos + 67 + i * 8, topPos + 31, leftPos + 69 + i * 8, topPos + 33, AIR_COLOR);
            }
        }
        if (menu.getRateTenths() > 0) {
            graphics.fill(leftPos + 103, topPos + 31, leftPos + 105, topPos + 33, WATER_COLOR);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, Component.translatable("gui.comminutioncraft.intake", menu.getIntake()), 30, 20, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.humidity", menu.getHumidity()), 30, 40, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.fluid_rate",
                String.format(Locale.ROOT, "%.1f", menu.getRateTenths() / 10.0F)), 110, 20, LABEL_COLOR, false);
        if (!menu.hasFilter()) {
            graphics.text(font, Component.translatable("gui.comminutioncraft.no_filter"), 102, 57, WARNING_COLOR, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.water", menu.getWater(), menu.getWaterCapacity());
        if (!menu.hasFilter() && isHovering(79, 52, 18, 18, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.no_filter.desc"), mouseX, mouseY);
        }
    }
}
