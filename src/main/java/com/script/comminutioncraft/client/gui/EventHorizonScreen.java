package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EventHorizonMenu;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity.Status;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the Event Horizon Controller: energy (left), antimatter for ignition (x 155), the matter intake slot, the
 * status, mass and FE/t, and a gold bar for the black hole's stored mass. Sheet: tools/gui_dark_chassis.py
 * ({@code horizon_sheet}).
 */
public class EventHorizonScreen extends MachineScreen<EventHorizonMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/horizon_controller.png");
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int GOOD_COLOR = 0xFFFFBA38;
    private static final int WARNING_COLOR = 0xFFFFB04A;
    private static final int TEXT_X = 30;
    private static final int BAR_X = 30;
    private static final int BAR_Y = 56;
    private static final int BAR_W = 78;
    private static final int BAR_H = 5;

    public EventHorizonScreen(EventHorizonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.SINGULARITY);
    }

    @Override
    protected boolean isActive() {
        return menu.getStatus() == Status.RUNNING;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawGauge(graphics, PLASMA_X, ANTIMATTER_U, menu.getAntimatter(), menu.getAntimatterCapacity());
        int width = Math.min(BAR_W, (int) ((long) BAR_W * menu.getMass() / menu.getMassCapacity()));
        if (width > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + BAR_X, topPos + BAR_Y, 176, 90, width, BAR_H, TEX_SIZE, TEX_SIZE);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Status status = menu.getStatus();
        int color = status == Status.RUNNING ? GOOD_COLOR : status == Status.OFF || status == Status.FULL ? LABEL_COLOR : WARNING_COLOR;
        graphics.text(font, Component.translatable("gui.comminutioncraft.horizon." + status.name().toLowerCase(Locale.ROOT)),
                TEXT_X, 20, color, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.horizon.mass", formatFe(menu.getMass())), TEXT_X, 31, LABEL_COLOR, false);
        Component third = status == Status.HUNGRY
                ? Component.translatable("gui.comminutioncraft.horizon.evaporates", menu.getStarveSeconds())
                : Component.translatable("gui.comminutioncraft.generating", formatFe(menu.getRate()));
        graphics.text(font, third, TEXT_X, 42, status == Status.HUNGRY ? WARNING_COLOR : LABEL_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.antimatter", menu.getAntimatter(), menu.getAntimatterCapacity());
        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.horizon.mass_bar",
                    formatFe(menu.getMass()), formatFe(menu.getMassCapacity())), mouseX, mouseY);
        }
    }
}
